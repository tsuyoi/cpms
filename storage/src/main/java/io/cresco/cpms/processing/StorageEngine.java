package io.cresco.cpms.processing;

import io.cresco.cpms.logging.BasicCPMSLogger;
import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;
import io.cresco.cpms.scripting.StorageTask;
import io.cresco.cpms.statics.CPMSStatics;
import io.cresco.cpms.storage.encapsulation.Archiver;
import io.cresco.cpms.storage.transfer.ObjectStorage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

@SuppressWarnings({"unused", "WeakerAccess", "BooleanMethodIsAlwaysInverted", "SameParameterValue"})
public class StorageEngine {
    private CPMSLogger logger;
    private Archiver archiver;
    private ObjectStorage objectStorage;

    /*
        Constructors
     */

    public StorageEngine(ObjectStorage objectStorage, Archiver archiver) {
        this(objectStorage, archiver, new BasicCPMSLoggerBuilder().withClass(StorageEngine.class).build());
    }

    public StorageEngine(ObjectStorage objectStorage, Archiver archiver,
                         CPMSLogger logger) throws IllegalArgumentException {
        setLogger(logger);
        setObjectStorage(objectStorage);
        setArchiver(archiver);
    }

    public boolean runStorageJob(StorageTask storageTask) {
        if (storageTask == null) {
            logger.cpmsError("Submitted storage job cannot be null");
            return false;
        }
        if (!objectStorage.doesBucketExist(storageTask.getS3Bucket())) {
            logger.cpmsError("S3 bucket [{}] is not accessible from this account",
                    storageTask.getS3Bucket());
            return false;
        }
        switch(storageTask.getAction()) {
            case "upload":
                if (!Files.exists(storageTask.getLocalPath())) {
                    logger.cpmsError("Local path to upload [{}] does not exist", storageTask.getLocalPath());
                    return false;
                }
                if (Files.isDirectory(storageTask.getLocalPath()))
                    return uploadBaggedDirectory(storageTask.getLocalPath(), storageTask.getS3Bucket(),
                            storageTask.getS3Path());
                else
                    return uploadSingleFile(storageTask.getLocalPath(), storageTask.getS3Bucket(),
                            storageTask.getS3Path());
            case "download":
                if (!storageTask.getS3Path().endsWith("/")) {
                    if (!objectStorage.doesObjectExist(storageTask.getS3Bucket(), storageTask.getS3Path())) {
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
                            storageTask.getLocalPath());
            case "delete":
                if (storageTask.getS3Path().endsWith("/")) {
                    return objectStorage.deleteBucketContents(storageTask.getS3Bucket(), storageTask.getS3Path());
                } else {
                    if (!objectStorage.doesObjectExist(storageTask.getS3Bucket(), storageTask.getS3Path())) {
                        logger.cpmsError("S3 object [{}/{}] does not exist to download",
                                storageTask.getS3Bucket(), storageTask.getS3Path());
                        return false;
                    }
                    return objectStorage.deleteBucketObject(storageTask.getS3Bucket(), storageTask.getS3Path());
                }
            default:
                logger.cpmsError("An invalid StorageJob type [{}] was encountered", storageTask.getAction());
                return false;
        }
    }

    /*
        Public Instance Methods
     */

    private boolean uploadSingleFile(Path inPath, String bucket, String s3Prefix) {
        logger.trace("uploadSingleFile({},{},{})", inPath, bucket, s3Prefix);
        if (!checkSetup(bucket, inPath, false)) {
            logger.cpmsError("Failed bagged upload setup verification");
            return false;
        }
        if (s3Prefix == null) {
            logger.cpmsError("No s3 prefix supplied for single file upload");
            return false;
        }
        long uncompressedSize = FileUtils.sizeOf(inPath.toFile());
        logger.cpmsInfo("Uploading [{}] to [{}/{}]", inPath, bucket, s3Prefix);
        try {
            return objectStorage.uploadFile(inPath, bucket, s3Prefix, uncompressedSize, false);
        } catch (IOException e) {
            logger.cpmsError("Failed to upload file [{}] to S3 [{}/{}]", inPath, bucket, s3Prefix);
            logger.debug("IOException: {}", ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    private boolean uploadBaggedDirectory(Path inPath, String bucket, String s3Prefix) {
        logger.trace("uploadBaggedDirectory('{}','{}','{}')", inPath, bucket, s3Prefix);
        if (!checkSetup(bucket, inPath, true)) {
            logger.cpmsError("Failed bagged upload setup verification");
            return false;
        }
        if (s3Prefix == null) {
            logger.cpmsError("No S3 prefix supplied for bagged upload");
            return false;
        }
        long uncompressedSize = FileUtils.sizeOfDirectory(inPath.toFile());
        logger.trace("uncompressedSize: {}", humanReadableByteCount(uncompressedSize));
        try {
            long freeSpace = Files.getFileStore(inPath).getUsableSpace();
            logger.trace("freeSpace: {}", humanReadableByteCount(freeSpace));
            long requiredSpace = uncompressedSize + (1024 * 1024 * 1024);
            logger.trace("requiredSpace: {}", humanReadableByteCount(requiredSpace));
            if (requiredSpace > freeSpace) {
                logger.cpmsError("Not enough free space to bag up [{}], needs [{}] has [{}]",
                                inPath.toAbsolutePath(), humanReadableByteCount(requiredSpace),
                                humanReadableByteCount(freeSpace));
                return false;
            }
        } catch (IOException e) {
            logger.cpmsError("Failed to locate path to upload [{}]", inPath);
            return false;
        }
        logger.cpmsInfo("Bagging up [{}]", inPath);
        Path bagged = archiver.bagItUp(inPath);
        if (bagged == null || !Files.exists(bagged)) {
            logger.cpmsError("Failed to bag up directory [{}]", inPath);
            return false;
        }
        logger.cpmsInfo("Verifying bagging on [{}]", inPath);
        if (!archiver.verifyBag(inPath)) {
            logger.cpmsError("Failed to bag up directory [{}]", inPath);
            archiver.debagify(inPath);
            return false;
        }
        logger.cpmsInfo("Boxing up [{}]", inPath.toAbsolutePath());
        Path boxed = archiver.archive(bagged.toFile());
        logger.cpmsInfo("Reverting bagging on directory [{}]", inPath);
        archiver.debagify(inPath);
        if (boxed == null || !Files.exists(boxed)) {
            logger.cpmsError("Failed to box up directory [{}]", inPath);
            return false;
        }
        logger.cpmsInfo("Uploading [{}] to [{}/{}]", boxed, bucket, s3Prefix);
        boolean success = false;
        try {
            success = objectStorage.uploadFile(boxed, bucket, s3Prefix, uncompressedSize, false);
            Files.delete(boxed);
            return success;
        } catch (IOException e) {
            logger.cpmsError("Failed to upload file [{}] to S3 [{}/{}]", boxed, bucket, s3Prefix);
            logger.debug("IOException: {}", ExceptionUtils.getStackTrace(e));
            return success;
        }
    }

    private boolean downloadDirectoryOfBaggedFiles(String bucket, String s3Prefix, Path outPath) {
        logger.trace("downloadDirectoryOfBaggedFiles('{}','{}','{}')", bucket, s3Prefix, outPath);
        if (!checkSetup(bucket, outPath, true)) {
            logger.cpmsError("Failed bagged download setup verification");
            return false;
        }
        if (!s3Prefix.endsWith("/"))
            s3Prefix += "/";
        long downloadSize = 0L;
        long largestObject = 0L;
        for (Map.Entry<String, Long> object : objectStorage.listBucketObjectsWithSize(bucket, s3Prefix).entrySet()) {
            downloadSize += object.getValue();
            if (object.getValue() > largestObject)
                largestObject = object.getValue();
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
        for (String objectName : objectStorage.listBucketObjects(bucket, s3Prefix)) {
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
    }

    private boolean downloadBaggedFile(String bucket, String objectName, Path outPath) {
        logger.trace("downloadBaggedFile('{}','{}','{}')", bucket, objectName, outPath);
        logger.trace("Checking setup");
        if (!checkSetup(bucket, outPath, true)) {
            logger.cpmsError("Failed bagged download setup verification");
            return false;
        }
        logger.trace("Getting object size");
        long downloadSize = objectStorage.getObjectSize(bucket, objectName);
        if (downloadSize < 0L) {
            logger.cpmsError("Object [{}/{}] does not exist or has an invalid size", bucket, objectName);
            return false;
        }
        logger.trace("Getting object uncompressed size tag");
        long uncompressedSize = 2 * downloadSize;
        String uncompressedSizeString = objectStorage.getObjectTag(bucket, objectName,
                CPMSStatics.UNCOMPRESSED_SIZE_METADATA_TAG_KEY);
        if (uncompressedSizeString == null)
            logger.warn("[{}/{}] is missing the 'uncompressedSize' metadata tag, " +
                            "assuming twice the download size to be safe", bucket, objectName);
        else {
            try {
                uncompressedSize = Long.parseLong(uncompressedSizeString);
            } catch (NumberFormatException e) {
                logger.warn("[{}/{}] has an invalid 'uncompressedSize' metadata tag [{}], " +
                                "assuming twice the download size to be safe",
                        bucket, objectName, uncompressedSizeString);
            }
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
    }

    public CPMSLogger getLogger() {
        return logger;
    }

    public void setLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(StorageEngine.class);
    }

    public void updateLogger(CPMSLogger logger) {
        // Todo: Rebuild this section
        /*this.logger.setFlowCellID(logger.getFlowCellID());
        this.logger.setSampleID(logger.getSampleID());
        this.logger.setRequestID(logger.getRequestID());
        this.logger.setStage(logger.getStage());
        this.logger.setStep(logger.getStep());*/
        this.objectStorage.updateLogger(logger);
        this.archiver.updateLogger(logger);
    }

    public ObjectStorage getObjectStorage() {
        return objectStorage;
    }
    public void setObjectStorage(ObjectStorage objectStorage) throws IllegalArgumentException {
        if (objectStorage == null)
            throw new IllegalArgumentException("ObjectStorage instance cannot be null");
        this.objectStorage = objectStorage;
    }

    public Archiver getArchiver() {
        return archiver;
    }
    public void setArchiver(Archiver archiver) {
        if (archiver == null)
            throw new IllegalArgumentException("Archiver instance cannot be null");
        this.archiver = archiver;
    }

    /*
        Private Helper Functions
     */

    private boolean checkSetup(String bucket, Path path, boolean isDirectory) {
        logger.trace("checkSetup('{}','{}')", bucket, path);
        if (archiver == null) {
            logger.error("You must configure your archival settings prior to use");
            return false;
        }
        if (objectStorage == null) {
            logger.error("You must configure your object storage settings prior to use");
            return false;
        }
        if (bucket == null || bucket.equals("") || !objectStorage.doesBucketExist(bucket)) {
            logger.error("You must supply a valid bucket name");
            return false;
        }
        if (path == null) {
            logger.error("You must supply a valid path to work with");
            return false;
        }
        if (!Files.exists(path)) {
            logger.error("Path [{}] does not exist", path);
            return false;
        }
        if (isDirectory && !Files.isDirectory(path)) {
            logger.error("Path [{}] is not a directory", path);
            return false;
        }
        if (!isDirectory && !Files.isRegularFile(path)) {
            logger.error("Path [{}] is not a regular file", path);
            return false;
        }
        return true;
    }

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
            Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
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
