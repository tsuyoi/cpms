package io.cresco.cpms.storage.utilities;

import java.nio.file.Path;
import java.nio.file.Paths;

public class StorageParameters {
    private static final String AWS_PREFIX = "s3://";
    private static final String GCP_PREFIX = "gs://";
    private static final String AZURE_PREFIX = "azb://";
    private static final String CLOUD_PATH_SEPARATOR = "/";

    public StorageProvider storageProvider;
    public String container;
    public String prefix;
    public String pathStr;
    public Path path;

    public StorageParameters(String path) {
        this.pathStr = path;
        if (path.startsWith(AWS_PREFIX)) {
            this.storageProvider = StorageProvider.AWS;
            String cloudPath = path.replace(AWS_PREFIX, "");
            parseCloudPath(cloudPath);
        } else if (path.startsWith(GCP_PREFIX)) {
            this.storageProvider = StorageProvider.GCS;
            String cloudPath = path.replace(GCP_PREFIX, "");
            parseCloudPath(cloudPath);
        } else if (path.startsWith(AZURE_PREFIX)) {
            this.storageProvider = StorageProvider.Azure;
            String cloudPath = path.replace(AZURE_PREFIX, "");
            parseCloudPath(cloudPath);
        } else {
            this.storageProvider = StorageProvider.local;
            this.path = Paths.get(path);
        }
    }

    private void parseCloudPath(String cloudPath) {
        if (!cloudPath.isEmpty()) {
            int prefixIndex = cloudPath.indexOf(CLOUD_PATH_SEPARATOR);
            if (prefixIndex > -1) {
                this.container = cloudPath.substring(0, prefixIndex);
                this.prefix = cloudPath.substring(prefixIndex + 1);
            } else {
                this.container = cloudPath;
            }
        }
    }
}
