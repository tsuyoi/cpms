package io.cresco.cpms.scripting;

import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class StorageTask implements ScriptedTask {
    private final String id;
    private final String name;
    private final String type;
    private final String action;
    private final String s3AccessKey;
    private final String s3SecretKey;
    private final String s3Endpoint;
    private final String s3Region;
    private final String s3Bucket;
    private final String s3Path;
    private final Path localPath;
    private final String storageTaskJSON;

    public StorageTask(String storageTaskJSON) throws ScriptException {
        this.storageTaskJSON = storageTaskJSON;
        StorageTaskScript storageTaskScript = StorageTaskScript.getInstance(storageTaskJSON);
        if (StringUtils.isBlank(storageTaskScript.id))
            throw new ScriptException(
                    "Storage task is missing required parameter [id]"
            );
        this.id = storageTaskScript.id;
        if (StringUtils.isBlank(storageTaskScript.name))
            throw new ScriptException(
                    "Storage task is missing required parameter [name]"
            );
        this.name = storageTaskScript.name;
        if (StringUtils.isBlank(storageTaskScript.type))
            throw new ScriptException(
                    String.format("Storage task [%s] is missing required parameter [type]", getName())
            );
        this.type = storageTaskScript.type;
        if (StringUtils.isBlank(storageTaskScript.action))
            throw new ScriptException(
                    String.format("Storage task [%s] is missing required parameter [action]", getName())
            );
        this.action = storageTaskScript.action;
        if (StringUtils.isBlank(storageTaskScript.s3Bucket))
            throw new ScriptException(
                    String.format("Storage task [%s] is missing required parameter [bucket]", getName())
            );
        this.s3Bucket = storageTaskScript.s3Bucket;
        if (StringUtils.isBlank(storageTaskScript.s3Path))
            throw new ScriptException(
                    String.format("Storage task [%s] is missing required parameter [s3Path]", getName())
            );
        this.s3Path = storageTaskScript.s3Path;
        if ((getAction().equals("upload") || getAction().equals("download")) && StringUtils.isBlank(storageTaskScript.localPath))
            throw new ScriptException(
                    String.format("Storage task [%s] is missing required parameter [localPath]", getName())
            );
        this.localPath = Paths.get(storageTaskScript.localPath);
        this.s3AccessKey = storageTaskScript.s3AccessKey;
        this.s3SecretKey = storageTaskScript.s3SecretKey;
        this.s3Endpoint = storageTaskScript.s3Endpoint;
        this.s3Region = storageTaskScript.s3Region;
    }

    public String getId() { return id; }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getAction() {
        return action;
    }

    public String getS3AccessKey() {
        return s3AccessKey;
    }

    public String getS3SecretKey() {
        return s3SecretKey;
    }

    public String getS3Endpoint() {
        return s3Endpoint;
    }

    public String getS3Region() {
        return s3Region;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public String getS3Path() {
        return s3Path;
    }

    public Path getLocalPath() {
        return localPath;
    }

    public String getStorageTaskJSON() {
        return storageTaskJSON;
    }

    public String toJson() {
        return getStorageTaskJSON();
    }

    @Override
    public String toString() {
        return String.format("- Storage Task (ID: %s, Name: %s)\n" +
                        "\tAction: %s\n" +
                        "\tS3 Access Key: %s\n" +
                        "\tS3 Secret Key: %s\n" +
                        "\tS3 Endpoint: %s\n" +
                        "\tS3 Region: %s\n" +
                        "\tS3 Bucket: %s\n" +
                        "\tS3 Path: %s\n" +
                        "\tLocal Path: %s",
                getId(), getName(),
                getAction(),
                getS3AccessKey(),
                getS3SecretKey(),
                getS3Endpoint(),
                getS3Region(),
                getS3Bucket(),
                getS3Path(),
                getLocalPath()
        );
    }
}
