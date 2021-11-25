package ru.lionzxy.aromas3;

import net.minecraftforge.common.config.Config;

@Config(modid = AromaBackupS3.MODID)
public class AromaBackupConfig {
    @Config.Comment("Enable/Disable upload via S3")
    public static boolean enable = false;

    @Config.Comment("URL to object storage service")
    public static String url = "https://play.min.io";

    @Config.Comment("Access key is like user ID that uniquely identifies your account")
    public static String access_key = "Q3AM3UQ867SPQQA43P2F";

    @Config.Comment("Secret key is the password to your account")
    public static String secret_key = "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG";

    public static String bucket_name = "minecraft";
    public static boolean delete_after_upload = true;

    @Config.RangeInt(min = 0)
    @Config.Comment("The maximum amount of backup objects to keep in bucket (0 = all)")
    public static int keep_latest = 10;
}
