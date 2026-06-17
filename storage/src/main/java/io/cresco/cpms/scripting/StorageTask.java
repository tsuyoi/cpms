package io.cresco.cpms.scripting;

import io.cresco.cpms.statics.ArchiveCompression;
import io.cresco.cpms.statics.BagItHashingAlgorithm;
import io.cresco.cpms.statics.BagItType;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class StorageTask implements ScriptedTask {
    private final String id;
    private final String name;
    private final String type;
    private final String action;
    private final String sourcePath;
    private final String destinationPath;
    private BagItType destinationArchiving;
    private BagItHashingAlgorithm destinationHashing;
    private final boolean destinationHiddenFiles;
    private ArchiveCompression destinationCompression;
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
        if ((getAction().equals("upload") || getAction().equals("download")) && StringUtils.isBlank(storageTaskScript.destinationPath))
            throw new ScriptException(
                    String.format("Storage task [%s] is missing required parameter [destinationPath]", getName())
            );
        this.destinationPath = storageTaskScript.destinationPath;
        if (storageTaskScript.destinationArchiving != null)
            this.destinationArchiving = BagItType.valueOf(storageTaskScript.destinationArchiving);
        if (storageTaskScript.destinationHashing != null)
            this.destinationHashing = BagItHashingAlgorithm.valueOf(storageTaskScript.destinationHashing);
        this.destinationHiddenFiles = storageTaskScript.destinationHiddenFiles;
        if (storageTaskScript.destinationCompression != null)
            this.destinationCompression = ArchiveCompression.valueOf(storageTaskScript.destinationCompression);
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

    public String getDestinationPath() {
        return destinationPath;
    }

    public BagItType getDestinationArchiving() {
        return destinationArchiving;
    }

    public BagItHashingAlgorithm getDestinationHashing() {
        return destinationHashing;
    }

    public boolean getDestinationHiddenFiles() {
        return destinationHiddenFiles;
    }

    public ArchiveCompression getDestinationCompression() {
        return destinationCompression;
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
                        \n- Storage Task (ID: %s, Name: %s)
                        \tAction: %s
                        \tSource Path: %s
                        \tDestination Path: %s
                        \tDestination Archiving: %s
                        \tDestination Hashing: %s
                        \tDestination Hidden Files: %b
                        \tDestination Compression: %s""",
                getId(), getName(),
                getAction(),
                getSourcePath(),
                getDestinationPath(),
                getDestinationArchiving(),
                getDestinationHashing(),
                getDestinationHiddenFiles(),
                getDestinationCompression()
        );
    }
}
