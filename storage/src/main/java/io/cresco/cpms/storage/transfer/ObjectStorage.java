package io.cresco.cpms.storage.transfer;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.*;
import com.amazonaws.services.s3.transfer.model.UploadResult;
import io.cresco.cpms.logging.BasicCPMSLogger;
import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;
import io.cresco.cpms.statics.CPMSStatics;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue", "BooleanMethodIsAlwaysInverted"})
public class ObjectStorage {
    private static final int DEFAULT_BUFFER_SIZE = 16384;

    private final AmazonS3 conn;

    private final String accessKey;
    private final String secretKey;
    private final String endpoint;
    private final String region;

    private CPMSLogger logger;

    /**
     * Default logging (Logback) for external or local usage instead of Cresco
     * @param accessKey         Object storage access key
     * @param secretKey         Object storage secret key
     * @param endpoint          Object storage endpoint web address
     * @param region            Object storage region
     * @throws IllegalArgumentException Thrown if any required argument is missing
     */
    public ObjectStorage(String accessKey, String secretKey, String endpoint,
                         String region) throws IllegalArgumentException {
        this(accessKey, secretKey, endpoint, region, new BasicCPMSLoggerBuilder().withClass(ObjectStorage.class).build());
    }

    /**
     * Fully parameterized constructor
     * @param accessKey         Object storage access key
     * @param secretKey         Object storage secret key
     * @param endpoint          Object storage endpoint web address
     * @param region            Object storage region
     * @param logger            ORIENLogger instance for class logging
     * @throws IllegalArgumentException Thrown if any required argument is missing
     */
    public ObjectStorage(String accessKey, String secretKey, String endpoint, String region,
                         CPMSLogger logger) throws IllegalArgumentException {
        setLogger(logger);
        this.logger.debug("Call to ObjectStorage constructor(...)");
        if (accessKey == null || accessKey.equals("")) {
            throw new IllegalArgumentException("accessKey cannot be empty");
        }
        this.accessKey = accessKey;
        if (secretKey == null || secretKey.equals("")) {
            throw new IllegalArgumentException("secretKey cannot be empty");
        }
        this.secretKey = secretKey;
        if (endpoint == null || endpoint.equals("")) {
            throw new IllegalArgumentException("endpoint cannot be empty");
        }
        this.endpoint = endpoint;
        if (region == null) {
            throw new IllegalArgumentException("region cannot be null");
        }
        this.region = region;
        this.logger.trace("Building AWS Credentials");
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        this.logger.trace("Building S3 client configuration");
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setProtocol(Protocol.HTTPS);
        clientConfiguration.setSignerOverride("S3SignerType");
        clientConfiguration.setMaxConnections(200);
        clientConfiguration.setConnectionMaxIdleMillis(1000);
        this.logger.trace("Building S3 client");
        conn = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .withClientConfiguration(clientConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withAccelerateModeEnabled(false)
                .withPathStyleAccessEnabled(true)
                .withPayloadSigningEnabled(false)
                .build();
        this.logger.trace("Building new MD5Tools");
    }

    public AmazonS3 getConnection() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        logger.trace("Building S3 client configuration");
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setProtocol(Protocol.HTTPS);
        clientConfiguration.setSignerOverride("S3SignerType");
        clientConfiguration.setMaxConnections(200);
        clientConfiguration.setConnectionMaxIdleMillis(1000);
        logger.trace("Building S3 client");
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .withClientConfiguration(clientConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withAccelerateModeEnabled(false)
                .withPathStyleAccessEnabled(true)
                .withPayloadSigningEnabled(false)
                .build();
    }

    public List<Bucket> buckets() {
        logger.debug("Call to buckets()");
        return conn.listBuckets();
    }

    public boolean doesBucketExist(String bucket) {
        logger.debug("Call to doesBucketExist [bucket = {}]", bucket);
        return conn.doesBucketExistV2(bucket);
    }

    public void createBucket(String bucket) {
        logger.debug("Call to createBucket [bucket = {}]", bucket);
        try {
            if (!conn.doesBucketExistV2(bucket)) {
                Bucket mybucket = conn.createBucket(bucket);
                logger.debug("Created bucket [{}] ", bucket);
            }
        } catch (Exception ex) {
            logger.error("createBucket {}", ex.getMessage());
        }
    }

    public List<String> listBucketDirectories(String bucket) {
        logger.debug("Call to listBucketDirs [bucket = {}]", bucket);
        List<String> dirList = new ArrayList<>();
        try {
            if (doesBucketExist(bucket)) {
                logger.trace("Instantiating new ListObjectsRequest");
                ListObjectsRequest lor = new ListObjectsRequest();
                lor.setBucketName(bucket);
                lor.setDelimiter("/");

                logger.trace("Grabbing [objects] list from [lor]");
                //if(doesBucketExist(bucket))
                ObjectListing objects = conn.listObjects(lor);
                do {
                    List<String> sublist = objects.getCommonPrefixes();
                    logger.trace("Adding all Common Prefixes from [objects]");
                    dirList.addAll(sublist);
                    logger.trace("Grabbing next batch of [objects]");
                    objects = conn.listNextBatchOfObjects(objects);
                } while (objects.isTruncated());
            } else {
                logger.info("Bucket :" + bucket + " does not exist!");
            }
        } catch (Exception ex) {
            logger.error("listBucketDirs {}", ex.getMessage());
            dirList = null;
        }
        return dirList;
    }

    public List<String> listBucketObjectNames(String bucket) {
        logger.debug("{}('{}')", new Object(){}.getClass().getEnclosingMethod().getName(), bucket);
        List<String> ret = new ArrayList<>();
        try {
            if (doesBucketExist(bucket)) {
                logger.trace("Grabbing [objects] list from [bucket]");
                ObjectListing objects = conn.listObjects(bucket);
                do {
                    for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
                        ret.add(objectSummary.getKey());
                    }
                    logger.trace("Grabbing next batch of [objects]");
                    objects = conn.listNextBatchOfObjects(objects);
                } while (objects.isTruncated());
            } else {
                logger.warn("Bucket [{}] does not exist!", bucket);
            }
        } catch (Exception e) {
            logger.error("{}('{}'):{}", new Object(){}.getClass().getEnclosingMethod().getName(), bucket,
                    ExceptionUtils.getStackTrace(e));
            ret = new ArrayList<>();
        }
        return ret;
    }

