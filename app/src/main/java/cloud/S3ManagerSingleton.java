package cloud;

import android.content.Context;
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

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.WebSocket;

public class S3ManagerSingleton {

    private static volatile S3ManagerSingleton instance;
    private static final Object lock = new Object();
    private S3Credentials s3Credentials;
    private AmazonS3Client s3Client;
    private TransferUtility transferUtility;
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
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
        Log.d("S3Manager:Credentials", "AWS credentials updated successfully");

        scheduleCredentialRefresh();
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

    public void uploadFile(String localFilePath, String s3Key) {
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
                            Log.d("S3Manager:Upload", "Upload completed successfully: " + s3Key);
                        } else if (state == TransferState.FAILED) {
                            Log.e("S3Manager:Upload", "Upload failed: " + s3Key);
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        int percentDone = (int) (((float) bytesCurrent / (float) bytesTotal) * 100);
                        Log.d("S3Manager:Upload", "Upload progress: " + percentDone + "%");
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
                            Log.d("S3Manager:Download", "Download completed successfully: " + s3Key);
                        } else if (state == TransferState.FAILED) {
                            Log.e("S3Manager:Download", "Download failed: " + s3Key);
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        int percentDone = (int) (((float) bytesCurrent / (float) bytesTotal) * 100);
                        Log.d("S3Manager:Download", "Download progress: " + percentDone + "%");
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
        if (s3Credentials == null) {
            Log.e("S3Manager:CreateFolder", "AWS credentials not initialized");
            return;
        }
        executorService.execute(() -> {
            try {
                String finalFolderPath = folderPath.endsWith("/") ? folderPath : folderPath + "/";
                s3Client.putObject(s3Credentials.getBucketName(), finalFolderPath, "");
                Log.d("S3Manager:CreateFolder", "Folder created successfully: " + finalFolderPath);
                callback.onSuccess(Map.of("folderPath", finalFolderPath));
            } catch (Exception e) {
                Log.e("S3Manager:CreateFolder", "Error creating folder", e);
                callback.onError(e);
            }
        });
    }

    public void deleteFolder(String folderPath, S3Callback callback) {
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
                    Log.d("S3Manager:DeleteFolder", "Deleted object: " + objectSummary.getKey());
                }
                Log.d("S3Manager:DeleteFolder", "Folder deleted successfully: " + finalFolderPath);
                callback.onSuccess(Map.of("folderPath", finalFolderPath));
            } catch (Exception e) {
                Log.e("S3Manager:DeleteFolder", "Error deleting folder", e);
                callback.onError(e);
            }
        });
    }

    public void uploadFolderToS3(String localFolderPath, String s3TargetPath, S3Callback callback) {
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
    }

    public void downloadFolderFromS3(String s3FolderPath, String localFolderPath, S3Callback callback) {
        if (s3Credentials == null) {
            Log.e("S3Manager:DownloadFolder", "AWS credentials not initialized");
            return;
        }
        executorService.execute(() -> {
            try {
                String finalS3FolderPath = s3FolderPath.endsWith("/") ? s3FolderPath : s3FolderPath + "/";
                ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(s3Credentials.getBucketName()).withPrefix(finalS3FolderPath);
                ObjectListing objectListing = s3Client.listObjects(listObjectsRequest);
                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    String key = objectSummary.getKey();
                    if (key.endsWith("/")) continue;
                    String relativePath = key.replace(finalS3FolderPath, "");
                    File localFile = new File(localFolderPath, relativePath);
                    if (!localFile.getParentFile().exists()) localFile.getParentFile().mkdirs();
                    transferUtility.download(s3Credentials.getBucketName(), key, localFile).setTransferListener(new TransferListener() {
                        @Override
                        public void onStateChanged(int id, TransferState state) {
                            if (state == TransferState.COMPLETED) {
                                Log.d("S3Manager:DownloadFolder", "File downloaded successfully: " + key);
                            } else if (state == TransferState.FAILED) {
                                Log.e("S3Manager:DownloadFolder", "Download failed: " + key);
                            }
                        }

                        @Override
                        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                            int percentDone = (int) (((float) bytesCurrent / (float) bytesTotal) * 100);
                            Log.d("S3Manager:DownloadFolder", "Download progress (" + key + "): " + percentDone + "%");
                        }

                        @Override
                        public void onError(int id, Exception ex) {
                            Log.e("S3Manager:DownloadFolder", "Error downloading file: " + key, ex);
                        }
                    });
                }
                Log.d("S3Manager:DownloadFolder", "Folder download completed: " + s3FolderPath);
                callback.onSuccess(Map.of("folderPath", localFolderPath));
            } catch (Exception e) {
                Log.e("S3Manager:DownloadFolder", "Error downloading folder", e);
                callback.onError(e);
            }
        });
    }

    public void getFileSize(String s3FilePath, S3Callback callback) {
        if (s3Credentials == null) {
            Log.e("S3Manager:GetFileSize", "AWS credentials not initialized");
            return;
        }
        executorService.execute(() -> {
            try {
                ObjectListing objectListing = s3Client.listObjects(s3Credentials.getBucketName(), s3FilePath);
                if (!objectListing.getObjectSummaries().isEmpty()) {
                    long fileSize = objectListing.getObjectSummaries().get(0).getSize();
                    Log.d("S3Manager:GetFileSize", "File size of " + s3FilePath + ": " + fileSize + " bytes");
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

                Log.d("S3Manager:GetFolderSize", "Dimensione totale della cartella " + s3FolderPath + ": " + totalSize + " byte");
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
            Log.d("S3Manager:Refresh", "Recreating scheduler...");
            scheduler = Executors.newSingleThreadScheduledExecutor();
        }

        // Annulla qualsiasi job precedente
        if (scheduledRefresh != null && !scheduledRefresh.isCancelled()) {
            scheduledRefresh.cancel(true);
        }

        long currentTime = System.currentTimeMillis() / 1000L; // in secondi
        long delayInSeconds = s3Credentials.getExpirationTime() - currentTime - 60; // 1 minuto prima

        if (delayInSeconds <= 0) {
            Log.w("S3Manager:Refresh", "Expiration time is too close or already passed.");
            return;
        }


        scheduledRefresh = scheduler.schedule(() -> {
            Log.d("S3Manager:Refresh", "Refreshing credentials...");
            try {
                JSONObject command = new JSONObject().put("type", "temp_credentials");
                webSocket.send(command.toString());
            } catch (JSONException e) {
                System.out.println("Error creating command temp credentials JSON object: " + e.getMessage());
            }
        }, delayInSeconds, TimeUnit.SECONDS);

        Log.d("S3Manager:Refresh", "Scheduled credential refresh in " + delayInSeconds + " seconds");
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
                            Log.d("S3Manager:UploadFolder", "File uploaded successfully: " + s3Key);
                        } else if (state == TransferState.FAILED) {
                            Log.e("S3Manager:UploadFolder", "Upload failed: " + s3Key);
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        int percentDone = (int) (((float) bytesCurrent / (float) bytesTotal) * 100);
                        Log.d("S3Manager:UploadFolder", "Upload progress (" + s3Key + "): " + percentDone + "%");
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


}
