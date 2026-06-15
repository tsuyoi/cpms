package io.cresco.cpms.scripting;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class StorageTask implements ScriptedTask {
    private final String id;
    private final String name;
    private final String type;
    private final String action;
    private final String sourcePath;
    private final String sourceCompression;
    private final String sourceArchiving;
    private final String destinationPath;
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
        if ((getAction().equals("upload") || getAction().equals("download")) && StringUtils.isBlank(storageTaskScript.sourcePath))
            throw new ScriptException(
                    String.format("Storage task [%s] is missing required parameter [sourcePath]", getName())
            );
        this.sourcePath = storageTaskScript.sourcePath;
        if (getAction().equals("upload") && StringUtils.isBlank(storageTaskScript.sourceCompression))
            throw new ScriptException(
                    String.format("Storage upload task [%s] is missing required parameter [sourceCompression]", getName())
            );
        this.sourceCompression = storageTaskScript.sourceCompression;
        if (getAction().equals("upload") && StringUtils.isBlank(storageTaskScript.sourceArchiving))
            throw new ScriptException(
                    String.format("Storage upload task [%s] is missing required parameter [sourceArchiving]", getName())
            );
        this.sourceArchiving = storageTaskScript.sourceArchiving;
        if ((getAction().equals("upload") || getAction().equals("download")) && StringUtils.isBlank(storageTaskScript.destinationPath))
            throw new ScriptException(
                    String.format("Storage task [%s] is missing required parameter [destinationPath]", getName())
            );
        this.destinationPath = storageTaskScript.destinationPath;
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

    public String getSourcePath() {
        return sourcePath;
    }

    public String getSourceCompression() {
        return sourceCompression;
    }

    public String getSourceArchiving() {
        return sourceArchiving;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public String getStorageTaskJSON() {
        return storageTaskJSON;
    }

    public String toJson() {
        return getStorageTaskJSON();
    }

    @Override
    public String toString() {
        return String.format("""
                        - Storage Task (ID: %s, Name: %s)
                        \tAction: %s
                        \tSource Path: %s
                        \tSource Compression: %s
                        \tSource Archiving: %s
                        \tDestination Path: %s""",
                getId(), getName(),
                getAction(),
                getSourcePath(),
                getSourceCompression(),
                getSourceArchiving(),
                getDestinationPath()
        );
    }
}