    public List<S3ObjectSummary> listBucketObjects(String bucket) {
        logger.debug("{}('{}')", new Object(){}.getClass().getEnclosingMethod().getName(), bucket);
        List<S3ObjectSummary> ret = new ArrayList<>();
        try {
            if (doesBucketExist(bucket)) {
                logger.trace("Grabbing [objects] list from [bucket]");
                ObjectListing objects = conn.listObjects(bucket);
                do {
                    ret.addAll(objects.getObjectSummaries());
                    logger.trace("Grabbing next batch of [objects]");
                    objects = conn.listNextBatchOfObjects(objects);
                } while (objects.isTruncated());
            } else {
                logger.warn("Bucket [{}] does not exist!", bucket);
            }
        } catch (Exception e) {
            logger.error("{}('{}'):{}", new Object(){}.getClass().getEnclosingMethod().getName(), bucket,
                    ExceptionUtils.getStackTrace(e));
            ret = new ArrayList<>();
        }
        return ret;
    }

    public Set<String> listBucketObjects(String bucket, String prefix) {
        logger.debug("{}('{}','{}')", new Object(){}.getClass().getEnclosingMethod().getName(), bucket, prefix);
        Set<String> ret = new HashSet<>();
        try {
            if (doesBucketExist(bucket)) {
                logger.trace("Grabbing [objects] list from [{}], matching [{}]", bucket, prefix);
                ObjectListing objects = conn.listObjects(bucket, prefix);
                do {
                    for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
                        ret.add(objectSummary.getKey());
                    }
                    logger.trace("Grabbing next batch of [objects]");
                    objects = conn.listNextBatchOfObjects(objects);
                } while (objects.isTruncated());
            } else {
                logger.warn("Bucket [{}] does not exist!", bucket);
            }
        } catch (Exception ex) {
            logger.error("listBucketContents {}", ex.getMessage());
            ret = new HashSet<>();
        }
        return ret;
    }

    public Map<String, String> listBucketObjectsWithETag(String bucket) {
        logger.debug("{}('{}')", new Object(){}.getClass().getEnclosingMethod().getName(), bucket);
        Map<String, String> ret = new HashMap<>();
        try {
            if (doesBucketExist(bucket)) {
                logger.trace("Grabbing [objects] list from [bucket]");
                ObjectListing objects = conn.listObjects(bucket);
                do {
                    for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
                        ret.put(objectSummary.getKey(), objectSummary.getETag());
                    }
                    logger.trace("Grabbing next batch of [objects]");
                    objects = conn.listNextBatchOfObjects(objects);
                } while (objects.isTruncated());
            } else {
                logger.warn("Bucket [{}] does not exist!", bucket);
            }
        } catch (Exception e) {
            logger.error("{}('{}'):{}", new Object(){}.getClass().getEnclosingMethod().getName(), bucket,
                    ExceptionUtils.getStackTrace(e));
            ret = new HashMap<>();
        }
        return ret;
    }

    public Map<String, String> listBucketObjectsWithETag(String bucket, String prefix) {
        logger.debug("{}('{}','{}')", new Object(){}.getClass().getEnclosingMethod().getName(), bucket, prefix);
        Map<String, String> ret = new HashMap<>();
        try {
            if (doesBucketExist(bucket)) {
                logger.trace("Grabbing [objects] list from [bucket]");
                ObjectListing objects = conn.listObjects(bucket, prefix);
                do {
                    for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
                        ret.put(objectSummary.getKey(), objectSummary.getETag());
                    }
                    logger.trace("Grabbing next batch of [objects]");
                    objects = conn.listNextBatchOfObjects(objects);
                } while (objects.isTruncated());
            } else {
                logger.warn("Bucket [{}] does not exist!", bucket);
            }
        } catch (Exception e) {
            logger.error("{}('{}'):{}", new Object(){}.getClass().getEnclosingMethod().getName(), bucket,
                    ExceptionUtils.getStackTrace(e));
            ret = new HashMap<>();
        }
        return ret;
    }

    public Map<String, Long> listBucketObjectsWithSize(String bucket) {
        logger.debug("{}('{}')", new Object(){}.getClass().getEnclosingMethod().getName(), bucket);
        Map<String, Long> ret = new HashMap<>();
        try {
            if (doesBucketExist(bucket)) {
                logger.trace("Grabbing [objects] list from [bucket]");
                ObjectListing objects = conn.listObjects(bucket);
                do {
                    for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
                        ret.put(objectSummary.getKey(), objectSummary.getSize());
                    }
                    logger.trace("Grabbing next batch of [objects]");
                    objects = conn.listNextBatchOfObjects(objects);
                } while (objects.isTruncated());
            } else {
                logger.warn("Bucket [{}] does not exist!", bucket);
            }
        } catch (Exception e) {
            logger.error("{}('{}'):{}", new Object(){}.getClass().getEnclosingMethod().getName(), bucket,
                    ExceptionUtils.getStackTrace(e));
            ret = new HashMap<>();
        }
        return ret;
    }

    public Map<String, Long> listBucketObjectsWithSize(String bucket, String prefix) {
        logger.debug("{}('{}','{}')", new Object(){}.getClass().getEnclosingMethod().getName(), bucket, prefix);
        Map<String, Long> ret = new HashMap<>();
        try {
            if (doesBucketExist(bucket)) {
                logger.trace("Grabbing [objects] list from [bucket]");
                ObjectListing objects = conn.listObjects(bucket, prefix);
                do {
                    for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
                        ret.put(objectSummary.getKey(), objectSummary.getSize());
                    }
                    logger.trace("Grabbing next batch of [objects]");
                    objects = conn.listNextBatchOfObjects(objects);
                } while (objects.isTruncated());
            } else {
                logger.warn("Bucket [{}] does not exist!", bucket);
            }
        } catch (Exception e) {
            logger.error("{}('{}'):{}", new Object(){}.getClass().getEnclosingMethod().getName(), bucket,
                    ExceptionUtils.getStackTrace(e));
            ret = new HashMap<>();
        }
        return ret;
    }

    public String findBucketObject(String bucket, String prefix, String objectName) {
        String delimiter = "/";
        if (!prefix.endsWith(delimiter))
            prefix += delimiter;
        for (String object : listBucketObjects(bucket, prefix)) {
            if (object.endsWith(objectName))
                return object;
        }
        return null;
    }

    public boolean doesObjectExist(String bucket, String objectName) {
        logger.trace("doesObjectExist('{}','{}')", bucket, objectName);
        try {
            return conn.doesObjectExist(bucket, objectName);
        } catch (Exception e) {
            logger.cpmsError("Exception getting object existence: {}:{}", e.getClass().getCanonicalName(), e.getMessage());
            return false;
        }
    }

    public long getObjectSize(String bucket, String objectName) {
        logger.trace("getObjectSize('{}','{}')", bucket, objectName);
        try {
            if (doesObjectExist(bucket, objectName))
                return conn.getObject(bucket, objectName).getObjectMetadata().getContentLength();
            return -1L;
        } catch (Exception e) {
            logger.cpmsError("Exception getting object size: {}:{}", e.getClass().getCanonicalName(), e.getMessage());
            return -1L;
        }
    }

    public String getObjectTag(String bucket, String objectName, String tagName) {
        logger.trace("getObjectTag('{}','{}','{}')", bucket, objectName, tagName);
        if (doesObjectExist(bucket, objectName))
            return conn.getObject(bucket, objectName).getObjectMetadata().getUserMetaDataOf(tagName);
        return null;
    }

    public boolean deleteBucketObject(String bucket, String objectKey) {
        logger.debug("Call to deleteBucketObject({},{})", bucket, objectKey);
        try {
            conn.deleteObject(bucket, objectKey);
            return true;
        } catch (Exception e) {
            logger.error("deleteBucketObject: {}", ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    public boolean deleteBucketContents(String bucket) {
        logger.debug("Call to deleteBucketContents [bucket = {}]", bucket);
        try {
            ObjectListing objects = conn.listObjects(bucket);
            do {
                for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
                    logger.trace("Deleting [{}] object from [{}] bucket", objectSummary.getKey(), bucket);
                    conn.deleteObject(bucket, objectSummary.getKey());
                    logger.debug("Deleted {}\t{}\t{}\t{}", objectSummary.getKey(),
                            objectSummary.getSize(),
                            objectSummary.getETag(),
                            DateFormatUtils.format(objectSummary.getLastModified(),
                                    DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.getPattern()));
                }
                objects = conn.listNextBatchOfObjects(objects);
            } while (objects.isTruncated());
            return true;
        } catch (Exception e) {
            logger.error("deleteBucketContents:{}", ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    public boolean deleteBucketContents(String bucket, String prefix) {
        logger.debug("Call to deleteBucketDirectoryContents [bucket = {}, prefixKey = {}]", bucket, prefix);
        try {
            ObjectListing objects = conn.listObjects(bucket, prefix);
            do {
                for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
                    logger.trace("Deleting [{}] object from [{}] bucket", objectSummary.getKey(), bucket);
                    conn.deleteObject(bucket, objectSummary.getKey());
                    logger.debug("Deleted {}\t{}\t{}\t{}", objectSummary.getKey(),
                            objectSummary.getSize(),
                            objectSummary.getETag(),
                            DateFormatUtils.format(objectSummary.getLastModified(),
                                    DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.getPattern()));
                }
                objects = conn.listNextBatchOfObjects(objects);
            } while (objects.isTruncated());
            return true;
        } catch (Exception e) {
            logger.error("deleteBucketContents:{}", ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    /*
        Object Transfer Methods
     */

    public boolean uploadFile(Path uploadPath, String bucket,
                              String s3Prefix, long uncompressedSize,
                              boolean renameExisting) throws IOException {
        return uploadFile(uploadPath, bucket, s3Prefix, CPMSStatics.DEFAULT_PART_SIZE, uncompressedSize, renameExisting);
    }

    /**
     * Uploads a local file to the object storage account for this instance
     * @param uploadPath        Path of the file to upload
     * @param bucket            Bucket into which the file is uploaded
     * @param s3Prefix          The object prefix (excluding bucket name) for this file
     * @param partSize          The multipart upload size threshold to use for this upload
     * @param uncompressedSize  The uncompressed size for S3 tagging to ensure adequate space on restoration
     * @param renameExisting    Rename any existing files with their timestamp, otherwise overwrite them
     * @return                  Whether the upload was a success
     * @throws IOException      If either the file to upload or the object storage bucket does not exist
     */
    public boolean uploadFile(Path uploadPath, String bucket,
                              String s3Prefix, int partSize, long uncompressedSize,
                              boolean renameExisting) throws IOException {
        logger.trace("uploadFile('{}','{}','{}',{},{},{})", uploadPath.toAbsolutePath(), bucket,
                s3Prefix, partSize, uncompressedSize, renameExisting);
        boolean success = false;
        if (!Files.exists(uploadPath))
            throw new IOException("file to upload does not exist");
        if (!doesBucketExist(bucket))
            throw new IOException("target bucket does not exist");
        if (s3Prefix == null)
            s3Prefix = "";
        AmazonS3 s3 = getConnection();
        TransferManager manager = null;
        logger.trace("Building TransferManager");
        try {
            manager = TransferManagerBuilder.standard()
                    .withS3Client(s3)
                    .withMultipartUploadThreshold(1024L * 1024L * partSize)
                    .withMinimumUploadPartSize(1024L * 1024L * partSize)
                    .build();
            logger.trace("s3Prefix: {}", s3Prefix);
            if (conn.doesObjectExist(bucket, s3Prefix)) {
                if (renameExisting) {
                    logger.cpmsInfo("Backing up existing object [{}/{}]", bucket, s3Prefix);
                    S3Object existingObject = conn.getObject(bucket, s3Prefix);
                    String s3PrefixRename = String.format("%s.%s", s3Prefix,
                            new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss-SSS")
                                    .format(existingObject.getObjectMetadata().getLastModified()));
                    logger.trace("[{}/{}] being renamed to [{}/{}]", bucket, s3Prefix, bucket, s3PrefixRename);
                    if (existingObject.getObjectMetadata().getContentLength() > manager.getConfiguration().getMultipartCopyThreshold()) {
                        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucket,
                                s3PrefixRename);
                        InitiateMultipartUploadResult initResult = conn.initiateMultipartUpload(initRequest);
                        long objectSize = existingObject.getObjectMetadata().getContentLength();
                        long copyPartSize = 1024L * 1024L * partSize;
                        long bytePosition = 0;
                        int partNum = 1;
                        int numParts = (int)(objectSize / copyPartSize);
                        int notificationStep = 5;
                        int percentDone = 0;
                        int nextPercent = percentDone + notificationStep;
                        List<CopyPartResult> copyResponses = new ArrayList<>();
                        logger.trace("Starting multipart upload (%d parts) to copy [{}/{}] to [{}/{}]",
                                        numParts, bucket, s3Prefix, bucket, s3PrefixRename);
                        while (bytePosition < objectSize) {
                            if ((partNum/numParts) > nextPercent) {
                                logger.cpmsInfo("Copying in progress ({}/{} {}%)",
                                                partNum, numParts, (partNum/numParts));
                                nextPercent = percentDone + notificationStep;
                            }
                            long lastByte = Math.min(bytePosition + copyPartSize - 1, objectSize - 1);
                            CopyPartRequest copyRequest = new CopyPartRequest()
                                    .withSourceBucketName(bucket)
                                    .withSourceKey(s3Prefix)
                                    .withDestinationBucketName(bucket)
                                    .withDestinationKey(s3PrefixRename)
                                    .withUploadId(initResult.getUploadId())
                                    .withFirstByte(bytePosition)
                                    .withLastByte(lastByte)
                                    .withPartNumber(partNum++);
                            copyResponses.add(conn.copyPart(copyRequest));
                            bytePosition += copyPartSize;
                        }
                        logger.trace("Creating multipart upload completion request");
                        CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(
                                bucket, s3PrefixRename, initResult.getUploadId(), getETags(copyResponses)
                        );
                        logger.trace("Completing multipart upload");
                        conn.completeMultipartUpload(completeRequest);
                    } else {
                        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(bucket, s3Prefix, bucket,
                                s3PrefixRename);
                        conn.copyObject(copyObjectRequest);
                    }
                    logger.cpmsInfo("Existing object [{}/{}] copied successfully to [{}/{}]",
                                    bucket, s3Prefix, bucket, s3PrefixRename);
                    existingObject.close();
                }
                logger.cpmsInfo("Deleting existing object [{}/{}]", bucket, s3Prefix);
                DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucket, s3Prefix);
                conn.deleteObject(deleteObjectRequest);
                logger.trace("[{}] is ready for upload of [{}]", bucket, s3Prefix);
            }
            logger.cpmsInfo("Starting Upload to S3: [{}] => [{}/{}]", uploadPath, bucket, s3Prefix);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.addUserMetadata(CPMSStatics.UNCOMPRESSED_SIZE_METADATA_TAG_KEY, String.valueOf(uncompressedSize));
            metadata.addUserMetadata(CPMSStatics.PART_SIZE_METADATA_TAG_KEY, String.valueOf(partSize));
            PutObjectRequest request = new PutObjectRequest(bucket, s3Prefix, uploadPath.toFile()).withMetadata(metadata);
            request.setGeneralProgressListener(new LoggingProgressListener(logger, Files.size(uploadPath)));
            logger.trace("Starting upload to S3");
            long uploadStartTime = System.currentTimeMillis();
            Upload transfer = manager.upload(request);
            UploadResult result = transfer.waitForUploadResult();
            long uploadEndTime = System.currentTimeMillis();
            Duration uploadDuration = Duration.of(uploadEndTime - uploadStartTime, ChronoUnit.MILLIS);
            logger.trace("Upload finished in {}", formatDuration(uploadDuration));
            String s3Checksum = result.getETag();
            logger.trace("s3Checksum: {}", result.getETag());
            logger.cpmsInfo("Verifying upload [{}/{}] via checksums", bucket, s3Prefix);
            String localChecksum;
            MD5Tools md5Tools = new MD5Tools(partSize);
            if (s3Checksum.contains("-"))
                localChecksum = md5Tools.getMultiCheckSum(uploadPath.toString(), manager);
            else
                localChecksum = md5Tools.getCheckSum(uploadPath.toString());
            logger.trace("localChecksum: {}", localChecksum);
            if (!localChecksum.equals(result.getETag()))
                logger.cpmsError("Checksums don't match [local: {}, S3: {}]", localChecksum, result.getETag());
            success = localChecksum.equals(result.getETag());
        } catch (AmazonServiceException ase) {
            logger.cpmsError("Caught an AmazonServiceException, which means your request made it "
                                    + "to Amazon S3, but was rejected with an error response for some reason. (" +
                                    "Error Message: {}, HTTP Status Code: {}, AWS Error Code: {}, Error Type: {}, " +
                                    "Request ID: {}",
                            ase.getMessage(), ase.getStatusCode(), ase.getErrorCode(),
                            ase.getErrorType().toString(), ase.getRequestId());
        } catch (SdkClientException ace) {
            logger.cpmsError("Caught an AmazonClientException, which means the client encountered "
                                    + "a serious internal problem while trying to communicate with S3, (" +
                                    "Error Message: {}",
                            ace.getMessage());
        } catch (InterruptedException | IOException ie) {
            logger.cpmsError("{}:{}", ie.getClass().getCanonicalName(), ie.getMessage());
        } finally {
            try {
                assert manager != null;
                manager.shutdownNow();
                assert s3 != null;
                s3.shutdown();
            } catch (AssertionError ae) {
                logger.cpmsError("uploadFile : TransferManager was pre-emptively shut down.");
            }
        }
        return success;
    }

    public boolean downloadObject(String bucket, String prefix, Path destinationDirectory) {
        logger.debug("downloadObject('{}','{}','{}')", bucket, prefix,
                destinationDirectory);
        AmazonS3 s3 = getConnection();
        if (!s3.doesBucketExistV2(bucket)) {
            logger.cpmsError("Bucket [{}] does not exist", bucket);
            return false;
        }
        if (!s3.doesObjectExist(bucket, prefix)) {
            logger.cpmsError("Bucket [{}] does not contain [{}]", bucket, prefix);
            return false;
        }
        S3Object s3Object = s3.getObject(bucket, prefix);
        long s3ObjectSize = s3Object.getObjectMetadata().getContentLength();
        String s3Checksum = s3Object.getObjectMetadata().getETag();
        int s3PartSize = CPMSStatics.DEFAULT_PART_SIZE;
        try {
            ObjectMetadata objectMetadata = s3Object.getObjectMetadata();
            if (objectMetadata != null) {
                if (objectMetadata.getUserMetadata().containsKey(CPMSStatics.PART_SIZE_METADATA_TAG_KEY))
                    s3PartSize = Integer.parseInt(s3Object.getObjectMetadata().getUserMetaDataOf(CPMSStatics.PART_SIZE_METADATA_TAG_KEY));
            }
        } catch (NumberFormatException e) {
            logger.cpmsError("Invalid multipart upload partSize metadata tag for [{}/{}]", bucket, prefix);
        }
        try {
            s3Object.getObjectContent().abort();
            s3Object.close();
        } catch (IOException e) {
            logger.error("{}:{}", e.getClass().getCanonicalName(), e.getMessage());
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
        boolean success = false;
        TransferManager manager;
        logger.debug("Building TransferManager");
        try {
            logger.trace("s3Checksum: {}", s3Checksum);
            manager = TransferManagerBuilder.standard()
                    .withS3Client(s3)
                    .withMultipartUploadThreshold(1024L * 1024L * s3PartSize)
                    .withMinimumUploadPartSize(1024L * 1024L * s3PartSize)
                    .build();
            int prefixLength = prefix.lastIndexOf("/") + 1;
            Path outFile = destinationDirectory.resolve(prefix.substring(prefixLength));
            logger.trace("outFile: {}", outFile);
            GetObjectRequest request = new GetObjectRequest(bucket, prefix);
            request.setGeneralProgressListener(new LoggingProgressListener(logger, s3ObjectSize));
            logger.debug("Initiating download: [{}] => [{}]", prefix, outFile);
            Download transfer = manager.download(request, outFile.toFile());
            transfer.waitForCompletion();
            if (!Files.exists(outFile)) {
                logger.cpmsError("[{}] does not exist after download of [{}/{}]", outFile, bucket, prefix);
                return false;
            }
            logger.cpmsInfo("Verifying download via checksums");
            MD5Tools md5Tools = new MD5Tools(s3PartSize * 1024 * 1024);
            String localChecksum;
            if (s3Checksum.contains("-"))
                localChecksum = md5Tools.getMultiCheckSum(outFile.toAbsolutePath().toString(), manager);
            else
                localChecksum = md5Tools.getCheckSum(outFile.toAbsolutePath().toString());
            logger.debug("localChecksum: {}", localChecksum);
            if (!localChecksum.equals(s3Checksum))
                logger.cpmsError("Checksums don't match [local: {}, S3: {}]", localChecksum, s3Checksum);
            success = localChecksum.equals(s3Checksum);
            manager.shutdownNow();
        } catch (AmazonServiceException ase) {
            logger.cpmsError("Caught an AmazonServiceException, which means your request made it "
                                    + "to Amazon S3, but was rejected with an error response for some reason. (" +
                                    "Error Message: {}, HTTP Status Code: {}, AWS Error Code: {}, Error Type: {}, " +
                                    "Request ID: {}",
                            ase.getMessage(), ase.getStatusCode(), ase.getErrorCode(),
                            ase.getErrorType().toString(), ase.getRequestId());
        } catch (SdkClientException ace) {
            logger.cpmsError("Caught an AmazonClientException, which means the client encountered "
                                    + "a serious internal problem while trying to communicate with S3, (" +
                                    "Error Message: {}",
                            ace.getMessage());
        } catch (InterruptedException | IOException ie) {
            logger.cpmsError("{}:{}", ie.getClass().getCanonicalName(), ie.getMessage());
        }/* finally {
            try {
                assert manager != null;
                manager.shutdownNow();
                assert s3 != null;
                s3.shutdown();
            } catch (AssertionError ae) {
                logger.cpmsError("downloadFile : TransferManager was pre-emptively shut down.");
            }
        }*/
        return success;
    }

    public boolean downloadObjectStream(String bucket, String prefix, Path destinationDirectory) {
        logger.debug("downloadObjectStream('{}','{}','{}')", bucket, prefix,
                destinationDirectory);
        if (!conn.doesBucketExistV2(bucket)) {
            logger.cpmsError("Bucket [{}] does not exist", bucket);
            return false;
        }
        if (!conn.doesObjectExist(bucket, prefix)) {
            logger.cpmsError("Bucket [{}] does not contain [{}]", bucket, prefix);
            return false;
        }
        S3Object s3Object = conn.getObject(bucket, prefix);
        long s3ObjectSize = s3Object.getObjectMetadata().getContentLength();
        int s3PartSize = CPMSStatics.DEFAULT_PART_SIZE;
        try {
            ObjectMetadata objectMetadata = s3Object.getObjectMetadata();
            if (objectMetadata != null) {
                if (objectMetadata.getUserMetadata().containsKey(CPMSStatics.PART_SIZE_METADATA_TAG_KEY))
                    s3PartSize = Integer.parseInt(s3Object.getObjectMetadata().getUserMetaDataOf(CPMSStatics.PART_SIZE_METADATA_TAG_KEY));
            }
        } catch (NumberFormatException e) {
            logger.cpmsError("Invalid multipart upload partSize metadata tag for [{}/{}]", bucket, prefix);
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
        boolean success = false;
        AmazonS3 s3 = getConnection();
        try {
            String s3Checksum = s3Object.getObjectMetadata().getETag();
            logger.trace("s3Checksum: {}", s3Checksum);
            int prefixLength = prefix.lastIndexOf("/") + 1;
            Path outFile = destinationDirectory.resolve(prefix.substring(prefixLength));
            logger.trace("outFile: {}", outFile);
            ProgressListener progressListener = new LoggingProgressListener(logger,
                    s3Object.getObjectMetadata().getContentLength());
            InputStream objectData = s3Object.getObjectContent();
            OutputStream outStream = new FileOutputStream(outFile.toFile());
            int n;
            long bytesDownloaded = 0L;
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

            while ((n = objectData.read(buffer)) > -1) {
                bytesDownloaded += n;
                progressListener.progressChanged(new ProgressEvent(ProgressEventType.REQUEST_BYTE_TRANSFER_EVENT, n));
                outStream.write(buffer, 0, n);
            }

            outStream.close();
            objectData.close();
            s3Object.close();
            if (!Files.exists(outFile)) {
                logger.cpmsError("[{}] does not exist after download of [{}/{}]", outFile, bucket, prefix);
                return false;
            }
            logger.cpmsInfo("Verifying download via checksums");
            MD5Tools md5Tools = new MD5Tools(s3PartSize * 1024 * 1024);
            String localChecksum;
            if (s3Checksum.contains("-"))
                localChecksum = md5Tools.getMultiCheckSum(outFile.toAbsolutePath().toString());
            else
                localChecksum = md5Tools.getCheckSum(outFile.toAbsolutePath().toString());
            logger.debug("localChecksum: {}", localChecksum);
            if (!localChecksum.equals(s3Checksum))
                logger.cpmsError("Checksums don't match [local: {}, S3: {}]", localChecksum, s3Checksum);
            success = localChecksum.equals(s3Checksum);
        } catch (AmazonServiceException ase) {
            logger.cpmsError("Caught an AmazonServiceException, which means your request made it "
                            + "to Amazon S3, but was rejected with an error response for some reason. (" +
                            "Error Message: {}, HTTP Status Code: {}, AWS Error Code: {}, Error Type: {}, " +
                            "Request ID: {}",
                    ase.getMessage(), ase.getStatusCode(), ase.getErrorCode(),
                    ase.getErrorType().toString(), ase.getRequestId());
        } catch (SdkClientException ace) {
            logger.cpmsError("Caught an AmazonClientException, which means the client encountered "
                            + "a serious internal problem while trying to communicate with S3, (" +
                            "Error Message: {}",
                    ace.getMessage());
        } catch (IOException ie) {
            logger.cpmsError("{}:{}", ie.getClass().getCanonicalName(), ie.getMessage());
        } finally {
            try {
                assert s3 != null;
                s3.shutdown();
            } catch (AssertionError ae) {
                logger.cpmsError("downloadObjectStream : TransferManager was pre-emptively shut down.");
            }
        }
        return success;
    }

    public CPMSLogger getLogger() {
        return logger;
    }

    public void setLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(ObjectStorage.class);
    }

    public void updateLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(ObjectStorage.class);
    }

    /*
        Private Helper Methods
     */

    private static class LoggingProgressListener implements ProgressListener {
        private final CPMSLogger logger;
        private final int updatePercentStep = 5;
        private final long totalBytes;
        private long lastTimestamp;
        private long totalTransferred = 0L;
        private long lastTransferred = 0L;
        private int nextUpdate = updatePercentStep;

        public LoggingProgressListener(CPMSLogger logger, long totalBytes) {
            this.lastTimestamp = System.currentTimeMillis();
            this.logger = logger.cloneLogger(LoggingProgressListener.class);
            this.totalBytes = totalBytes;
        }

        @Override
        public void progressChanged(ProgressEvent progressEvent) {
            Thread.currentThread().setName("TransferListener");
            long currentBytesTransferred = progressEvent.getBytesTransferred();
            this.totalTransferred += currentBytesTransferred;
            this.lastTransferred += currentBytesTransferred;
            float currentTransferPercentage = ((float)totalTransferred / (float)totalBytes) * (float)100;
            if (currentTransferPercentage > (float)nextUpdate - 0.01) {
                long currentTimestamp = System.currentTimeMillis();
                logger.cpmsInfo("Transferring ({}/{} {}%) at {}",
                        humanReadableByteCount(totalTransferred), humanReadableByteCount(totalBytes),
                        (int)(currentTransferPercentage + 0.01),
                        humanReadableTransferRate(lastTransferred, currentTimestamp - lastTimestamp));
                lastTransferred = 0L;
                lastTimestamp = currentTimestamp;
                nextUpdate += updatePercentStep;
                if (currentTransferPercentage >= (float)(100.0 - 0.01))
                    logger.cpmsInfo("Completing/closing parallel transfers");
            }
        }
    }

    private static List<PartETag> getETags(List<CopyPartResult> responses) {
        List<PartETag> etags = new ArrayList<>();
        for (CopyPartResult response : responses) {
            etags.add(new PartETag(response.getPartNumber(), response.getETag()));
        }
        return etags;
    }

    private static String humanReadableByteCount(long bytes/*, boolean si*/) {
        int unit = /*si ? */1000/* : 1024*/;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (/*si ? */"kMGTPE"/* : "KMGTPE"*/).charAt(exp-1) + (/*si ? */""/* : "i"*/);
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
