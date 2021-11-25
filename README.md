# AromaBackup S3 addon

Mod for uploading backup file to S3 storage (like Amazon S3)

# Getting started

1. Create your own S3 cloud storage. (ex. [minio](https://github.com/minio/minio))
2. Download and put in mods folder [AromaBackup](https://www.curseforge.com/minecraft/mc-mods/aromabackup) and AromaBackupS3 addon
3. Set up config mod 'config/aromabackups3.cfg' with S3 credential 
4. Enable `enable=true`

# Config example

```
general {
    # Access key is like user ID that uniquely identifies your account
    S:access_key=Q3AM3UQ867SPQQA43P2F
    S:bucket_name=minecraft
    B:delete_after_upload=true

    # Enable/Disable upload via S3
    B:enable=true

    # The maximum amount of backup objects to keep in bucket (0 = all)
    # Min: 0
    # Max: 2147483647
    I:keep_latest=10

    # Secret key is the password to your account
    S:secret_key=zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG

    # URL to object storage service
    S:url=https://play.min.io
}
```
