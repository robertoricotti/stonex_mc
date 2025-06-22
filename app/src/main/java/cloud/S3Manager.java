package cloud;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.cpdevice.cpcomm.boards.CPDEVICE;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import gui.MyApp;
import gui.dialogs_and_toast.CustomToast;

public class S3Manager {
    ProgressDialog progressDialog;
    Handler mainHandler;
    private final String bucketName;
    private final AmazonS3Client s3Client;
    private final TransferUtility transferUtility;
    private final ExecutorService executorService;

    public S3Manager(Context context, String accessKey, String secretKey, String bucketName) {
        this.bucketName = bucketName;

        // Configura le credenziali AWS
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);


        // Inizializza il client S3
        this.s3Client = new AmazonS3Client(credentials);

        // Inizializza il TransferUtility
        this.transferUtility = TransferUtility.builder()
                .context(context)
                .s3Client(s3Client)
                .build();

        // Inizializza ExecutorService con un pool di thread
        this.executorService = Executors.newFixedThreadPool(4); // Numero di thread configurabile
    }

    // Metodo per terminare il pool di thread quando l'app è chiusa
    public void shutdown() {
        executorService.shutdown();
    }

    // Metodo per caricare un file su S3
    public void uploadFile(String localFilePath, String s3Key) {
        executorService.execute(() -> {
            try {
                File file = new File(localFilePath);

                // Verifica che il file locale esista
                if (!file.exists()) {
                    Log.e("S3Manager:Upload", "File non trovato: " + localFilePath);
                    return;
                }

                // Esegui l'upload
                transferUtility.upload(bucketName, s3Key, file).setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if (state == TransferState.COMPLETED) {
                            Log.d("S3Manager:Upload", "Upload completato con successo: " + s3Key);
                        } else if (state == TransferState.FAILED) {
                            Log.e("S3Manager:Upload", "Upload fallito: " + s3Key);
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                        int percentDone = (int) percentDonef;
                        Log.d("S3Manager:Upload", "Progresso upload: " + percentDone + "%");
                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        Log.e("S3Manager:Upload", "Errore durante l'upload", ex);
                    }
                });
            } catch (Exception e) {
                Log.e("S3Manager:Upload", "Errore generale durante l'upload", e);
            }
        });
    }

    // Metodo per scaricare un file da S3
    public void downloadFile(String s3Key, String localFilePath) {
        executorService.execute(() -> {
            try {
                File file = new File(localFilePath);

                // Esegui il download
                transferUtility.download(bucketName, s3Key, file).setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if (state == TransferState.COMPLETED) {
                            Log.d("S3Manager:Download", "Download completato con successo: " + s3Key);
                        } else if (state == TransferState.FAILED) {
                            Log.e("S3Manager:Download", "Download fallito: " + s3Key);
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                        int percentDone = (int) percentDonef;

                        Log.d("S3Manager:Download", "Progresso download: " + percentDone + "%");
                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        Log.e("S3Manager:Download", "Errore durante il download", ex);
                    }
                });
            } catch (Exception e) {
                Log.e("S3Manager:Download", "Errore generale durante il download", e);
            }
        });
    }

    // Metodo per ottenere l'albero dei file da S3
    public void getTreeFromS3(List<String> serials, S3Callback callback) {
        executorService.execute(() -> {
            Map<String, Object> tree = new HashMap<>();
            try {
                for (String serial : serials) {
                    String prefix = "serials/" + serial + "/";

                    // Richiesta per elencare gli oggetti con il prefisso
                    ListObjectsRequest request = new ListObjectsRequest()
                            .withBucketName(bucketName)
                            .withPrefix(prefix);

                    ObjectListing objectListing = s3Client.listObjects(request);

                    Map<String, Object> serialTree = new HashMap<>();

                    // Itera sugli oggetti trovati
                    for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                        addToTree(serialTree, objectSummary.getKey(), prefix);
                    }

                    tree.put(serial, serialTree);
                }

                // Chiamata di successo
                callback.onSuccess(tree);

            } catch (Exception e) {
                // Chiamata in caso di errore
                callback.onError(e);
            }
        });
    }

    // Metodo helper per costruire l'albero dei file
    private void addToTree(Map<String, Object> tree, String key, String prefix) {
        String relativePath = key.replace(prefix, "");
        String[] parts = relativePath.split("/");

        Map<String, Object> currentLevel = tree;
        for (int i = 0; i < parts.length; i++) {
            if (i == parts.length - 1 && !key.endsWith("/")) {
                currentLevel.put(parts[i], null); // È un file
            } else {
                currentLevel.putIfAbsent(parts[i], new HashMap<>()); // È una directory
                currentLevel = (Map<String, Object>) currentLevel.get(parts[i]);
            }
        }
    }

    public void createFolder(String folderPath, S3Callback callback) {
        executorService.execute(() -> {
            try {
                // Crea una variabile effettivamente finale
                String finalFolderPath = folderPath.endsWith("/") ? folderPath : folderPath + "/";

                // Crea un oggetto vuoto con il percorso della cartella
                s3Client.putObject(bucketName, finalFolderPath, "");

                Log.d("S3Manager:CreateFolder", "Cartella creata con successo: " + finalFolderPath);
                callback.onSuccess(Map.of("folderPath", finalFolderPath));
            } catch (Exception e) {
                Log.e("S3Manager:CreateFolder", "Errore durante la creazione della cartella", e);
                callback.onError(e);
            }
        });
    }

    public void deleteFolder(String folderPath, S3Callback callback) {
        executorService.execute(() -> {
            try {
                // Assicurati che il percorso termini con "/"
                String finalFolderPath = folderPath.endsWith("/") ? folderPath : folderPath + "/";

                // Elenca tutti gli oggetti con il prefisso della cartella
                ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                        .withBucketName(bucketName)
                        .withPrefix(finalFolderPath);

                ObjectListing objectListing = s3Client.listObjects(listObjectsRequest);

                // Elimina tutti gli oggetti trovati
                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    s3Client.deleteObject(bucketName, objectSummary.getKey());
                    Log.d("S3Manager:DeleteFolder", "Oggetto eliminato: " + objectSummary.getKey());
                }

                Log.d("S3Manager:DeleteFolder", "Cartella eliminata con successo: " + finalFolderPath);
                callback.onSuccess(Map.of("folderPath", finalFolderPath));
            } catch (Exception e) {
                Log.e("S3Manager:DeleteFolder", "Errore durante l'eliminazione della cartella", e);
                callback.onError(e);
            }
        });
    }

    public void uploadFolderToS3(String localFolderPath, String s3TargetPath, S3Callback callback) {
        executorService.execute(() -> {
            try {
                File localFolder = new File(localFolderPath);

                // Verifica che la cartella locale esista
                if (!localFolder.exists() || !localFolder.isDirectory()) {
                    Log.e("S3Manager:UploadFolder", "Cartella non trovata o non valida: " + localFolderPath);
                    callback.onError(new Exception("Cartella non trovata o non valida: " + localFolderPath));
                    return;
                }

                // Carica ricorsivamente tutti i file e le directory
                uploadFolderRecursive(localFolder, s3TargetPath);

                Log.d("S3Manager:UploadFolder", "Caricamento della cartella completato: " + s3TargetPath);
                callback.onSuccess(Map.of("folderPath", s3TargetPath));
            } catch (Exception e) {
                Log.e("S3Manager:UploadFolder", "Errore durante il caricamento della cartella", e);
                callback.onError(e);
            }
        });
    }

    private void uploadFolderRecursive(File folder, String s3BasePath) {
        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                // Percorso del file su S3
                String s3Key = s3BasePath + "/" + file.getName();

                // Esegui l'upload del file
                transferUtility.upload(bucketName, s3Key, file).setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if (state == TransferState.COMPLETED) {
                            Log.d("S3Manager:UploadFolder", "File caricato con successo: " + s3Key);
                        } else if (state == TransferState.FAILED) {
                            Log.e("S3Manager:UploadFolder", "Caricamento fallito: " + s3Key);
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                        int percentDone = (int) percentDonef;
                        Log.d("S3Manager:UploadFolder", "Progresso upload (" + s3Key + "): " + percentDone + "%");
                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        Log.e("S3Manager:UploadFolder", "Errore durante l'upload del file: " + s3Key, ex);
                    }
                });
            } else if (file.isDirectory()) {
                // Upload ricorsivo per le sottodirectory
                String newS3Path = s3BasePath + "/" + file.getName();
                uploadFolderRecursive(file, newS3Path);
            }
        }
    }
    public void downloadFolderFromS3(Context context, String s3FolderPath, String localFolderPath, S3Callback callback) {
        executorService.execute(() -> {
            try {
                String finalS3FolderPath = s3FolderPath.endsWith("/") ? s3FolderPath : s3FolderPath + "/";
                ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                        .withBucketName(bucketName)
                        .withPrefix(finalS3FolderPath);

                ObjectListing objectListing = s3Client.listObjects(listObjectsRequest);
                List<S3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();
                int totalFiles = objectSummaries.size();
                AtomicInteger completedFiles = new AtomicInteger(0);
                AtomicInteger failedFiles = new AtomicInteger(0);
                AtomicBoolean isDownloading = new AtomicBoolean(true);

                if (totalFiles == 0) {
                    callback.onError(new Exception("La cartella è vuota o non esiste."));
                    return;
                }

                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(() -> {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setMessage("Download in corso...");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setCancelable(false);
                    progressDialog.setMax(totalFiles);
                    progressDialog.show();
                });

                // Timer di timeout per chiudere la progress se non ci sono progressi per 60 secondi
                Handler timeoutHandler = new Handler(Looper.getMainLooper());
                Runnable timeoutRunnable = () -> {
                    if (isDownloading.get()) {
                        Log.e("S3Manager", "Timeout: nessun progresso per 60 secondi. Chiusura del download.");
                        mainHandler.post(progressDialog::dismiss);
                        callback.onError(new Exception("Timeout: download bloccato per 60 secondi senza progressi."));
                    }
                };

                // Avvia il timer di timeout
                timeoutHandler.postDelayed(timeoutRunnable, 60000);

                for (S3ObjectSummary objectSummary : objectSummaries) {
                    String key = objectSummary.getKey();
                    if (key.endsWith("/")) continue;

                    String relativePath = key.replace(finalS3FolderPath, "");
                    File localFile = new File(localFolderPath, relativePath);
                    if (!localFile.getParentFile().exists()) {
                        localFile.getParentFile().mkdirs();
                    }

                    transferUtility.download(bucketName, key, localFile).setTransferListener(new TransferListener() {
                        @Override
                        public void onStateChanged(int id, TransferState state) {
                            if (state == TransferState.COMPLETED) {
                                Log.d("S3Manager", "Download completato: " + key);
                                completedFiles.incrementAndGet();
                            } else if (state == TransferState.FAILED) {
                                Log.e("S3Manager", "Download fallito: " + key);
                                failedFiles.incrementAndGet();
                            }

                            // Aggiorna il progresso
                            mainHandler.post(() -> progressDialog.setProgress(completedFiles.get() + failedFiles.get()));

                            // Resetta il timer di timeout ogni volta che c'è progresso
                            timeoutHandler.removeCallbacks(timeoutRunnable);
                            timeoutHandler.postDelayed(timeoutRunnable, 60000);

                            // Se tutti i file sono stati processati, chiudi il dialog
                            if (completedFiles.get() + failedFiles.get() == totalFiles) {
                                isDownloading.set(false);
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
                            if (bytesTotal > 0) {
                                int progress = (int) ((bytesCurrent * 100) / bytesTotal);
                                Log.d("S3Manager", "Download progresso (" + key + "): " + progress + "%");
                            }

                            // Resetta il timer di timeout se c'è progresso
                            timeoutHandler.removeCallbacks(timeoutRunnable);
                            timeoutHandler.postDelayed(timeoutRunnable, 30000);
                        }

                        @Override
                        public void onError(int id, Exception ex) {
                            Log.e("S3Manager", "Errore nel download del file: " + key, ex);
                            failedFiles.incrementAndGet();
                            mainHandler.post(() -> {
                                new CustomToast(MyApp.visibleActivity,"Error Downloading").show_error();
                            });
                            mainHandler.post(() -> progressDialog.setProgress(completedFiles.get() + failedFiles.get()));
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("S3Manager", "Errore durante il download della cartella", e);
                callback.onError(e);
            }
        });
    }




    public void getFileSize(String s3FilePath, S3Callback callback) {
        executorService.execute(() -> {
            try {
                // Ottiene le informazioni dell'oggetto su S3
                ObjectListing objectListing = s3Client.listObjects(bucketName, s3FilePath);

                if (!objectListing.getObjectSummaries().isEmpty()) {
                    long fileSize = objectListing.getObjectSummaries().get(0).getSize();
                    Log.d("S3Manager:GetFileSize", "Dimensione del file " + s3FilePath + ": " + fileSize + " byte");
                    callback.onSuccess(Map.of("filePath", s3FilePath, "size", fileSize));
                } else {
                    Log.e("S3Manager:GetFileSize", "File non trovato: " + s3FilePath);
                    callback.onError(new Exception("File non trovato: " + s3FilePath));
                }
            } catch (Exception e) {
                Log.e("S3Manager:GetFileSize", "Errore durante il recupero della dimensione del file", e);
                callback.onError(e);
            }
        });
    }

    // Metodo per elencare tutti i file in una specifica cartella su S3
    public void getFoldersFiles(String folderPath, S3Callback callback) {
        executorService.execute(() -> {
            Map<String, Object> files = new HashMap<>();
            try {
                // Assicurati che il percorso termini con "/"
                String finalFolderPath = folderPath.endsWith("/") ? folderPath : folderPath + "/";

                // Crea una richiesta per elencare gli oggetti nel bucket con il prefisso specificato
                ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                        .withBucketName(bucketName)
                        .withPrefix(finalFolderPath)
                        .withDelimiter("/");

                ObjectListing objectListing = s3Client.listObjects(listObjectsRequest);

                // Itera sugli oggetti trovati per elencare solo i file
                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    String key = objectSummary.getKey();
                    // Escludi le voci che rappresentano cartelle (che terminano con "/")
                    if (!key.endsWith("/")) {
                        files.put(key.replace(finalFolderPath, ""), objectSummary.getSize());
                    }
                }

                // Chiamata di successo con la mappa dei file trovati
                callback.onSuccess(files);

            } catch (Exception e) {
                // Chiamata in caso di errore
                callback.onError(e);
                Log.e("S3Manager:getFoldersFiles", "Errore durante l'elenco dei file", e);
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
                        .withBucketName(bucketName)
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


    // Interfaccia per il callback
    public interface S3Callback {
        void onSuccess(Map<String, Object> result);
        void onError(Exception e);
    }
}
