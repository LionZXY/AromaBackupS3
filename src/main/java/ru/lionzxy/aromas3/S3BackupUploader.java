package ru.lionzxy.aromas3;

import aroma1997.backup.common.notification.IBackupNotification;
import aroma1997.backup.common.storageformat.IBackupInfo;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import io.minio.errors.*;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class S3BackupUploader implements IBackupNotification {
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
        final File file = info.getFile();
        logger.info("Start uploading file to S3 Storage: " + AromaBackupConfig.url + " bucket " + AromaBackupConfig.bucket_name);
        final String newFileName = System.currentTimeMillis() + "-" + file.getName();
        logger.info("With name: " + newFileName + " from " + file.getAbsolutePath());
        final PutObjectOptions putObjectOptions = new PutObjectOptions(file.length(), -1);

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
