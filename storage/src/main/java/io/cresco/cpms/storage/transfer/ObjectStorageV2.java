package io.cresco.cpms.storage.transfer;

import io.cresco.cpms.logging.CPMSLogger;
import io.cresco.cpms.statics.CPMSStatics;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.*;
import software.amazon.awssdk.transfer.s3.progress.TransferListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue", "BooleanMethodIsAlwaysInverted"})
public class ObjectStorageV2 {
    private final String accessKey;
    private final String secretKey;
    private final Region region;

    private final int partSize;
    private final long minimumUploadPartSize;
    private final long multipartUploadThreshold;

    private CPMSLogger logger;

    /**
     * Object Storage Constructer utilizing the Builder paradigm
     * @param builder - Builder object
     */
    public ObjectStorageV2(ObjectStorageBuilder builder) {
        this.accessKey = builder.getAccessKey();
        this.secretKey = builder.getSecretKey();
        this.region = builder.getRegion();
        this.partSize = builder.getPartSize();
        this.minimumUploadPartSize = builder.getMinimumUploadPartSize();
        this.multipartUploadThreshold = builder.getMultipartUploadThreshold();
        setLogger(builder.getLogger());
    }

    private S3Client getClient() {
        if (accessKey == null || accessKey.isEmpty() || secretKey == null || secretKey.isEmpty())
            return getClientWithDefaultCredentials();
        return getClientWithCredentials();
    }

    private S3Client getClientWithDefaultCredentials() {
        return S3Client.builder()
                .build();
    }

    private S3Client getClientWithCredentials() {
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );
        if (region == null)
            return S3Client.builder()
                    .credentialsProvider(credentialsProvider)
                    .build();
        return S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    private S3AsyncClient getAsyncClient() {
        return S3AsyncClient.crtBuilder()
                .minimumPartSizeInBytes(minimumUploadPartSize)
                .thresholdInBytes(multipartUploadThreshold)
                .build();
    }

