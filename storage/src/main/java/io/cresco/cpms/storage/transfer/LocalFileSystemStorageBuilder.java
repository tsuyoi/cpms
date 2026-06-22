package io.cresco.cpms.storage.transfer;

import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;

public class LocalFileSystemStorageBuilder {
    private CPMSLogger logger;

    public LocalFileSystemStorageBuilder() {
        this.logger = new BasicCPMSLoggerBuilder().withClass(LocalFileSystemStorageBuilder.class).build();
    }

    public LocalFileSystemStorageBuilder withLogger(CPMSLogger logger) {
        setLogger(logger);
        return this;
    }

    public LocalFileSystemStorage build() {
        LocalFileSystemStorage fileSystemStorage = new LocalFileSystemStorage(this);
        validateFileSystemStorageObject(fileSystemStorage);
        return fileSystemStorage;
    }

    public void validateFileSystemStorageObject(LocalFileSystemStorage fileSystemStorage) {
        //Todo: Add some validation here
    }

    public CPMSLogger getLogger() {
        return logger;
    }

    public void setLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(LocalFileSystemStorageBuilder.class);
    }
}
