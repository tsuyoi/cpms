package io.cresco.cpms.storage.transfer;

import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;

public class FileSystemStorageBuilder {
    private CPMSLogger logger;

    public FileSystemStorageBuilder() {
        this.logger = new BasicCPMSLoggerBuilder().withClass(FileSystemStorageBuilder.class).build();
    }

    public FileSystemStorageBuilder withLogger(CPMSLogger logger) {
        setLogger(logger);
        return this;
    }

    public FileSystemStorage build() {
        FileSystemStorage fileSystemStorage = new FileSystemStorage(this);
        validateFileSystemStorageObject(fileSystemStorage);
        return fileSystemStorage;
    }

    public void validateFileSystemStorageObject(FileSystemStorage fileSystemStorage) {
        //Todo: Add some validation here
    }

    public CPMSLogger getLogger() {
        return logger;
    }

    public void setLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(FileSystemStorageBuilder.class);
    }
}
