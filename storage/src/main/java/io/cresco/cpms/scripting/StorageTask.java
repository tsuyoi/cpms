package io.cresco.cpms.scripting;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class StorageTask implements ScriptedTask {
    private final String id;
    private final String name;
    private final String type;
    private final String action;
    private final Path localPath;
    private final String remotePath;
    private final String storageTaskJSON;

    public StorageTask(Map<String, String> storageTaskMap) throws ScriptException {
        this(StorageTaskScript.getInstance(storageTaskMap));
    }

    public StorageTask(String storageTaskJSON) throws ScriptException {
        this(StorageTaskScript.getInstance(storageTaskJSON));
    }

    public StorageTask(StorageTaskScript storageTaskScript) throws ScriptException {
        this.storageTaskJSON = storageTaskScript.toString();
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
        if ((getAction().equals("upload") || getAction().equals("download")) && StringUtils.isBlank(storageTaskScript.localPath))
            throw new ScriptException(
                    String.format("Storage task [%s] is missing required parameter [localPath]", getName())
            );
        this.localPath = (storageTaskScript.localPath != null) ? Paths.get(storageTaskScript.localPath) : null;
        if ((getAction().equals("upload") || getAction().equals("download")) && StringUtils.isBlank(storageTaskScript.remotePath))
            throw new ScriptException(
                    String.format("Storage task [%s] is missing required parameter [remotePath]", getName())
            );
        this.remotePath = storageTaskScript.remotePath;
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

    public Path getLocalPath() {
        return localPath;
    }

    public String getRemotePath() {
        return remotePath;
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
                        "\tLocal Path: %s\n" +
                        "\tRemote Path: %s",
                getId(), getName(),
                getAction(),
                getLocalPath(),
                getRemotePath()
        );
    }
}
