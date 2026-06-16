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
        this.bagitHashingAlgorithm = CPMSStatics.DEFAULT_BAGIT_HASHING;
        this.bagitHiddenFiles = CPMSStatics.DEFAULT_HIDDEN_FILES;
    }

    public ArchiverBuilder withBagItType(BagItType bagitType) {
        if (bagitType != null)
            this.bagitType = bagitType;
        return this;
    }

    public ArchiverBuilder withBagItHashingAlgorithm(BagItHashingAlgorithm bagItHashingAlgorithm) {
        if (bagItHashingAlgorithm != null)
            this.bagitHashingAlgorithm = bagItHashingAlgorithm;
        return this;
    }

    public ArchiverBuilder withBagItHiddenfiles(boolean bagItHiddenfiles) {
        if (bagItHiddenfiles)
            this.bagitHiddenFiles = bagItHiddenfiles;
        return this;
    }

    public ArchiverBuilder withArchiveCompression(ArchiveCompression archiveCompression) {
        if (archiveCompression != null)
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
