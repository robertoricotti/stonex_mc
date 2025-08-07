package cloud;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.stx_dig.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import gui.MyApp;
import gui.dialogs_and_toast.CustomToast;
import okhttp3.WebSocket;

public class S3ManagerSingleton {
    ProgressDialog progressDialog;
    Handler mainHandler;
    private static volatile S3ManagerSingleton instance;
    private static final Object lock = new Object();
    private S3Credentials s3Credentials;
    private AmazonS3Client s3Client;
    private TransferUtility transferUtility;
    private  ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final Context appContext;
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledRefresh;
    private WebSocket webSocket;

    private S3ManagerSingleton(Context context) {
        this.appContext = context.getApplicationContext();
    }
    public static S3ManagerSingleton getInstance(Context context) {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new S3ManagerSingleton(context);
                }
            }
        }
        return instance;
    }
    public synchronized void setS3Credentials(String region, String accessKey, String secretKey, String sessionToken, String bucketName, long expirationUnixTimestamp) {
        this.s3Credentials = new S3Credentials(region, accessKey, secretKey, sessionToken, bucketName, expirationUnixTimestamp);

        BasicSessionCredentials credentials = new BasicSessionCredentials(s3Credentials.getAccessKey(), s3Credentials.getSecretKey(), s3Credentials.getSessionToken());
        this.s3Client = new AmazonS3Client(credentials, Region.getRegion(s3Credentials.getRegion()));
        this.transferUtility = TransferUtility.builder().context(appContext).s3Client(s3Client).build();
        //Log.d("S3Manager:Credentials", "AWS credentials updated successfully");
        //TODO fare controllo
        scheduleCredentialRefresh();
    }
    private void ensureExecutor() {
        if (executorService == null || executorService.isShutdown() || executorService.isTerminated()) {
            executorService = Executors.newFixedThreadPool(4);
        }
    }
    public void shutdown() {
        if (scheduledRefresh != null && !scheduledRefresh.isCancelled()) {
            scheduledRefresh.cancel(true);
        }
        if (executorService != null) {
            executorService.shutdownNow();
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        webSocket = null;
    }

    public static boolean isInitialized() {
        return instance != null;
    }
    public void getTreeFromS3V2(List<String> serials, S3Callback callback) {
        ensureExecutor();
        if (s3Credentials == null) {
            Log.e("S3Manager:GetTree", "AWS credentials not initialized");
            return;
        }
        executorService.execute(() -> {
            Map<String, Object> tree = new HashMap<>();
            try {
                for (String serial : serials) {
                    String prefix = "serials/" + serial + "/";
                    ListObjectsRequest request = new ListObjectsRequest()
                            .withBucketName(s3Credentials.getBucketName())
                            .withPrefix(prefix);
                    ObjectListing objectListing = s3Client.listObjects(request);
                    Map<String, Object> serialTree = new HashMap<>();

                    for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                        addToTreeWithMetadata(serialTree, objectSummary.getKey(), prefix, objectSummary.getLastModified(), objectSummary.getSize());
                    }

                    tree.put(serial, serialTree);
                }
                callback.onSuccess(tree);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void uploadFile(String localFilePath, String s3Key) {
        ensureExecutor();
        if (s3Credentials == null) {
            Log.e("S3Manager:Upload", "AWS credentials not initialized");
            return;
        }
        executorService.execute(() -> {
            try {
                File file = new File(localFilePath);
                if (!file.exists()) {
                    Log.e("S3Manager:Upload", "File not found: " + localFilePath);
                    return;
                }
                transferUtility.upload(s3Credentials.getBucketName(), s3Key, file).setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if (state == TransferState.COMPLETED) {
                            //Log.d("S3Manager:Upload", "Upload completed successfully: " + s3Key);
                        } else if (state == TransferState.FAILED) {
                            Log.e("S3Manager:Upload", "Upload failed: " + s3Key);
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        int percentDone = (int) (((float) bytesCurrent / (float) bytesTotal) * 100);
                        //Log.d("S3Manager:Upload", "Upload progress: " + percentDone + "%");
                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        Log.e("S3Manager:Upload", "Error during upload", ex);
                    }
                });
            } catch (Exception e) {
                Log.e("S3Manager:Upload", "General error during upload", e);
            }
        });
    }

    public void downloadFile(String s3Key, String localFilePath) {
        if (s3Credentials == null) {
            Log.e("S3Manager:Download", "AWS credentials not initialized");
            return;
        }
        executorService.execute(() -> {
            try {
                File file = new File(localFilePath);
                transferUtility.download(s3Credentials.getBucketName(), s3Key, file).setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if (state == TransferState.COMPLETED) {
                           // Log.d("S3Manager:Download", "Download completed successfully: " + s3Key);
                        } else if (state == TransferState.FAILED) {
                            Log.e("S3Manager:Download", "Download failed: " + s3Key);
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        int percentDone = (int) (((float) bytesCurrent / (float) bytesTotal) * 100);
                       // Log.d("S3Manager:Download", "Download progress: " + percentDone + "%");
                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        Log.e("S3Manager:Download", "Error during download", ex);
                    }
                });
            } catch (Exception e) {
                Log.e("S3Manager:Download", "General error during download", e);
            }
        });
    }

    public void getTreeFromS3(List<String> serials, S3Callback callback) {
        ensureExecutor();
        if (s3Credentials == null) {
            Log.e("S3Manager:GetTree", "AWS credentials not initialized");
            return;
        }
        executorService.execute(() -> {
            Map<String, Object> tree = new HashMap<>();
            try {
                for (String serial : serials) {
                    String prefix = "serials/" + serial + "/";
                    ListObjectsRequest request = new ListObjectsRequest().withBucketName(s3Credentials.getBucketName()).withPrefix(prefix);
                    ObjectListing objectListing = s3Client.listObjects(request);
                    Map<String, Object> serialTree = new HashMap<>();
                    for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                        addToTree(serialTree, objectSummary.getKey(), prefix);
                    }
                    tree.put(serial, serialTree);
                }
                callback.onSuccess(tree);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void createFolder(String folderPath, S3Callback callback) {
        ensureExecutor();
        if (s3Credentials == null) {
            Log.e("S3Manager:CreateFolder", "AWS credentials not initialized");
            return;
        }
        executorService.execute(() -> {
            try {
                String finalFolderPath = folderPath.endsWith("/") ? folderPath : folderPath + "/";
                s3Client.putObject(s3Credentials.getBucketName(), finalFolderPath, "");
                //Log.d("S3Manager:CreateFolder", "Folder created successfully: " + finalFolderPath);
                callback.onSuccess(Map.of("folderPath", finalFolderPath));
            } catch (Exception e) {
                Log.e("S3Manager:CreateFolder", "Error creating folder", e);
                callback.onError(e);
            }
        });
    }

    public void deleteFolder(String folderPath, S3Callback callback) {
        ensureExecutor();
        if (s3Credentials == null) {
            Log.e("S3Manager:DeleteFolder", "AWS credentials not initialized");
            return;
        }
        executorService.execute(() -> {
            try {
                String finalFolderPath = folderPath.endsWith("/") ? folderPath : folderPath + "/";
                ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(s3Credentials.getBucketName()).withPrefix(finalFolderPath);
                ObjectListing objectListing = s3Client.listObjects(listObjectsRequest);
                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    s3Client.deleteObject(s3Credentials.getBucketName(), objectSummary.getKey());
                    //Log.d("S3Manager:DeleteFolder", "Deleted object: " + objectSummary.getKey());
                }
                //Log.d("S3Manager:DeleteFolder", "Folder deleted successfully: " + finalFolderPath);
                callback.onSuccess(Map.of("folderPath", finalFolderPath));
            } catch (Exception e) {
                Log.e("S3Manager:DeleteFolder", "Error deleting folder", e);
                callback.onError(e);
            }
        });
    }
    public void uploadFolderToS3(String localFolderPath, String s3TargetPath, S3Callback callback) {
        ensureExecutor();

        if (s3Credentials == null) {
            Log.e("S3Manager:UploadFolder", "AWS credentials not initialized");
            return;
        }

        executorService.execute(() -> {
            try {
                File localFolder = new File(localFolderPath);
                if (!localFolder.exists() || !localFolder.isDirectory()) {
                    Log.e("S3Manager:UploadFolder", "Folder not found or invalid: " + localFolderPath);
                    callback.onError(new Exception("Folder not found or invalid: " + localFolderPath));
                    return;
                }

                // Recupera tutti i file
                List<File> filesToUpload = getAllFiles(localFolder);

                // Calcola byte totali
                long totalBytes = filesToUpload.stream().mapToLong(File::length).sum();
                AtomicLong uploadedBytes = new AtomicLong(0);
                AtomicInteger completedFiles = new AtomicInteger(0);
                AtomicInteger failedFiles = new AtomicInteger(0);
                AtomicBoolean isUploading = new AtomicBoolean(true);

                Handler mainHandler = new Handler(Looper.getMainLooper());

                // Mostra ProgressDialog
                mainHandler.post(() -> {
                    progressDialog = new ProgressDialog(MyApp.visibleActivity);
                    progressDialog.setMessage(MyApp.visibleActivity.getString(R.string.uploading));
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setIndeterminate(false);
                    progressDialog.setCancelable(false);
                    progressDialog.setMax(100); // Percentuale
                    progressDialog.show();
                });

                // Timeout handler
                Handler timeoutHandler = new Handler(Looper.getMainLooper());
                Runnable timeoutRunnable = () -> {
                    if (isUploading.get()) {
                        Log.e("S3Manager", "Timeout: nessun progresso per 60 secondi. Chiusura upload.");
                        mainHandler.post(progressDialog::dismiss);
                        callback.onError(new Exception("Timeout: upload bloccato per 60 secondi senza progressi."));
                    }
                };
                timeoutHandler.postDelayed(timeoutRunnable, 60000);

                // Avvia caricamento
                for (File file : filesToUpload) {
                    String relativePath = localFolder.toURI().relativize(file.toURI()).getPath();
                    String s3Key = s3TargetPath.endsWith("/") ? s3TargetPath + relativePath : s3TargetPath + "/" + relativePath;

                    transferUtility.upload(s3Credentials.getBucketName(), s3Key, file)
                            .setTransferListener(new TransferListener() {
                                long lastReportedBytes = 0;

                                @Override
                                public void onStateChanged(int id, TransferState state) {
                                    if (state == TransferState.COMPLETED) {
                                        completedFiles.incrementAndGet();
                                        //Log.d("S3Manager:UploadFolder", "File caricato: " + s3Key);
                                    } else if (state == TransferState.FAILED) {
                                        failedFiles.incrementAndGet();
                                        Log.e("S3Manager:UploadFolder", "Errore upload: " + s3Key);
                                        mainHandler.post(() ->
                                                new CustomToast(MyApp.visibleActivity, "Error Uploading").show_error()
                                        );
                                    }

                                    // Controllo completamento
                                    if (completedFiles.get() + failedFiles.get() == filesToUpload.size()) {
                                        isUploading.set(false);
                                        timeoutHandler.removeCallbacks(timeoutRunnable);
                                        mainHandler.post(progressDialog::dismiss);

                                        if (failedFiles.get() == 0) {
                                            callback.onSuccess(Map.of("folderPath", s3TargetPath));
                                        } else {
                                            callback.onError(new Exception("Alcuni file non sono stati caricati correttamente."));
                                        }
                                    }
                                }

                                @Override
                                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                                    // Delta bytes caricati
                                    long delta = bytesCurrent - lastReportedBytes;
                                    if (delta > 0) {
                                        uploadedBytes.addAndGet(delta);
                                        lastReportedBytes = bytesCurrent;
                                    }

                                    int percent = (int) ((uploadedBytes.get() * 100) / totalBytes);

                                    // Aggiorna barra
                                    mainHandler.post(() -> progressDialog.setProgress(percent));

                                    // Resetta timeout
                                    timeoutHandler.removeCallbacks(timeoutRunnable);
                                    timeoutHandler.postDelayed(timeoutRunnable, 60000);
                                }

                                @Override
                                public void onError(int id, Exception ex) {
                                    failedFiles.incrementAndGet();
                                    Log.e("S3Manager:UploadFolder", "Errore caricamento file: " + s3Key, ex);
                                    mainHandler.post(() ->
                                            new CustomToast(MyApp.visibleActivity, "Error Uploading").show_error()
                                    );
                                }
                            });
                }
            } catch (Exception e) {
                Log.e("S3Manager:UploadFolder", "Errore upload cartella", e);
                callback.onError(e);
            }
        });
    }

    /**
     * Recupera ricorsivamente tutti i file nella cartella
     */
    private List<File> getAllFiles(File folder) {
        List<File> files = new ArrayList<>();
        File[] fileList = folder.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (file.isDirectory()) {
                    files.addAll(getAllFiles(file));
                } else {
                    files.add(file);
                }
            }
        }
        return files;
    }


    /*
    public void uploadFolderToS3(String localFolderPath, String s3TargetPath, S3Callback callback) {
        ensureExecutor();
        if (s3Credentials == null) {
            Log.e("S3Manager:UploadFolder", "AWS credentials not initialized");
            return;
        }
        executorService.execute(() -> {
            try {
                File localFolder = new File(localFolderPath);
                if (!localFolder.exists() || !localFolder.isDirectory()) {
                    Log.e("S3Manager:UploadFolder", "Folder not found or invalid: " + localFolderPath);
                    callback.onError(new Exception("Folder not found or invalid: " + localFolderPath));
                    return;
                }
                uploadFolderRecursive(localFolder, s3TargetPath);
                Log.d("S3Manager:UploadFolder", "Folder upload completed: " + s3TargetPath);
                callback.onSuccess(Map.of("folderPath", s3TargetPath));
            } catch (Exception e) {
                Log.e("S3Manager:UploadFolder", "Error uploading folder", e);
                callback.onError(e);
            }
        });
    }*/


    public void downloadFolderFromS3(String s3FolderPath, String localFolderPath, S3Callback callback) {
        ensureExecutor();

        if (s3Credentials == null) {
            Log.e("S3Manager:DownloadFolder", "AWS credentials not initialized");
            return;
        }

        executorService.execute(() -> {
            try {
                String finalS3FolderPath = s3FolderPath.endsWith("/") ? s3FolderPath : s3FolderPath + "/";
                ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                        .withBucketName(s3Credentials.getBucketName())
                        .withPrefix(finalS3FolderPath);

                ObjectListing objectListing = s3Client.listObjects(listObjectsRequest);
                List<S3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();

                // Calcola dimensione totale da scaricare (solo file, niente cartelle)
                long totalBytes = objectSummaries.stream()
                        .filter(obj -> !obj.getKey().endsWith("/"))
                        .mapToLong(S3ObjectSummary::getSize)
                        .sum();

                AtomicLong downloadedBytes = new AtomicLong(0);
                AtomicInteger completedFiles = new AtomicInteger(0);
                AtomicInteger failedFiles = new AtomicInteger(0);
                AtomicBoolean isDownloading = new AtomicBoolean(true);

                Handler mainHandler = new Handler(Looper.getMainLooper());

                // Mostra ProgressDialog
                mainHandler.post(() -> {
                    progressDialog = new ProgressDialog(MyApp.visibleActivity);
                    progressDialog.setMessage(MyApp.visibleActivity.getString(R.string.downloading));
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setIndeterminate(false);
                    progressDialog.setCancelable(false);
                    progressDialog.setMax(100); // Percentuale
                    progressDialog.show();
                });

                // Timeout handler
                Handler timeoutHandler = new Handler(Looper.getMainLooper());
                Runnable timeoutRunnable = () -> {
                    if (isDownloading.get()) {
                        Log.e("S3Manager", "Timeout: nessun progresso per 60 secondi. Chiusura del download.");
                        mainHandler.post(progressDialog::dismiss);
                        callback.onError(new Exception("Timeout: download bloccato per 60 secondi senza progressi."));
                    }
                };
                timeoutHandler.postDelayed(timeoutRunnable, 60000);

                // Avvia download file
                for (S3ObjectSummary objectSummary : objectSummaries) {
                    String key = objectSummary.getKey();
                    if (key.endsWith("/")) continue; // ignora directory vuote

                    String relativePath = key.replace(finalS3FolderPath, "");
                    File localFile = new File(localFolderPath, relativePath);
                    if (!localFile.getParentFile().exists()) {
                        localFile.getParentFile().mkdirs();
                    }

                    transferUtility.download(s3Credentials.getBucketName(), key, localFile)
                            .setTransferListener(new TransferListener() {
                                long lastReportedBytes = 0;

                                @Override
                                public void onStateChanged(int id, TransferState state) {
                                    if (state == TransferState.COMPLETED) {
                                        completedFiles.incrementAndGet();
                                        //Log.d("S3Manager:DownloadFolder", "File scaricato: " + key);
                                    } else if (state == TransferState.FAILED) {
                                        failedFiles.incrementAndGet();
                                        Log.e("S3Manager:DownloadFolder", "Errore download: " + key);
                                        mainHandler.post(() ->
                                                new CustomToast(MyApp.visibleActivity, "Error Downloading").show_error()
                                        );
                                    }

                                    // Controllo completamento
                                    if (completedFiles.get() + failedFiles.get() ==
                                            (int) objectSummaries.stream().filter(obj -> !obj.getKey().endsWith("/")).count()) {
                                        isDownloading.set(false);
                                        timeoutHandler.removeCallbacks(timeoutRunnable);
                                        mainHandler.post(progressDialog::dismiss);

                                        if (failedFiles.get() == 0) {
                                            callback.onSuccess(Map.of("folderPath", localFolderPath));
                                        } else {
                                            callback.onError(new Exception("Alcuni file non sono stati scaricati correttamente."));
                                        }
                                    }
                                }

                                @Override
                                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                                    // Calcola delta rispetto all'ultimo aggiornamento
                                    long delta = bytesCurrent - lastReportedBytes;
                                    if (delta > 0) {
                                        downloadedBytes.addAndGet(delta);
                                        lastReportedBytes = bytesCurrent;
                                    }

                                    int percent = (int) ((downloadedBytes.get() * 100) / totalBytes);

                                    // Aggiorna barra percentuale
                                    mainHandler.post(() -> progressDialog.setProgress(percent));

                                    // Resetta timeout
                                    timeoutHandler.removeCallbacks(timeoutRunnable);
                                    timeoutHandler.postDelayed(timeoutRunnable, 60000);
                                }

                                @Override
                                public void onError(int id, Exception ex) {
                                    failedFiles.incrementAndGet();
                                    Log.e("S3Manager:DownloadFolder", "Errore download file: " + key, ex);
                                    mainHandler.post(() ->
                                            new CustomToast(MyApp.visibleActivity, "Error Downloading").show_error()
                                    );
                                }
                            });
                }
            } catch (Exception e) {
                Log.e("S3Manager:DownloadFolder", "Errore download cartella", e);
                callback.onError(e);
            }
        });
    }





    public void getFileSize(String s3FilePath, S3Callback callback) {
        ensureExecutor();
        if (s3Credentials == null) {
            Log.e("S3Manager:GetFileSize", "AWS credentials not initialized");
            return;
        }
        executorService.execute(() -> {
            try {
                ObjectListing objectListing = s3Client.listObjects(s3Credentials.getBucketName(), s3FilePath);
                if (!objectListing.getObjectSummaries().isEmpty()) {
                    long fileSize = objectListing.getObjectSummaries().get(0).getSize();
                    //Log.d("S3Manager:GetFileSize", "File size of " + s3FilePath + ": " + fileSize + " bytes");
                    callback.onSuccess(Map.of("filePath", s3FilePath, "size", fileSize));
                } else {
                    Log.e("S3Manager:GetFileSize", "File not found: " + s3FilePath);
                    callback.onError(new Exception("File not found: " + s3FilePath));
                }
            } catch (Exception e) {
                Log.e("S3Manager:GetFileSize", "Error retrieving file size", e);
                callback.onError(e);
            }
        });
    }

    public void getFoldersFiles(String folderPath, S3Callback callback) {
        ensureExecutor();
        if (s3Credentials == null) {
            Log.e("S3Manager:GetFoldersFiles", "AWS credentials not initialized");
            return;
        }
        executorService.execute(() -> {
            Map<String, Object> files = new HashMap<>();
            try {
                String finalFolderPath = folderPath.endsWith("/") ? folderPath : folderPath + "/";
                ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                        .withBucketName(s3Credentials.getBucketName())
                        .withPrefix(finalFolderPath)
                        .withDelimiter("/");
                ObjectListing objectListing = s3Client.listObjects(listObjectsRequest);
                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    String key = objectSummary.getKey();
                    if (!key.endsWith("/")) {
                        files.put(key.replace(finalFolderPath, ""), objectSummary.getSize());
                    }
                }
                callback.onSuccess(files);
            } catch (Exception e) {
                callback.onError(e);
                Log.e("S3Manager:getFoldersFiles", "Error listing folder files", e);
            }
        });
    }
    public void getFolderSize(String s3FolderPath, S3Callback callback) {
        ensureExecutor();
        executorService.execute(() -> {
            try {
                // Assicurati che il percorso termini con "/"
                String finalS3FolderPath = s3FolderPath.endsWith("/") ? s3FolderPath : s3FolderPath + "/";

                // Elenca tutti gli oggetti con il prefisso della cartella
                ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                        .withBucketName(s3Credentials.getBucketName())
                        .withPrefix(finalS3FolderPath);

                ObjectListing objectListing = s3Client.listObjects(listObjectsRequest);

                long totalSize = 0;

                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    // Escludi cartelle vuote
                    if (!objectSummary.getKey().endsWith("/")) {
                        totalSize += objectSummary.getSize();
                    }
                }

                //Log.d("S3Manager:GetFolderSize", "Dimensione totale della cartella " + s3FolderPath + ": " + totalSize + " byte");
                callback.onSuccess(Map.of("folderPath", s3FolderPath, "size", totalSize));
            } catch (Exception e) {
                Log.e("S3Manager:GetFolderSize", "Errore durante il recupero della dimensione della cartella", e);
                callback.onError(e);
            }
        });
    }

    private void scheduleCredentialRefresh() {

        // Crea un nuovo scheduler se necessario
        if (scheduler.isShutdown() || scheduler.isTerminated()) {
            //Log.d("S3Manager:Refresh", "Recreating scheduler...");
            scheduler = Executors.newSingleThreadScheduledExecutor();
        }

        // Annulla qualsiasi job precedente
        if (scheduledRefresh != null && !scheduledRefresh.isCancelled()) {
            scheduledRefresh.cancel(true);
        }

        long currentTime = System.currentTimeMillis() / 1000L; // in secondi
        long delayInSeconds = s3Credentials.getExpirationTime() - currentTime - 60; // 1 minuto prima

        if (delayInSeconds <= 0) {
            //Log.w("S3Manager:Refresh", "Expiration time is too close or already passed.");
            return;
        }


        scheduledRefresh = scheduler.schedule(() -> {
            //Log.d("S3Manager:Refresh", "Refreshing credentials...");
            try {
                JSONObject command = new JSONObject().put("type", "temp_credentials");
                webSocket.send(command.toString());
            } catch (JSONException e) {
                System.out.println("Error creating command temp credentials JSON object: " + e.getMessage());
            }
        }, delayInSeconds, TimeUnit.SECONDS);

        //Log.d("S3Manager:Refresh", "Scheduled credential refresh in " + delayInSeconds + " seconds");
    }


    private void addToTree(Map<String, Object> tree, String key, String prefix) {
        String relativePath = key.replace(prefix, "");
        String[] parts = relativePath.split("/");
        Map<String, Object> currentLevel = tree;
        for (int i = 0; i < parts.length; i++) {
            if (i == parts.length - 1 && !key.endsWith("/")) {
                currentLevel.put(parts[i], null);
            } else {
                currentLevel.putIfAbsent(parts[i], new HashMap<>());
                currentLevel = (Map<String, Object>) currentLevel.get(parts[i]);
            }
        }
    }

    private void uploadFolderRecursive(File folder, String s3BasePath) {
        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                String s3Key = s3BasePath + "/" + file.getName();
                transferUtility.upload(s3Credentials.getBucketName(), s3Key, file).setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if (state == TransferState.COMPLETED) {
                            //Log.d("S3Manager:UploadFolder", "File uploaded successfully: " + s3Key);
                        } else if (state == TransferState.FAILED) {
                            Log.e("S3Manager:UploadFolder", "Upload failed: " + s3Key);
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        int percentDone = (int) (((float) bytesCurrent / (float) bytesTotal) * 100);
                        //Log.d("S3Manager:UploadFolder", "Upload progress (" + s3Key + "): " + percentDone + "%");
                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        Log.e("S3Manager:UploadFolder", "Error uploading file: " + s3Key, ex);
                    }
                });
            } else if (file.isDirectory()) {
                String newS3Path = s3BasePath + "/" + file.getName();
                uploadFolderRecursive(file, newS3Path);
            }
        }
    }

    public void setWebSocket(@NotNull WebSocket webSocket) {
        if (this.webSocket != null) {
            System.out.println("WebSocket already set");
            return;
        }
        this.webSocket = webSocket;

        try {
            JSONObject commandRequest = new JSONObject();
            commandRequest.put("type", "temp_credentials");
            commandRequest.put("timeStamp", System.currentTimeMillis());
            this.webSocket.send(commandRequest.toString());
        } catch (JSONException e) {
            System.out.println("Error creating command temp credentials JSON object: " + e.getMessage());
        }
    }

    public interface S3Callback {
        void onSuccess(Map<String, Object> result);

        void onError(Exception e);
    }
    private void addToTreeWithMetadata(Map<String, Object> tree, String key, String basePrefix, Date lastModified, long size) {
        String relativePath = key.substring(basePrefix.length());
        String[] parts = relativePath.split("/");

        Map<String, Object> currentLevel = tree;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];

            if (i == parts.length - 1) {
                // È un file: aggiungi con metadati
                Map<String, Object> fileData = new HashMap<>();
                fileData.put("type", "file");
                fileData.put("name", part);
                fileData.put("lastModified", lastModified);
                fileData.put("size", size);
                currentLevel.put(part, fileData);
            } else {
                // È una cartella: prosegui o crea
                if (!currentLevel.containsKey(part)) {
                    currentLevel.put(part, new HashMap<String, Object>());
                }
                currentLevel = (Map<String, Object>) currentLevel.get(part);
            }
        }
    }

}
