package ru.lionzxy.aromas3;

import aroma1997.backup.common.notification.IBackupNotification;
import aroma1997.backup.common.storageformat.IBackupInfo;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class S3BackupUploader implements IBackupNotification {
    // 200 MB
    private static long PART_SIZE = 200 * 1024 * 1024;
    private Executor executor = Executors.newSingleThreadExecutor();
    private Logger logger;
    private MinioClient client;

    public S3BackupUploader(MinioClient client, Logger logger) throws Exception {
        this.logger = logger;
        this.client = client;

        if (!client.bucketExists(AromaBackupConfig.bucket_name)) {
            client.makeBucket(AromaBackupConfig.bucket_name);
            logger.info("Create bucket " + AromaBackupConfig.bucket_name);
        }
    }

    @Override
    public void backupCreateEnd(IBackupInfo info) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                uploadBackup(info.getFile());
            }
        });
    }

    private void uploadBackup(File file) {
        logger.info("Start uploading file to S3 Storage: " + AromaBackupConfig.url + " bucket " + AromaBackupConfig.bucket_name);
        final String newFileName = System.currentTimeMillis() + "-" + file.getName();
        logger.info("With name: " + newFileName + " from " + file.getAbsolutePath());
        final PutObjectOptions putObjectOptions = new PutObjectOptions(file.length(), PART_SIZE);

        try {
            client.putObject(AromaBackupConfig.bucket_name, newFileName, file.getAbsolutePath(), putObjectOptions);
        } catch (Exception ex) {
            logger.error("Failed upload file to S3 storage", ex);
            return;
        }

        logger.info("Upload to S3 Storage successful!");

        if (AromaBackupConfig.delete_after_upload) {
            if (file.delete()) {
                logger.info("Delete local backup file " + file.getAbsolutePath() + " successful");
            } else {
                logger.error("Delete local backup file " + file.getAbsolutePath() + " unsuccessful");
            }
        }
    }
}
