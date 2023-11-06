package io.cresco.cpms.storage.utilities;

import io.cresco.cpms.processing.StorageEngine;

public class StorageParameters {
    public StorageProvider storageProvider;
    public String bucket;
    public String prefix;
    public String path;

    public StorageParameters(String path) {
        this.path = path;
        if (path.startsWith("s3://")) {
            this.storageProvider = StorageProvider.S3;
            String cloudPath = path.replace("s3://", "");
            if (!cloudPath.isEmpty()) {
                int prefixIndex = cloudPath.indexOf("/");
                if (prefixIndex > -1) {
                    this.bucket = cloudPath.substring(0, prefixIndex);
                    this.prefix = cloudPath.substring(prefixIndex + 1);
                } else {
                    this.bucket = cloudPath;
                }
            }
        } else if (path.startsWith("gs://")) {
            this.storageProvider = StorageProvider.GCS;
            String cloudPath = path.replace("gs://", "");
            if (!cloudPath.isEmpty()) {
                int prefixIndex = cloudPath.indexOf("/");
                if (prefixIndex > -1) {
                    this.bucket = cloudPath.substring(0, prefixIndex);
                    this.prefix = cloudPath.substring(prefixIndex + 1);
                } else {
                    this.bucket = cloudPath;
                }
            }
        } else {
            this.storageProvider = StorageProvider.local;
        }
    }
}
