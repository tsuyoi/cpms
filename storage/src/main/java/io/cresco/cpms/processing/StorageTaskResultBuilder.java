package io.cresco.cpms.processing;

import io.cresco.cpms.storage.transfer.S3ObjectStorage;

public class StorageTaskResultBuilder {
    private boolean success;
    private String errorMessage;

    private String sourcePath;
    private String destinationPath;

    public StorageTaskResultBuilder() {
        this.success = false;
    }

    public StorageTaskResultBuilder withSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public StorageTaskResultBuilder withErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public StorageTaskResultBuilder withSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
        return this;
    }

    public StorageTaskResultBuilder withDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
        return this;
    }

    public StorageTaskResult build() {
        StorageTaskResult storageTaskResult = new StorageTaskResult(this);
        validateStorageTaskResultObject(storageTaskResult);
        return storageTaskResult;
    }

    public void validateStorageTaskResultObject(StorageTaskResult storageTaskResult) {
        // Todo: Add some validation
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
