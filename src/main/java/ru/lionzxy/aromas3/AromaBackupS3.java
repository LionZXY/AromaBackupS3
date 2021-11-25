package ru.lionzxy.aromas3;

import aroma1997.backup.common.notification.NotificationHelper;
import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = AromaBackupS3.MODID, name = AromaBackupS3.NAME,
        version = AromaBackupS3.VERSION, dependencies = "required-after:aromabackup",
        acceptableRemoteVersions = "*")
public class AromaBackupS3 {
    public static final String MODID = "aromabackups3";
    public static final String NAME = "Aroma Backup S3";
    public static final String VERSION = "1.2";

    private static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (!AromaBackupConfig.enable) {
            logger.info("Upload to S3 storage DISABLED");
            return;
        }
        logger.info("Upload to S3 storage ENABLED");

        try {
            initListener();
        } catch (Exception ex) {
            logger.error("Failed init backup listener and S3 storage", ex);
        }

    }

    private void initListener() throws Exception {
        final MinioClient minioClient = new MinioClient(AromaBackupConfig.url, AromaBackupConfig.access_key, AromaBackupConfig.secret_key);

        NotificationHelper.INSTANCE.registerNotification(new S3BackupUploader(minioClient, logger));
    }
}
