package io.cresco.cpms.storage.encapsulation;

import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;
import io.cresco.cpms.statics.ArchiveCompression;
import io.cresco.cpms.statics.BagItHashingAlgorithm;
import io.cresco.cpms.statics.BagItType;
import io.cresco.cpms.statics.CPMSStatics;

public class ArchiverBuilder {
    private CPMSLogger logger;
    private BagItType bagitType;
    private BagItHashingAlgorithm bagitHashingAlgorithm;
    private boolean bagitHiddenFiles;
    private ArchiveCompression archiveCompression;

    public ArchiverBuilder() {
        this.logger = new BasicCPMSLoggerBuilder().withClass(Archiver.class).build();
        this.bagitType = CPMSStatics.DEFAULT_BAGIT_TYPE;
        this.bagitHashingAlgorithm = CPMSStatics.DEFAULT_BAGIT_HASHING;
        this.bagitHiddenFiles = CPMSStatics.DEFAULT_HIDDEN_FILES;
        this.archiveCompression = CPMSStatics.DEFAULT_ARCHIVE_COMPRESSION;
    }

    public ArchiverBuilder withBagItType(BagItType bagitType) {
        this.bagitType = bagitType;
        return this;
    }

    public ArchiverBuilder withBagItHashingAlgorithm(BagItHashingAlgorithm bagItHashingAlgorithm) {
        this.bagitHashingAlgorithm = bagItHashingAlgorithm;
        return this;
    }

    public ArchiverBuilder withBagItHiddenfiles(boolean bagItHiddenfiles) {
        this.bagitHiddenFiles = bagItHiddenfiles;
        return this;
    }

    public ArchiverBuilder withArchiveCompression(ArchiveCompression archiveCompression) {
        this.archiveCompression = archiveCompression;
        return this;
    }

    public ArchiverBuilder withLogger(CPMSLogger logger) {
        this.logger = logger;
        return this;
    }

    public Archiver build() {
        Archiver archiver = new Archiver(this);
        validateArchiverObject(archiver);
        return archiver;
    }

    public void validateArchiverObject(Archiver archiver) {
        //Todo: Add some validation here
    }

    public CPMSLogger getLogger() {
        return logger;
    }

    public BagItType getBagitType() {
        return bagitType;
    }

    public BagItHashingAlgorithm getBagitHashingAlgorithm() {
        return bagitHashingAlgorithm;
    }

    public boolean isBagitHiddenFiles() {
        return bagitHiddenFiles;
    }

    public ArchiveCompression getArchiveCompression() {
        return archiveCompression;
    }
}
