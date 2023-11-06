package io.cresco.cpms.processing;

import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;
import io.cresco.cpms.scripting.StorageTask;
import io.cresco.cpms.storage.encapsulation.Archiver;
import io.cresco.cpms.storage.encapsulation.ArchiverBuilder;
import io.cresco.cpms.storage.transfer.ObjectStorageBuilder;
import io.cresco.cpms.storage.transfer.ObjectStorageV2;
import io.cresco.cpms.storage.utilities.StorageParameters;
import io.cresco.cpms.storage.utilities.StorageProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

@SuppressWarnings({"unused", "WeakerAccess", "BooleanMethodIsAlwaysInverted", "SameParameterValue"})
public class StorageEngine {
    private CPMSLogger logger;

    /*
        Constructors
     */

    public StorageEngine() {
        this(new BasicCPMSLoggerBuilder().withClass(StorageEngine.class).build());
    }

    public StorageEngine(CPMSLogger logger) {
        setLogger(logger);
    }

    public boolean runStorageJob(StorageTask storageTask) {
        logger.debug("runStorageJob({})", storageTask);
        if (storageTask == null) {
            logger.cpmsError("Submitted storage job cannot be null");
            return false;
        }
        switch(storageTask.getAction()) {
            case "list": {
                logger.info("List task");
                StorageParameters remoteStorageParameters = new StorageParameters(storageTask.getRemotePath());
                logger.trace("Remote Storage Provider: {}", remoteStorageParameters.storageProvider);
                logger.trace("Remote Bucket: {}", remoteStorageParameters.bucket);
                logger.trace("Remote Prefix: {}", remoteStorageParameters.prefix);
                if (remoteStorageParameters.storageProvider == StorageProvider.S3) {
                    if (remoteStorageParameters.prefix != null)
                        return listS3BucketObjectsWithPrefix(remoteStorageParameters.bucket,
                                remoteStorageParameters.prefix);
                    else if (remoteStorageParameters.bucket != null)
                        return listS3BucketObjects(remoteStorageParameters.bucket);
                    else
                        return listS3Buckets();
                } else {
                    logger.error("Storage provider [{}] is not implemented yet!",
                            remoteStorageParameters.storageProvider.name());
                    return false;
                }
            }
            case "upload": {
                logger.info("Upload task");
                if (storageTask.getLocalPath() == null || !Files.exists(storageTask.getLocalPath())) {
                    logger.cpmsError("Local path to upload [{}] does not exist", storageTask.getLocalPath());
                    return false;
                }
                if (storageTask.getRemotePath() == null || storageTask.getRemotePath().isEmpty()) {
                    logger.cpmsError("Remote path to upload cannot be empty");
                    return false;
                }
                StorageParameters remoteStorageParameters = new StorageParameters(storageTask.getRemotePath());
                if (remoteStorageParameters.storageProvider == StorageProvider.S3) {
                    return uploadToS3(storageTask.getLocalPath(), remoteStorageParameters.bucket,
                            remoteStorageParameters.prefix);
                } else {
                    logger.error("Storage provider [{}] is not implemented yet!",
                            remoteStorageParameters.storageProvider.name());
                    return false;
                }
            }
            case "download": {
                logger.info("Download task");
                /*if (!storageTask.getS3Path().endsWith("/")) {
                    if (objectStorage.headObject(storageTask.getS3Bucket(), storageTask.getS3Path()) == null) {
                        logger.cpmsError("S3 object [{}/{}] does not exist to download",
                                storageTask.getS3Bucket(), storageTask.getS3Path());
                        return false;
                    }
                }
                if (!Files.exists(storageTask.getLocalPath())) {
                    try {
                        Files.createDirectories(storageTask.getLocalPath());
                    } catch (IOException e) {
                        logger.cpmsError("Failed to create download directory [{}]",
                                storageTask.getLocalPath());
                        return false;
                    }
                }
                if (storageTask.getS3Path().endsWith("/"))
                    return downloadDirectoryOfBaggedFiles(storageTask.getS3Bucket(), storageTask.getS3Path(),
                            storageTask.getLocalPath());
                else
                    return downloadBaggedFile(storageTask.getS3Bucket(), storageTask.getS3Path(),
                            storageTask.getLocalPath());*/
                return true;
            }
            case "delete": {
                logger.info("Delete task");
                /*if (storageTask.getS3Path().endsWith("/")) {
                    return objectStorage.deleteBucketContents(storageTask.getS3Bucket(), storageTask.getS3Path());
                } else {
                    if (objectStorage.headObject(storageTask.getS3Bucket(), storageTask.getS3Path()) == null) {
                        logger.cpmsError("S3 object [{}/{}] does not exist to download",
                                storageTask.getS3Bucket(), storageTask.getS3Path());
                        return false;
                    }
                    return objectStorage.deleteBucketObject(storageTask.getS3Bucket(), storageTask.getS3Path());
                }*/
                return true;
            }
            default: {
                logger.cpmsError("An invalid StorageJob type [{}] was encountered", storageTask.getAction());
                return false;
            }
        }
    }

