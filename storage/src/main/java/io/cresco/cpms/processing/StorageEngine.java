package io.cresco.cpms.processing;

import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;
import io.cresco.cpms.scripting.StorageTask;
import io.cresco.cpms.storage.encapsulation.Archiver;
import io.cresco.cpms.storage.encapsulation.ArchiverBuilder;
import io.cresco.cpms.storage.transfer.*;
import io.cresco.cpms.storage.utilities.StorageParameters;
import io.cresco.cpms.storage.utilities.StorageProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

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

    /*
        Main Methods
     */

    /**
     * Executes a supplied storage job
     * @param storageTask The storage job script to execute
     * @return A StorageTaskResult object with information about the success of the job execution
     * @throws ExecutionException If there was a failure in job execution
     */
    public StorageTaskResult runStorageJob(StorageTask storageTask) throws ExecutionException {
        logger.debug("runStorageJob({})", storageTask);
        if (storageTask == null) {
            logger.cpmsError("Submitted storage job cannot be null");
            return new StorageTaskResultBuilder().withSuccess(false).build();
        }
        switch(storageTask.getAction()) {
            case "list": {
                logger.info("List task");
                StorageParameters sourceStorageParameters = new StorageParameters(storageTask.getSourcePath());
                logger.trace("Source Storage Provider: {}", sourceStorageParameters.storageProvider);
                logger.trace("Source Container: {}", sourceStorageParameters.container);
                logger.trace("Source Prefix: {}", sourceStorageParameters.prefix);
                TransferAdapter transferAdapter;
                if (sourceStorageParameters.storageProvider == StorageProvider.AWS) {
                    transferAdapter = new S3ObjectStorageBuilder().withLogger(logger).build();
                } else if (sourceStorageParameters.storageProvider == StorageProvider.Azure) {
                    transferAdapter = new AzureBlobStorageBuilder().withLogger(logger).build();
                } else {
                    logger.error("Storage provider [{}] is not implemented yet!",
                            sourceStorageParameters.storageProvider.name());
                    return new StorageTaskResultBuilder().withSuccess(false).withSourcePath(storageTask.getSourcePath())
                            .build();
                }
                if (sourceStorageParameters.prefix != null) {
                    transferAdapter.listObjectsInContainer(sourceStorageParameters.container,
                            sourceStorageParameters.prefix).forEach(System.out::println);
                } else if (sourceStorageParameters.container != null) {
                    transferAdapter.listObjectsInContainer(sourceStorageParameters.container)
                            .forEach(System.out::println);
                } else {
                    transferAdapter.listTopLevelContainers().forEach(System.out::println);
                }
                return new StorageTaskResultBuilder().withSuccess(true).withSourcePath(storageTask.getSourcePath())
                        .build();
            }
            case "upload": {
                logger.info("Upload task");
                if (storageTask.getSourcePath() == null || !Files.exists(Paths.get(storageTask.getSourcePath()))) {
                    logger.cpmsError("Source path to upload [{}] does not exist", storageTask.getSourcePath());
                    return new StorageTaskResultBuilder().withSuccess(false).build();
                }
                if (storageTask.getDestinationPath() == null || storageTask.getDestinationPath().isEmpty()) {
                    logger.cpmsError("Destination path to upload cannot be empty");
                    return new StorageTaskResultBuilder().withSuccess(false).withSourcePath(storageTask.getSourcePath())
                            .build();
                }
                StorageParameters sourceStorageParameters = new StorageParameters(storageTask.getSourcePath());
                logger.trace("Source Path: {}", storageTask.getSourcePath());
                logger.trace("Source Storage Provider: {}", sourceStorageParameters.storageProvider);
                StorageParameters destinationStorageParameters = new StorageParameters(storageTask.getDestinationPath());
                logger.trace("Destination Path: {}", storageTask.getDestinationPath());
                logger.trace("Destination Storage Provider: {}", destinationStorageParameters.storageProvider);
                logger.trace("Destination Container: {}", destinationStorageParameters.container);
                logger.trace("Destination Prefix: {}", destinationStorageParameters.prefix);
                String destinationKey = "";
                if (destinationStorageParameters.prefix != null && !destinationStorageParameters.prefix.isEmpty())
                    destinationKey += destinationStorageParameters.prefix + "/";
                destinationKey += sourceStorageParameters.path.getFileName();
                logger.trace("Destination Key: {}",  destinationKey);
                // Todo: If archiving and/or compression is needed, perform here to pass the new path to the
                //  TransferAdapter
                TransferAdapter transferAdapter;
                if (destinationStorageParameters.storageProvider == StorageProvider.AWS) {
                    transferAdapter = new S3ObjectStorageBuilder().withLogger(logger).build();
                } else if (destinationStorageParameters.storageProvider == StorageProvider.Azure) {
                    transferAdapter = new AzureBlobStorageBuilder().withLogger(logger).build();
                } else {
                    logger.error("Storage provider [{}] is not implemented yet!",
                            destinationStorageParameters.storageProvider.name());
                    return new StorageTaskResultBuilder().withSuccess(false).withSourcePath(storageTask.getSourcePath())
                            .build();
                }
                try {
                    if (transferAdapter.uploadFile(sourceStorageParameters.path, destinationStorageParameters.container,
                            destinationKey)) {
                        return new StorageTaskResultBuilder()
                                .withSuccess(true)
                                .withSourcePath(storageTask.getSourcePath())
                                .withDestinationPath(storageTask.getDestinationPath())
                                .build();
                    } else {
                        logger.error("Failed to upload file!");
                        return new StorageTaskResultBuilder()
                                .withSuccess(false)
                                .withSourcePath(storageTask.getSourcePath())
                                .withDestinationPath(storageTask.getDestinationPath())
                                .withErrorMessage("Failed to upload file!")
                                .build();
                    }
                } catch (IOException e) {
                    logger.error("Failed to upload file due to IOException!");
                    return new StorageTaskResultBuilder()
                            .withSuccess(false)
                            .withSourcePath(storageTask.getSourcePath())
                            .withDestinationPath(storageTask.getDestinationPath())
                            .withErrorMessage(e.getMessage())
                            .build();
                }
            }
            case "download": {
                logger.info("Download task");
                if (storageTask.getSourcePath() == null || storageTask.getSourcePath().isEmpty()) {
                    logger.cpmsError("Source path to download [{}] cannot be empty", storageTask.getSourcePath());
                    return new StorageTaskResultBuilder().withSuccess(false).build();
                }
                if (storageTask.getDestinationPath() == null || storageTask.getDestinationPath().isEmpty()) {
                    logger.cpmsError("Destination path to upload cannot be empty");
                    return new StorageTaskResultBuilder().withSuccess(false).withSourcePath(storageTask.getSourcePath())
                            .build();
                }
                StorageParameters sourceStorageParameters = new StorageParameters(storageTask.getSourcePath());
                logger.trace("Source Path: {}", storageTask.getSourcePath());
                logger.trace("Source Storage Provider: {}", sourceStorageParameters.storageProvider);
                logger.trace("Source Container: {}", sourceStorageParameters.container);
                logger.trace("Source Prefix: {}", sourceStorageParameters.prefix);
                TransferAdapter transferAdapter;
                if (sourceStorageParameters.storageProvider == StorageProvider.AWS) {
                    transferAdapter = new S3ObjectStorageBuilder().withLogger(logger).build();
                } else if (sourceStorageParameters.storageProvider == StorageProvider.Azure) {
                    transferAdapter = new AzureBlobStorageBuilder().withLogger(logger).build();
                } else {
                    logger.error("Storage provider [{}] is not implemented yet!",
                            sourceStorageParameters.storageProvider.name());
                    return new StorageTaskResultBuilder().withSuccess(false).build();
                }
                StorageParameters destinationStorageParameters = new StorageParameters(storageTask.getDestinationPath());
                logger.trace("Source Path: {}", storageTask.getDestinationPath());
                logger.trace("Source Storage Provider: {}", destinationStorageParameters.storageProvider);
                try {
                    Path finalDestinationPath = transferAdapter.downloadObject(sourceStorageParameters.container, 
                            sourceStorageParameters.prefix, destinationStorageParameters.path);
                    if (finalDestinationPath != null) {
                        Archiver archiver = new ArchiverBuilder().withLogger(logger).build();
                        if (archiver.isArchive(finalDestinationPath)) {
                            String folder = finalDestinationPath.toString();
                            int suffix = folder.lastIndexOf(".tar");
                            if (suffix != -1)
                                suffix = finalDestinationPath.toString().lastIndexOf(".tgz");
                            if (suffix > 0)
                                folder = finalDestinationPath.toString().substring(suffix);
                            Path finalDestinationFolder = destinationStorageParameters.path.resolve(folder);
                            logger.cpmsInfo("Unboxing [{}] to [{}]",  finalDestinationPath,
                                    finalDestinationFolder);
                            if (!archiver.unarchive(finalDestinationPath, destinationStorageParameters.path) || 
                                    !Files.exists(finalDestinationFolder)) {
                                logger.cpmsError("Failed to unbox [{}] to [{}]",  finalDestinationPath,
                                        finalDestinationFolder);
                                return new StorageTaskResultBuilder()
                                        .withSuccess(false)
                                        .withSourcePath(storageTask.getSourcePath())
                                        .withDestinationPath(storageTask.getDestinationPath())
                                        .withErrorMessage(String.format("Failed to unbox [%s] to [%s]",
                                                finalDestinationPath, finalDestinationFolder))
                                        .build();
                            }
                            try {
                                Files.deleteIfExists(finalDestinationPath);
                            } catch (IOException e) {
                                logger.cpmsError("Failed to clean up downloaded object [{}]", finalDestinationPath);
                                return new StorageTaskResultBuilder()
                                        .withSuccess(false)
                                        .withSourcePath(storageTask.getSourcePath())
                                        .withDestinationPath(storageTask.getDestinationPath())
                                        .withErrorMessage(String.format("Failed to clean up downloaded object [%s]",
                                                finalDestinationPath))
                                        .build();
                            }
                            if (archiver.isBag(finalDestinationFolder)) {
                                logger.cpmsInfo("Verifying [{}] using BagIt data",  finalDestinationFolder);
                                if (!archiver.verifyBag(finalDestinationFolder)) {
                                    logger.cpmsError("Failed to verify [{}] using BagIt data", finalDestinationFolder);
                                    return new StorageTaskResultBuilder()
                                            .withSuccess(false)
                                            .withSourcePath(storageTask.getSourcePath())
                                            .withDestinationPath(storageTask.getDestinationPath())
                                            .withErrorMessage(String.format("Failed to verify [%s] using BagIt data",
                                                            finalDestinationFolder))
                                            .build();
                                }
                                logger.cpmsInfo("Reverting [{}] to original format",  finalDestinationFolder);
                                archiver.debagify(finalDestinationFolder);
                            }
                            finalDestinationPath = finalDestinationFolder;
                        }
                        return new StorageTaskResultBuilder()
                                .withSuccess(true)
                                .withSourcePath(storageTask.getSourcePath())
                                .withDestinationPath(finalDestinationPath.toAbsolutePath().toString())
                                .build();
                    } else {
                        logger.error("Failed to download file!");
                        return new StorageTaskResultBuilder()
                                .withSuccess(false)
                                .withSourcePath(storageTask.getSourcePath())
                                .withDestinationPath(storageTask.getDestinationPath())
                                .withErrorMessage("Failed to download file!")
                                .build();
                    }
                } catch (IOException e) {
                    logger.error("Failed to download file due to IOException!");
                    return new StorageTaskResultBuilder()
                            .withSuccess(false)
                            .withSourcePath(storageTask.getSourcePath())
                            .withDestinationPath(storageTask.getDestinationPath())
                            .withErrorMessage(e.getMessage())
                            .build();
                }
            }
            case "delete": {
                logger.info("Delete task");
                // Todo: Implement or delete this section
                return new StorageTaskResultBuilder().withSuccess(true).build();
            }
            default: {
                logger.cpmsError("An invalid StorageJob type [{}] was encountered", storageTask.getAction());
                return new StorageTaskResultBuilder()
                        .withSuccess(false)
                        .withErrorMessage(String.format("An invalid StorageJob type [%s] was encountered", storageTask.getAction()))
                        .build();
            }
        }
    }

    private boolean uploadBaggedDirectoryToS3(Path localPath, String bucket, String prefix) {
        logger.trace("uploadBaggedDirectoryToS3('{}','{}','{}')", localPath, bucket, prefix);
        try {
            Archiver archiver = new ArchiverBuilder().withLogger(logger).build();
            S3ObjectStorage objectStorage = new S3ObjectStorageBuilder().withLogger(logger).build();
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
