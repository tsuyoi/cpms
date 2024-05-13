package io.cresco.cpms.storage.utilities;

public class StorageParameters {
    private static final String AWS_PREFIX = "s3://";
    private static final String GCP_PREFIX = "gs://";
    private static final String AZURE_PREFIX = "azureblob://";
    private static final String CLOUD_PATH_SEPARATOR = "/";

    public StorageProvider storageProvider;
    public String bucket;
    public String prefix;
    public String path;

    public StorageParameters(String path) {
        this.path = path;
        if (path.startsWith(AWS_PREFIX)) {
            this.storageProvider = StorageProvider.AWS;
            String cloudPath = path.replace(AWS_PREFIX, "");
            parseCloudPath(cloudPath);
            /*if (!cloudPath.isEmpty()) {
                int prefixIndex = cloudPath.indexOf(CLOUD_PATH_SEPARATOR);
                if (prefixIndex > -1) {
                    this.bucket = cloudPath.substring(0, prefixIndex);
                    this.prefix = cloudPath.substring(prefixIndex + 1);
                } else {
                    this.bucket = cloudPath;
                }
            }*/
        } else if (path.startsWith(GCP_PREFIX)) {
            this.storageProvider = StorageProvider.GCS;
            String cloudPath = path.replace(GCP_PREFIX, "");
            parseCloudPath(cloudPath);
            /*if (!cloudPath.isEmpty()) {
                int prefixIndex = cloudPath.indexOf(CLOUD_PATH_SEPARATOR);
                if (prefixIndex > -1) {
                    this.bucket = cloudPath.substring(0, prefixIndex);
                    this.prefix = cloudPath.substring(prefixIndex + 1);
                } else {
                    this.bucket = cloudPath;
                }
            }*/
        } else if (path.startsWith(AZURE_PREFIX)) {
            this.storageProvider = StorageProvider.Azure;
            String cloudPath = path.replace(AZURE_PREFIX, "");
            parseCloudPath(cloudPath);
            /*if (!cloudPath.isEmpty()) {
                int prefixIndex = cloudPath.indexOf(CLOUD_PATH_SEPARATOR);
                if (prefixIndex > -1) {
                    this.bucket = cloudPath.substring(0, prefixIndex);
                    this.prefix = cloudPath.substring(prefixIndex + 1);
                } else {
                    this.bucket = cloudPath;
                }
            }*/
        } else {
            this.storageProvider = StorageProvider.local;
        }
    }

    private void parseCloudPath(String cloudPath) {
        if (!cloudPath.isEmpty()) {
            int prefixIndex = cloudPath.indexOf(CLOUD_PATH_SEPARATOR);
            if (prefixIndex > -1) {
                this.bucket = cloudPath.substring(0, prefixIndex);
                this.prefix = cloudPath.substring(prefixIndex + 1);
            } else {
                this.bucket = cloudPath;
            }
        }
    }
}
