package io.cresco.cpms.storage.transfer;

import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;

public class RemoteFileSystemStorageBuilder {
    private CPMSLogger logger;

    public RemoteFileSystemStorageBuilder() {
        this.logger = new BasicCPMSLoggerBuilder().withClass(RemoteFileSystemStorageBuilder.class).build();
    }

    public RemoteFileSystemStorageBuilder withLogger(CPMSLogger logger) {
        setLogger(logger);
        return this;
    }

    public RemoteFileSystemStorage build() {
        RemoteFileSystemStorage fileSystemStorage = new RemoteFileSystemStorage(this);
        validateFileSystemStorageObject(fileSystemStorage);
        return fileSystemStorage;
    }

    public void validateFileSystemStorageObject(RemoteFileSystemStorage fileSystemStorage) {
        //Todo: Add some validation here
    }

    public CPMSLogger getLogger() {
        return logger;
    }

    public void setLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(RemoteFileSystemStorageBuilder.class);
    }
}
