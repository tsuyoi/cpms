package io.cresco.cpms.storage.transfer;

import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;
import io.cresco.cpms.statics.CPMSStatics;
import software.amazon.awssdk.regions.Region;

@SuppressWarnings({"unused"})
public class ObjectStorageBuilder {
    private static final long BYTES_ORDER_OF_MAGNITUDE = 1024L;
    private String accessKey;
    private String secretKey;
    private Region region;

    private int partSize;
    private long minimumUploadPartSize;
    private long multipartUploadThreshold;

    private CPMSLogger logger;

    public ObjectStorageBuilder() {
        this.logger = new BasicCPMSLoggerBuilder().withClass(ObjectStorageBuilder.class).build();
        this.partSize = CPMSStatics.DEFAULT_PART_SIZE;
        this.minimumUploadPartSize = partSize * BYTES_ORDER_OF_MAGNITUDE * BYTES_ORDER_OF_MAGNITUDE;
        this.multipartUploadThreshold = minimumUploadPartSize;
    }

    public ObjectStorageBuilder withStaticCredentials(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        return this;
    }

    public ObjectStorageBuilder withRegion(Region region) {
        this.region = region;
        return this;
    }

    public ObjectStorageBuilder withRegion(String regionName) {
        this.region = Region.of(regionName);
        return this;
    }

    public ObjectStorageBuilder withPartSize(int partSize) {
        this.partSize = partSize;
        this.minimumUploadPartSize = partSize * BYTES_ORDER_OF_MAGNITUDE * BYTES_ORDER_OF_MAGNITUDE;
        this.multipartUploadThreshold = minimumUploadPartSize;
        return this;
    }

    public ObjectStorageBuilder withLogger(CPMSLogger logger) {
        this.logger = logger;
        return this;
    }

    public ObjectStorageV2 build() {
        ObjectStorageV2 objectStorage = new ObjectStorageV2(this);
        validateObjectStorageObject(objectStorage);
        return objectStorage;
    }

    public void validateObjectStorageObject(ObjectStorageV2 objectStorage) {
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
}
