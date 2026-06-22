package io.cresco.cpms.storage.utilities;

import io.cresco.cpms.storage.transfer.TransferPath;

import java.nio.file.Path;
import java.nio.file.Paths;

public class StorageParameters {
    public static final String AWS_PREFIX = "s3://";
    public static final String GCP_PREFIX = "gs://";
    public static final String AZURE_PREFIX = "azb://";
    public static final String REMOTE_PREFIX = "remote://";
    public static final String CLOUD_PATH_SEPARATOR = "/";
    public static final String REMOTE_HOST_SEPARATOR = ":";

    private final StorageProvider storageProvider;
    private String container;
    private String prefix;
    private final String pathStr;
    private Path path;

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
        } else if (path.contains(REMOTE_HOST_SEPARATOR)) {
            this.storageProvider = StorageProvider.remote;
            String remotePath = path.replace(REMOTE_PREFIX, "");
            parseRemotePath(remotePath);
        } else {
            this.storageProvider = StorageProvider.local;
            this.prefix = path;
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

    private void parseRemotePath(String remotePath) {
        if (!remotePath.isEmpty()) {
            int hostIndex = remotePath.indexOf(REMOTE_HOST_SEPARATOR);
            if (hostIndex > -1) {
                this.container = remotePath.substring(0, hostIndex);
                this.prefix = remotePath.substring(hostIndex + 1);
                this.path = Paths.get(this.prefix);
            } else {
                this.container = remotePath;
            }
        }
    }

    public StorageProvider getStorageProvider() {
        return storageProvider;
    }

    public String getContainer() {
        return container;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getPathStr() {
        return pathStr;
    }

    public Path getPath() {
        return path;
    }

    public TransferPath getTransferPath() {
        if (storageProvider == StorageProvider.AWS || storageProvider == StorageProvider.GCS ||  storageProvider == StorageProvider.Azure || storageProvider == StorageProvider.remote) {
            return new TransferPath(container, prefix);
        } else {
            return new TransferPath(path.toString());
        }
    }
}
