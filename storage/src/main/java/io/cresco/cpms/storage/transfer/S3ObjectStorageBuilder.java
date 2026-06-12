package io.cresco.cpms.storage.transfer;

import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;
import io.cresco.cpms.statics.CPMSStatics;
import software.amazon.awssdk.regions.Region;

@SuppressWarnings({"unused"})
public class S3ObjectStorageBuilder {
    private static final long BYTES_ORDER_OF_MAGNITUDE = 1024L;
    private String accessKey;
    private String secretKey;
    private Region region;

    private int partSize;
    private long minimumUploadPartSize;
    private long multipartUploadThreshold;

    private CPMSLogger logger;

    public S3ObjectStorageBuilder() {
        this.logger = new BasicCPMSLoggerBuilder().withClass(S3ObjectStorageBuilder.class).build();
        this.partSize = CPMSStatics.DEFAULT_PART_SIZE;
        this.minimumUploadPartSize = partSize * BYTES_ORDER_OF_MAGNITUDE * BYTES_ORDER_OF_MAGNITUDE;
        this.multipartUploadThreshold = minimumUploadPartSize;
    }

    public S3ObjectStorageBuilder withStaticCredentials(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        return this;
    }

    public S3ObjectStorageBuilder withRegion(Region region) {
        this.region = region;
        return this;
    }

    public S3ObjectStorageBuilder withRegion(String regionName) {
        this.region = Region.of(regionName);
        return this;
    }

    public S3ObjectStorageBuilder withPartSize(int partSize) {
        this.partSize = partSize;
        this.minimumUploadPartSize = partSize * BYTES_ORDER_OF_MAGNITUDE * BYTES_ORDER_OF_MAGNITUDE;
        this.multipartUploadThreshold = minimumUploadPartSize;
        return this;
    }

    public S3ObjectStorageBuilder withLogger(CPMSLogger logger) {
        setLogger(logger);
        return this;
    }

    public S3ObjectStorage build() {
        logger.trace("Building S3ObjectStorage");
        S3ObjectStorage s3ObjectStorage = new S3ObjectStorage(this);
        validateObjectStorageObject(s3ObjectStorage);
        return s3ObjectStorage;
    }

    public void validateObjectStorageObject(S3ObjectStorage objectStorage) {
        //Todo: Add some validation here
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public Region getRegion() {
        return region;
    }

    public int getPartSize() {
        return partSize;
    }

    public long getMinimumUploadPartSize() {
        return minimumUploadPartSize;
    }

    public long getMultipartUploadThreshold() {
        return multipartUploadThreshold;
    }

    public CPMSLogger getLogger() {
        return logger;
    }

    public void setLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(S3ObjectStorageBuilder.class);
    }
}