    /*
        Public Instance Methods
     */

    private boolean listS3Buckets() {
        logger.trace("listS3Buckets()");
        try {
            ObjectStorageV2 objectStorage = new ObjectStorageBuilder().withLogger(logger).build();
            objectStorage.listBuckets().stream().map(Bucket::name).forEach(System.out::println);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean listS3BucketObjects(String bucket) {
        logger.trace("listS3BucketObjects({})", bucket);
        try {
            ObjectStorageV2 objectStorage = new ObjectStorageBuilder().withLogger(logger).build();
            objectStorage.listBucketObjects(bucket).stream().map(S3Object::key).forEach(System.out::println);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean listS3BucketObjectsWithPrefix(String bucket, String prefix) {
        logger.trace("listS3BucketObjectsWithPrefix({},{})", bucket, prefix);
        try {
            ObjectStorageV2 objectStorage = new ObjectStorageBuilder().withLogger(logger).build();
            objectStorage.listBucketObjects(bucket, prefix).stream().map(S3Object::key).forEach(System.out::println);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean uploadToS3(Path localPath, String bucket, String prefix) {
        logger.trace("uploadToS3({},{},{})", localPath, bucket, prefix);
        try {
            if (!Files.exists(localPath)) {
                logger.cpmsError("Local path to upload [{}] does not exist", localPath);
                return false;
            }
            String key = (prefix == null || prefix.isEmpty()) ?
                    String.format("%s", localPath.getFileName()) :
                    String.format("%s/%s", prefix, localPath.getFileName());
            if (Files.isRegularFile(localPath)) {
                return uploadSingleFileToS3(localPath, bucket, key);
            } else {
                return uploadBaggedDirectoryToS3(localPath, bucket, prefix);
            }
        } catch (Exception e) {
            logger.cpmsError("Failed to upload path [{}] to S3 [{}/{}]", localPath, bucket, prefix);
            logger.debug("Exception: {}", ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    private boolean uploadSingleFileToS3(Path localFile, String bucket, String key) {
        logger.trace("uploadSingleFileToS3({},{},{})", localFile, bucket, key);
        try {
            ObjectStorageV2 objectStorage = new ObjectStorageBuilder().withLogger(logger).build();
            return objectStorage.uploadFile(localFile, bucket, key);
        } catch (Exception e) {
            logger.cpmsError("Failed to upload file [{}] to S3 [{}/{}]", localFile, bucket, key);
            logger.debug("Exception: {}", ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    private boolean uploadBaggedDirectoryToS3(Path localPath, String bucket, String prefix) {
        logger.trace("uploadBaggedDirectoryToS3('{}','{}','{}')", localPath, bucket, prefix);
        try {
            Archiver archiver = new ArchiverBuilder().withLogger(logger).build();
            ObjectStorageV2 objectStorage = new ObjectStorageBuilder().withLogger(logger).build();
            long uncompressedSize = FileUtils.sizeOfDirectory(localPath.toFile());
            logger.trace("uncompressedSize: {}", humanReadableByteCount(uncompressedSize));
            try {
                long freeSpace = Files.getFileStore(localPath).getUsableSpace();
                logger.trace("freeSpace: {}", humanReadableByteCount(freeSpace));
                long requiredSpace = uncompressedSize + (1024 * 1024 * 1024);
                logger.trace("requiredSpace: {}", humanReadableByteCount(requiredSpace));
                if (requiredSpace > freeSpace) {
                    logger.cpmsError("Not enough free space to bag up [{}], needs [{}] has [{}]",
                            localPath.toAbsolutePath(), humanReadableByteCount(requiredSpace),
                            humanReadableByteCount(freeSpace));
                    return false;
                }
            } catch (IOException e) {
                logger.cpmsError("Failed to locate path to upload [{}]", localPath);
                return false;
            }
            logger.cpmsInfo("Bagging up [{}]", localPath);
            Path bagged = archiver.bagItUp(localPath);
            if (bagged == null || !Files.exists(bagged)) {
                logger.cpmsError("Failed to bag up directory [{}]", localPath);
                return false;
            }
            logger.cpmsInfo("Verifying bagging on [{}]", localPath);
            if (!archiver.verifyBag(localPath)) {
                logger.cpmsError("Failed to bag up directory [{}]", localPath);
                archiver.debagify(localPath);
                return false;
            }
            logger.cpmsInfo("Boxing up [{}]", localPath.toAbsolutePath());
            Path boxed = archiver.archive(bagged.toFile());
            logger.cpmsInfo("Reverting bagging on directory [{}]", localPath);
            archiver.debagify(localPath);
            if (boxed == null || !Files.exists(boxed)) {
                logger.cpmsError("Failed to box up directory [{}]", localPath);
                return false;
            }
            String key = (prefix == null || prefix.isEmpty()) ?
                    String.format("%s", boxed.getFileName()) :
                    String.format("%s/%s", prefix, boxed.getFileName());
            logger.cpmsInfo("Uploading [{}] to [{}/{}]", boxed, bucket, key);
            boolean success = false;
            try {
                success = objectStorage.uploadFile(boxed, bucket, key);
                Files.delete(boxed);
                return success;
            } catch (IOException e) {
                logger.cpmsError("Failed to upload file [{}] to S3 [{}/{}]", boxed, bucket, key);
                logger.debug("IOException: {}", ExceptionUtils.getStackTrace(e));
                return success;
            }
        } catch (Exception e) {
            logger.cpmsError("Failed to upload bagged directory [{}] to S3 [{}/{}]", localPath, bucket, prefix);
            logger.debug("Exception: {}", ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    /*private boolean downloadDirectoryOfBaggedFiles(String bucket, String s3Prefix, Path outPath) {
        logger.trace("downloadDirectoryOfBaggedFiles('{}','{}','{}')", bucket, s3Prefix, outPath);
        if (!checkSetup(bucket, outPath, true)) {
            logger.cpmsError("Failed bagged download setup verification");
            return false;
        }
        if (!s3Prefix.endsWith("/"))
            s3Prefix += "/";
        long downloadSize = 0L;
        long largestObject = 0L;
        List<S3Object> s3Objects = objectStorage.listBucketObjects(bucket, s3Prefix);
        for (S3Object s3Object : s3Objects) {
            downloadSize += s3Object.size();
            if (s3Object.size() > largestObject)
                largestObject = s3Object.size();
        }
        logger.trace("downloadSize: {}", humanReadableByteCount(downloadSize));
        logger.trace("largestObject: {}", humanReadableByteCount(largestObject));
        try {
            long freeSpace = Files.getFileStore(outPath).getUsableSpace();
            logger.trace("freeSpace: {}", humanReadableByteCount(freeSpace));
            long requiredSpace = downloadSize + largestObject + (1024 * 1024 * 1024);
            logger.trace("requiredSpace: {}", humanReadableByteCount(requiredSpace));
            if (requiredSpace > freeSpace) {
                logger.cpmsError("Not enough free space in [{}], needs [{}] has [{}]",
                        outPath.toAbsolutePath(), humanReadableByteCount(requiredSpace),
                        humanReadableByteCount(freeSpace));
                return false;
            }
        } catch (IOException e) {
            logger.cpmsError("Invalid path provided: {}", outPath.toAbsolutePath());
            return false;
        }
        for (S3Object s3Object : s3Objects) {
            int prefixLength = s3Object.key().lastIndexOf("/") + 1;
            String objectName = s3Object.key().substring(prefixLength);
            Path boxedPath = outPath.resolve(objectName);
            Path parentFolder = boxedPath.getParent();
            try {
                Files.createDirectories(parentFolder);
            } catch (IOException e) {
                logger.cpmsError("Failed to create local parent directory [{}] for sample [{}]",
                        parentFolder, objectName);
                return false;
            }
            boolean success = downloadBaggedFile(bucket, objectName, parentFolder);
            if (!success) {
                logger.cpmsError("Failed to download sample [{}] to [{}]", objectName, parentFolder);
                return false;
            }
        }
        return true;
    }*/

    /*private boolean downloadBaggedFile(String bucket, String objectName, Path outPath) {
        logger.trace("downloadBaggedFile('{}','{}','{}')", bucket, objectName, outPath);
        logger.trace("Checking setup");
        if (!checkSetup(bucket, outPath, true)) {
            logger.cpmsError("Failed bagged download setup verification");
            return false;
        }
        logger.trace("Getting object size");
        HeadObjectResponse s3Object = objectStorage.headObject(bucket, objectName);
        long downloadSize = s3Object.contentLength();
        if (downloadSize < 0L) {
            logger.cpmsError("Object [{}/{}] does not exist or has an invalid size", bucket, objectName);
            return false;
        }
        logger.trace("Getting object uncompressed size tag");
        long uncompressedSize = 2 * downloadSize;
        try {
            if (s3Object.hasMetadata() && s3Object.metadata().containsKey(CPMSStatics.UNCOMPRESSED_SIZE_METADATA_TAG_KEY))
                uncompressedSize = Long.parseLong(s3Object.metadata().get(CPMSStatics.UNCOMPRESSED_SIZE_METADATA_TAG_KEY));
       } catch (NumberFormatException e) {
            logger.warn("[{}/{}] has an invalid 'uncompressedSize' metadata tag [{}], " +
                            "assuming twice the download size to be safe",
                    bucket, objectName, s3Object.metadata().get(CPMSStatics.UNCOMPRESSED_SIZE_METADATA_TAG_KEY));
        }
        logger.trace("Formatting paths");
        int prefix = objectName.lastIndexOf("/") + 1;
        String object = objectName.substring(prefix);
        Path boxedPath = outPath.resolve(object);
        String folder = objectName;
        int suffix = objectName.lastIndexOf(".tar");
        if (suffix == -1)
            suffix = objectName.lastIndexOf(".tgz");
        if (suffix > 0)
            folder = objectName.substring(prefix, suffix);
        Path unboxedPath = outPath.resolve(folder);
        if (Files.exists(unboxedPath)) {
            long existingSize = FileUtils.sizeOfDirectory(unboxedPath.toFile());
            if (uncompressedSize == existingSize) {
                logger.cpmsInfo("Files for [s3://{}/{}] exist at [{}] and are of the correct size, skipping. " +
                                "To re-download, delete existing files and resubmit request.",
                        bucket, objectName, unboxedPath);
                return true;
            } else {
                deleteDirectory(unboxedPath);
            }
        }
        logger.trace("Checking free space");
        try {
            long freeSpace = Files.getFileStore(outPath).getUsableSpace();
            logger.trace("freeSpace: {}", humanReadableByteCount(freeSpace));
            long requiredSpace = downloadSize + uncompressedSize + (1024 * 1024 * 1024);
            logger.trace("requiredSpace: {}", humanReadableByteCount(requiredSpace));
            if (requiredSpace > freeSpace) {
                logger.cpmsError("Not enough free space in [{}], needs [{}] has [{}]",
                        outPath.toAbsolutePath(), humanReadableByteCount(requiredSpace),
                        humanReadableByteCount(freeSpace));
                return false;
            }
        } catch (IOException e) {
            logger.cpmsError("Invalid path provided: {}", outPath);
            return false;
        }
        logger.cpmsInfo("Downloading [{}/{}] to [{}]", bucket, objectName, boxedPath);
        if (!objectStorage.downloadObject(bucket, objectName, outPath) || !Files.exists(boxedPath) ||
                !Files.isRegularFile(boxedPath)) {
            logger.cpmsError("Failed to download [{}/{}] to [{}]", bucket, objectName, boxedPath);
            return false;
        }
        if (archiver.isArchive(boxedPath)) {
            logger.cpmsInfo("Unboxing [{}]", boxedPath);
            if (!archiver.unarchive(boxedPath, outPath) || !Files.exists(unboxedPath) ||
                    !Files.isDirectory(unboxedPath)) {
                logger.cpmsError("Failed to unarchive [{}] to [{}]", boxedPath, unboxedPath);
                return false;
            }
            try {
                Files.deleteIfExists(boxedPath);
            } catch (IOException e) {
                logger.cpmsError("Failed to clean up downloaded object [{}]", boxedPath);
            }
            logger.cpmsInfo("Verifying [{}] using BagIt data", unboxedPath);
            if (!archiver.isBag(unboxedPath) || !archiver.verifyBag(unboxedPath)) {
                logger.cpmsError("[{}] contains missing or invalid BagIt data", unboxedPath);
                return false;
            }
            logger.cpmsInfo("Reverting [{}] to original format", unboxedPath);
            archiver.debagify(unboxedPath);
        }
        return true;
    }*/

    public CPMSLogger getLogger() {
        return logger;
    }

    public void setLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(StorageEngine.class);
    }

    public void updateLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(StorageEngine.class);
    }

    /*
        Private Helper Functions
     */

    private boolean moveToFolder(Path srcFolder, Path dstFolder) {
        try {
            if (!Files.exists(srcFolder)) {
                logger.error("Folder to move [{}] does not exist", srcFolder.toString()
                        .replace("\\", "\\\\"));
                return false;
            }
            if (!Files.exists(dstFolder)) {
                logger.error("Destination folder [{}] does not exist", dstFolder.toString()
                        .replace("\\", "\\\\"));
                return false;
            }
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(srcFolder)) {
                boolean bSuccess = true;
                for (Path path : directoryStream) {
                    if (!movePath(path, dstFolder.resolve(path.getFileName())))
                        bSuccess = false;
                }
                return bSuccess;
            } catch (IOException e) {
                logger.error("Failed to move [{}] to folder [{}] : {}",
                        srcFolder.toString().replace("\\", "\\\\"), dstFolder.toString()
                                .replace("\\", "\\\\"),
                        ExceptionUtils.getStackTrace(e).replace("\\", "\\\\"));
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to move [{}] to folder [{}] : {}",
                    srcFolder.toString().replace("\\", "\\\\"), dstFolder.toString()
                            .replace("\\", "\\\\"),
                    ExceptionUtils.getStackTrace(e).replace("\\", "\\\\"));
            return false;
        }
    }

    private boolean movePath(Path srcPath, Path dstPath) {
        try {
            if (!Files.exists(srcPath)) {
                logger.error("Folder to move [{}] does not exist", srcPath.toString()
                        .replace("\\", "\\\\"));
                return false;
            }
            Files.deleteIfExists(dstPath);
            long started = System.currentTimeMillis();
            Files.move(srcPath, dstPath, ATOMIC_MOVE);
            return true;
        } catch (IOException e) {
            logger.error("Failed to move [{}] to [{}] : {}",
                    srcPath.toString().replace("\\", "\\\\"), dstPath.toString()
                            .replace("\\", "\\\\"),
                    ExceptionUtils.getStackTrace(e).replace("\\", "\\\\"));
            return false;
        }
    }

    /**
     * Deletes an entire folder structure
     * @param folder Path of the folder to delete
     */
    private void deleteDirectory(Path folder) {
        logger.trace("deleteFolder({})", folder);
        try {
            Files.walkFileTree(folder, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.error("Failed to delete directory [{}]", folder);
        }
    }

    /**
     * Formats bytecount into human-readable format
     * @param bytes The number of bytes
     * @return human-readable formatted String
     */
    private static String humanReadableByteCount(long bytes) {
        int unit = 1000;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (/*si ? */"kMGTPE"/* : "KMGTPE"*/).charAt(exp-1) + (/*si ? */""/* : "i"*/);
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
