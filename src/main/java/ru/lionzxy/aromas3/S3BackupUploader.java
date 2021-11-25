package ru.lionzxy.aromas3;

import aroma1997.backup.common.notification.IBackupNotification;
import aroma1997.backup.common.storageformat.IBackupInfo;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.Item;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

        if (AromaBackupConfig.keep_latest > 0) {
            try {
                List<Result<Item>> objects = new ArrayList<>();
                List<String> remove = new ArrayList<>();
                client.listObjects(AromaBackupConfig.bucket_name).forEach(objects::add);
                for (int i = objects.size() - AromaBackupConfig.keep_latest - 1; i >= 0; --i)
                    remove.add(objects.get(i).get().objectName());
                int failed = 0;
                for (Result<DeleteError> result : client.removeObjects(AromaBackupConfig.bucket_name, remove)) {
                    logger.error(String.format("Failed deleting old backup %s", result.get().objectName()));
                    ++failed;
                }
                logger.info(String.format("Deleted %d old backup object(s)", remove.size() - failed));
            } catch (Exception ex) {
                logger.error("Failed deleting oldest backup objects", ex);
            }
        }

        if (AromaBackupConfig.delete_after_upload) {
            if (file.delete()) {
                logger.info("Delete local backup file " + file.getAbsolutePath() + " successful");
            } else {
                logger.error("Delete local backup file " + file.getAbsolutePath() + " unsuccessful");
            }
        }
    }
}