    private S3AsyncClient getAsyncClientWithCredentials() {
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );
        return S3AsyncClient.crtBuilder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .minimumPartSizeInBytes(minimumUploadPartSize)
                .thresholdInBytes(multipartUploadThreshold)
                .build();
    }

    public List<Bucket> listBuckets() {
        logger.debug("listBuckets()");
        try (S3Client s3Client = getClient()) {
            ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder()
                    .build();
            ListBucketsResponse listBucketsResponse = s3Client.listBuckets(listBucketsRequest);
            return new ArrayList<>(listBucketsResponse.buckets());
        } catch (SdkException e) {
            logger.error("listBuckets Error: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean doesBucketExist(String bucket) {
        logger.debug("doesBucketExist({})", bucket);
        try (S3Client s3Client = getClient()) {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucket)
                    .build();
            HeadBucketResponse headBucketResponse = s3Client.headBucket(headBucketRequest);
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        } catch (SdkException e) {
            logger.error("doesBucketExist Error: {}", e.getMessage());
            return false;
        }
    }

    public void createBucket(String bucket) {
        logger.debug("createBucket({})", bucket);
        try (S3Client s3Client = getClient()) {
            S3Waiter s3Waiter = s3Client.waiter();
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket(bucket)
                    .build();
            s3Client.createBucket(createBucketRequest);
            HeadBucketRequest waitForCreateBucket = HeadBucketRequest.builder()
                    .bucket(bucket)
                    .build();
            WaiterResponse<HeadBucketResponse> createWaiterResponse = s3Waiter.waitUntilBucketExists(waitForCreateBucket);
            createWaiterResponse.matched();
            logger.info("Created bucket: {}", bucket);
        } catch (SdkException e) {
            logger.error("createBucket Error: {}", e.getMessage());
        }
    }

    public HeadObjectResponse headObject(String bucket, String key) {
        logger.debug("headObject({}, {})", bucket, key);
        try (S3Client s3Client = getClient()) {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            return s3Client.headObject(headObjectRequest);
        } catch (NoSuchKeyException e) {
            return null;
        } catch (SdkException e) {
            logger.error("headObject Error: {}", e.getMessage());
            return null;
        }
    }

    public List<S3Object> listBucketObjects(String bucket) {
        logger.debug("listBucketObjects({})", bucket);
        try (S3Client s3Client = getClient()) {
            ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
                    .bucket(bucket)
                    .build();
            ListObjectsResponse listObjectsResponse = s3Client.listObjects(listObjectsRequest);
            return listObjectsResponse.contents();
        }
    }

    public List<S3Object> listBucketObjects(String bucket, String prefix) {
        logger.debug("listBucketObjects({})", bucket);
        try (S3Client s3Client = getClient()) {
            ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .build();
            ListObjectsResponse listObjectsResponse = s3Client.listObjects(listObjectsRequest);
            return listObjectsResponse.contents();
        }
    }

    public boolean deleteBucketObject(String bucket, String key) {
        logger.debug("deleteBucketObject({}, {})", bucket, key);
        try (S3Client s3Client = getClient()) {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            DeleteObjectResponse deleteObjectResponse = s3Client.deleteObject(deleteObjectRequest);
            return true;
        } catch (SdkException e) {
            logger.error("deleteBucketObject Error: {}", e.getMessage());
            return false;
        }
    }

    public boolean deleteBucketContents(String bucket, String prefix) {
        logger.debug("deleteBucketContents({}, {})", bucket, prefix);
        try (S3Client s3Client = getClient()) {
            List<ObjectIdentifier> toDelete = listBucketObjects(bucket, prefix).stream()
                    .map(S3Object::key)
                    .map(key -> ObjectIdentifier.builder().key(key).build())
                    .collect(Collectors.toList());
            if (toDelete.isEmpty())
                return true;
            DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                    .bucket(bucket)
                    .delete(Delete.builder().objects(toDelete).build())
                    .build();
            DeleteObjectsResponse deleteObjectsResponse = s3Client.deleteObjects(deleteObjectsRequest);
            return true;
        } catch (SdkException e) {
            logger.error("deleteBucketContents Error: {}", e.getMessage());
            return false;
        }
    }

    public List<String> listBucketDirectoriesAsString(String bucket) {
        return listBucketDirectories(bucket).stream().map(CommonPrefix::prefix).collect(Collectors.toList());
    }

    public List<CommonPrefix> listBucketDirectories(String bucket) {
        logger.debug("listBucketDirectories({})", bucket);
        try (S3Client s3Client = getClient()) {
            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .delimiter("/")
                    .build();
            ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);
            return listObjectsResponse.commonPrefixes();
        } catch (SdkException e) {
            logger.error("listBucketDirectories Error: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean uploadFile(Path uploadPath, String bucket, String key) throws IOException {
        logger.debug("uploadFile({}, {}, {})", uploadPath, bucket, key);
        if (!Files.exists(uploadPath))
            throw new IOException("file to upload does not exist");
        if (!doesBucketExist(bucket))
            throw new IOException("target bucket does not exist");
        try (S3AsyncClient s3Client = getAsyncClient();
             S3TransferManager s3TransferManager = S3TransferManager.builder().s3Client(s3Client).build()) {
            Map<String, String> metadata = new HashMap<>();
            metadata.put(CPMSStatics.UNCOMPRESSED_SIZE_METADATA_TAG_KEY, String.valueOf(Files.size(uploadPath)));
            metadata.put(CPMSStatics.PART_SIZE_METADATA_TAG_KEY, String.valueOf(partSize));
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .metadata(metadata)
                    .build();
            UploadFileRequest uploadFileRequest = UploadFileRequest.builder()
                    .putObjectRequest(putObjectRequest)
                    .source(uploadPath)
                    .addTransferListener(new CrescoLoggingTransferListener(logger))
                    .build();
            FileUpload fileUpload = s3TransferManager.uploadFile(uploadFileRequest);
            CompletedFileUpload uploadResult = fileUpload.completionFuture().join();
            logger.cpmsInfo("Verifying upload [{}/{}] via checksums", bucket, key);
            String localChecksum;
            String s3Checksum = uploadResult.response().eTag().replace("\"", "");
            logger.trace("s3Checksum: {}", s3Checksum);
            MD5Tools md5Tools = new MD5Tools(partSize * 1024 * 1024);
            if (s3Checksum.contains("-"))
                localChecksum = md5Tools.getMultiCheckSum(uploadPath.toString());
            else
                localChecksum = md5Tools.getCheckSum(uploadPath.toString());
            logger.trace("localChecksum: {}", localChecksum);
            if (!localChecksum.equals(s3Checksum))
                logger.cpmsError("Checksums do not match [local: {}, S3: {}]", localChecksum, s3Checksum);
            return localChecksum.equals(s3Checksum);
        } catch (SdkException e) {
            logger.error("uploadFile Error: {}", e.getMessage());
            return false;
        }
    }

    public boolean downloadObject(String bucket, String key, Path destinationDirectory) {
        logger.debug("downloadObject({}, {}, {})", bucket, key, destinationDirectory);
        if (!doesBucketExist(bucket)) {
            logger.cpmsError("Bucket [{}] does not exist", bucket);
            return false;
        }
        HeadObjectResponse s3Object = headObject(bucket, key);
        if (s3Object == null) {
            logger.cpmsError("Bucket [{}] does not contain [{}]", bucket, key);
            return false;
        }
        try (S3AsyncClient s3Client = getAsyncClient();
             S3TransferManager s3TransferManager = S3TransferManager.builder().s3Client(s3Client).build()) {
            long s3ObjectSize = s3Object.contentLength();
            String s3Checksum = s3Object.eTag().replace("\"", "");
            int s3PartSize = partSize;
            try {
                if (s3Object.hasMetadata() && s3Object.metadata().containsKey(CPMSStatics.PART_SIZE_METADATA_TAG_KEY))
                    s3PartSize = Integer.parseInt(s3Object.metadata().get(CPMSStatics.PART_SIZE_METADATA_TAG_KEY));
            } catch (NumberFormatException e) {
                logger.cpmsError("Invalid multipart upload partSize metadata tag for [{}/{}]", bucket, key);
            }
            logger.trace("downloadDir.getAbsolutePath(): {}", destinationDirectory.toAbsolutePath());
            if (!Files.exists(destinationDirectory)) {
                try {
                    if (Files.createDirectories(destinationDirectory) == null) {
                        logger.cpmsError("Output directory [{}] does not exist and could not be created",
                                destinationDirectory.toAbsolutePath());
                        return false;
                    }
                } catch (IOException e) {
                    logger.cpmsError("Failed to create output directory [{}]",
                            destinationDirectory.toAbsolutePath());
                    return false;
                }
            }
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            int prefixLength = key.lastIndexOf("/") + 1;
            Path outFile = destinationDirectory.resolve(key.substring(prefixLength));
            DownloadFileRequest downloadFileRequest = DownloadFileRequest.builder()
                    .getObjectRequest(getObjectRequest)
                    .destination(destinationDirectory.resolve(outFile))
                    .addTransferListener(new CrescoLoggingTransferListener(logger))
                    .build();
            FileDownload fileDownload = s3TransferManager.downloadFile(downloadFileRequest);
            CompletedFileDownload downloadResult = fileDownload.completionFuture().join();
            logger.cpmsInfo("Verifying download [{}] via checksums", outFile);
            logger.trace("s3Checksum: {}", s3Checksum);
            String localChecksum;
            MD5Tools md5Tools = new MD5Tools(s3PartSize * 1024 * 1024);
            if (s3Checksum.contains("-"))
                localChecksum = md5Tools.getMultiCheckSum(outFile.toAbsolutePath().toString());
            else
                localChecksum = md5Tools.getCheckSum(outFile.toAbsolutePath().toString());
            logger.trace("localChecksum: {}", localChecksum);
            if (!localChecksum.equals(s3Checksum))
                logger.cpmsError("Checksums do not match [local: {}, S3: {}]", localChecksum, s3Checksum);
            return localChecksum.equals(s3Checksum);
        } catch (SdkException e) {
            logger.error("downloadObject SDK Error: {}", e.getMessage());
            return false;
        } catch (IOException e) {
            logger.cpmsError("downloadObject IO Error: {}", e.getMessage());
            return false;
        }
    }

    public CPMSLogger getLogger() {
        return logger;
    }

    public void setLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(ObjectStorageV2.class);
    }

    public void updateLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(ObjectStorageV2.class);
    }

    private static class CrescoLoggingTransferListener implements TransferListener {
        private final CPMSLogger logger;
        private final int updatePercentStep = 5;
        private Long totalBytes;
        private long lastTimestamp;
        private long lastTransferred = 0L;
        private int nextUpdate = updatePercentStep;

        public CrescoLoggingTransferListener(CPMSLogger logger) {
            this.lastTimestamp = System.currentTimeMillis();
            this.logger = logger.cloneLogger(CrescoLoggingTransferListener.class);
        }

        @Override
        public void bytesTransferred(TransferListener.Context.BytesTransferred context) {
            if (totalBytes == null && context.progressSnapshot().totalBytes().isPresent())
                totalBytes = context.progressSnapshot().totalBytes().getAsLong();
            long currentBytesTransferred = context.progressSnapshot().transferredBytes();
            float currentTransferPercentage = ((float)currentBytesTransferred / (float)totalBytes) * (float)100;
            if (currentTransferPercentage > (float)nextUpdate - 0.01) {
                long transferredSinceLastUpdate = currentBytesTransferred - lastTransferred;
                long currentTimestamp = System.currentTimeMillis();
                logger.cpmsInfo("Transferring ({}/{} {}%) at {}",
                        humanReadableByteCount(currentBytesTransferred, true),
                        humanReadableByteCount(totalBytes, true),
                        (int)(currentTransferPercentage + 0.01),
                        humanReadableTransferRate(transferredSinceLastUpdate, currentTimestamp - lastTimestamp));
                lastTransferred = currentBytesTransferred;
                lastTimestamp = currentTimestamp;
                nextUpdate += updatePercentStep;
                if (currentTransferPercentage >= (float)(100.0 - 0.01))
                    logger.cpmsInfo("Completing/closing parallel transfers");
            }
        }

        @SuppressWarnings("SameParameterValue")
        private static String humanReadableByteCount(long bytes, boolean si) {
            int unit = si ? 1000 : 1024;
            if (bytes < unit) return bytes + " B";
            int exp = (int) (Math.log(bytes) / Math.log(unit));
            String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
            return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
        }

        private static String humanReadableTransferRate(long transferred, long duration) {
            float rate = (((float)transferred * (float)1000) * (float)8) / duration;
            int unit = 1000;
            if ((int)rate < unit) return String.format("%.1f b/s", rate);
            int exp = (int) (Math.log(rate) / Math.log(unit));
            String pre = (exp >= 0 && exp < 7) ? "kMGTPE".charAt(exp - 1) + "" : "";
            return String.format("%.1f %sb/s", rate / Math.pow(unit, exp), pre);
        }

        private static String formatDuration(Duration duration) {
            long seconds = duration.getSeconds();
            long absSeconds = Math.abs(seconds);
            String positive = String.format(
                    "%d:%02d:%02d",
                    absSeconds / 3600,
                    (absSeconds % 3600) / 60,
                    absSeconds % 60);
            return seconds < 0 ? "-" + positive : positive;
        }
    }
}
