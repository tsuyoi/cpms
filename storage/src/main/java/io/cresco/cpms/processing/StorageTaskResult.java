package io.cresco.cpms.processing;

public class StorageTaskResult {
    private boolean success;
    private String errorMessage;
    private String sourcePath;
    private String destinationPath;

    public StorageTaskResult(StorageTaskResultBuilder builder) {
        this.success = builder.getSuccess();
        this.errorMessage = builder.getErrorMessage();
        this.sourcePath = builder.getSourcePath();
        this.destinationPath = builder.getDestinationPath();
    }

    public boolean getSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getDestinationPath() {
        return destinationPath;
    }
}
