package io.cresco.cpms.processing;

import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;
import io.cresco.cpms.scripting.StorageTask;
import io.cresco.cpms.statics.BagItType;
import io.cresco.cpms.storage.encapsulation.Archiver;
import io.cresco.cpms.storage.encapsulation.ArchiverBuilder;
import io.cresco.cpms.storage.transfer.*;
import io.cresco.cpms.storage.utilities.StorageParameters;
import io.cresco.cpms.storage.utilities.StorageProvider;

import java.io.IOException;
import java.nio.file.*;

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
                logger.trace("Source Storage Provider: {}", sourceStorageParameters.getStorageProvider());
                logger.trace("Source Container: {}", sourceStorageParameters.getContainer());
                logger.trace("Source Prefix: {}", sourceStorageParameters.getPrefix());
                TransferPath sourceTransferPath = sourceStorageParameters.getTransferPath();
                TransferAdapter transferAdapter;
                if (sourceStorageParameters.getStorageProvider() == StorageProvider.AWS) {
                    transferAdapter = new S3ObjectStorageBuilder().withLogger(logger).build();
                } else if (sourceStorageParameters.getStorageProvider() == StorageProvider.Azure) {
                    transferAdapter = new AzureBlobStorageBuilder().withLogger(logger).build();
                } else if (sourceStorageParameters.getStorageProvider() == StorageProvider.local) {
                    transferAdapter = new FileSystemStorageBuilder().withLogger(logger).build();
                } else {
                    logger.error("Storage provider [{}] is not implemented yet!",
                            sourceStorageParameters.getStorageProvider().name());
                    return new StorageTaskResultBuilder().withSuccess(false).withSourcePath(storageTask.getSourcePath())
                            .build();
                }
                transferAdapter.listFilesInPath(sourceTransferPath).forEach(System.out::println);
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
                logger.trace("Source Storage Provider: {}", sourceStorageParameters.getStorageProvider());
                StorageParameters destinationStorageParameters = new StorageParameters(storageTask.getDestinationPath());
                logger.trace("Destination Path: {}", storageTask.getDestinationPath());
                logger.trace("Destination Storage Provider: {}",
                        destinationStorageParameters.getStorageProvider());
                logger.trace("Destination Container: {}", destinationStorageParameters.getContainer());
                logger.trace("Destination Prefix: {}", destinationStorageParameters.getPrefix());
                logger.trace("Destination Archiving: {}", storageTask.getDestinationArchiving());
                logger.trace("Destination Hashing: {}", storageTask.getDestinationHashing());
                logger.trace("Destination Hidden Files: {}", storageTask.getDestinationHiddenFiles());
                logger.trace("Destination Compression: {}", storageTask.getDestinationCompression());
                String destinationKey = "";
                if (destinationStorageParameters.getPrefix() != null &&
                        !destinationStorageParameters.getPrefix().isEmpty())
                    destinationKey += destinationStorageParameters.getPrefix() + "/";
                Path localWorkingPath = sourceStorageParameters.getPath();
                if (Files.isDirectory(localWorkingPath)) {
                    Archiver archiver = new ArchiverBuilder()
                            .withBagItType(storageTask.getDestinationArchiving())
                            .withBagItHashingAlgorithm(storageTask.getDestinationHashing())
                            .withBagItHiddenfiles(storageTask.getDestinationHiddenFiles())
                            .withArchiveCompression(storageTask.getDestinationCompression())
                            .build();
                    if (storageTask.getDestinationArchiving() != null &&
                            !storageTask.getDestinationArchiving().equals(BagItType.None)) {
                        logger.cpmsInfo("Archiving directory [{}]", localWorkingPath);
                        localWorkingPath = archiver.bagItUp(localWorkingPath).getFileName();
                        if (localWorkingPath == null || !Files.exists(localWorkingPath)) {
                            logger.cpmsError("Failed to archive directory [{}]",
                                    sourceStorageParameters.getPath());
                            return new StorageTaskResultBuilder()
                                    .withSuccess(false)
                                    .withSourcePath(storageTask.getSourcePath())
                                    .withDestinationPath(storageTask.getDestinationPath())
                                    .withErrorMessage("Failed to archive directory!")
                                    .build();
                        }
                        logger.cpmsInfo("Verifying archived directory [{}]", localWorkingPath);
                        if (!archiver.verifyBag(localWorkingPath)) {
                            logger.cpmsError("Failed to verify archived directory [{}]",
                                    sourceStorageParameters.getPath());
                            return new StorageTaskResultBuilder()
                                    .withSuccess(false)
                                    .withSourcePath(storageTask.getSourcePath())
                                    .withDestinationPath(storageTask.getDestinationPath())
                                    .withErrorMessage("Failed to verify directory archiving!")
                                    .build();
                        }
                    }
                    if (storageTask.getDestinationCompression() != null &&
                            !storageTask.getDestinationArchiving().equals(BagItType.None)) {
                        logger.cpmsInfo("Compressing directory [{}]", localWorkingPath);
                        localWorkingPath = archiver.archive(localWorkingPath.toFile());
                        if (localWorkingPath == null || !Files.exists(localWorkingPath)) {
                            logger.cpmsError("Failed to compress directory [{}]",
                                    sourceStorageParameters.getPath());
                            return new StorageTaskResultBuilder()
                                    .withSuccess(false)
                                    .withSourcePath(storageTask.getSourcePath())
                                    .withDestinationPath(storageTask.getDestinationPath())
                                    .withErrorMessage("Failed to compress directory!")
                                    .build();
                        }
                    }
                    if (storageTask.getDestinationArchiving() != null) {
                        logger.cpmsInfo("Reverting archiving on directory [{}]", localWorkingPath);
                        archiver.debagify(sourceStorageParameters.getPath());
                    }
                }
                destinationKey += localWorkingPath.getFileName();
                logger.trace("Destination Key: {}",  destinationKey);
                String finalDestinationKey;
                TransferPath destinationTransferPath = new TransferPath(destinationStorageParameters.getContainer(),
                        destinationKey);
                TransferAdapter transferAdapter;
                if (destinationStorageParameters.getStorageProvider() == StorageProvider.AWS) {
                    transferAdapter = new S3ObjectStorageBuilder().withLogger(logger).build();
                    finalDestinationKey = StorageParameters.AWS_PREFIX;
                } else if (destinationStorageParameters.getStorageProvider() == StorageProvider.Azure) {
                    transferAdapter = new AzureBlobStorageBuilder().withLogger(logger).build();
                    finalDestinationKey = StorageParameters.AZURE_PREFIX;
                } else if (destinationStorageParameters.getStorageProvider() == StorageProvider.local) {
                    transferAdapter = new FileSystemStorageBuilder().withLogger(logger).build();
                    finalDestinationKey = "";
                } else {
                    logger.error("Storage provider [{}] is not implemented yet!",
                            destinationStorageParameters.getStorageProvider().name());
                    return new StorageTaskResultBuilder()
                            .withSuccess(false)
                            .withSourcePath(storageTask.getSourcePath())
                            .build();
                }
                try {
                    if (transferAdapter.uploadFile(localWorkingPath, destinationTransferPath)) {
                        if (destinationStorageParameters.getContainer() != null)
                            finalDestinationKey += destinationStorageParameters.getContainer() +
                                StorageParameters.CLOUD_PATH_SEPARATOR;
                        finalDestinationKey += destinationKey;
                        logger.trace("Final Destination Key: {}",  finalDestinationKey);
                        return new StorageTaskResultBuilder()
                                .withSuccess(true)
                                .withSourcePath(storageTask.getSourcePath())
                                .withDestinationPath(finalDestinationKey)
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
                logger.trace("Source Storage Provider: {}", sourceStorageParameters.getStorageProvider());
                logger.trace("Source Container: {}", sourceStorageParameters.getContainer());
                logger.trace("Source Prefix: {}", sourceStorageParameters.getPrefix());
                TransferPath sourceTransferPath = sourceStorageParameters.getTransferPath();
                TransferAdapter transferAdapter;
                if (sourceStorageParameters.getStorageProvider() == StorageProvider.AWS) {
                    transferAdapter = new S3ObjectStorageBuilder().withLogger(logger).build();
                } else if (sourceStorageParameters.getStorageProvider() == StorageProvider.Azure) {
                    transferAdapter = new AzureBlobStorageBuilder().withLogger(logger).build();
                } else if (sourceStorageParameters.getStorageProvider() == StorageProvider.local) {
                    transferAdapter = new FileSystemStorageBuilder().withLogger(logger).build();
                } else {
                    logger.error("Storage provider [{}] is not implemented yet!",
                            sourceStorageParameters.getStorageProvider().name());
                    return new StorageTaskResultBuilder().withSuccess(false).build();
                }
                StorageParameters destinationStorageParameters = new StorageParameters(storageTask.getDestinationPath());
                logger.trace("Source Path: {}", storageTask.getDestinationPath());
                logger.trace("Source Storage Provider: {}", destinationStorageParameters.getStorageProvider());
                try {
                    Path finalDestinationPath = transferAdapter.downloadFile(sourceTransferPath, destinationStorageParameters.getPath());
                    if (finalDestinationPath != null) {
                        Archiver archiver = new ArchiverBuilder().withLogger(logger).build();
                        if (archiver.isArchive(finalDestinationPath)) {
                            String folder = finalDestinationPath.toString();
                            int suffix = folder.lastIndexOf(".tar");
                            if (suffix != -1)
                                suffix = finalDestinationPath.toString().lastIndexOf(".tgz");
                            if (suffix > 0)
                                folder = finalDestinationPath.toString().substring(suffix);
                            Path finalDestinationFolder = destinationStorageParameters.getPath().resolve(folder);
                            logger.cpmsInfo("Unboxing [{}] to [{}]",  finalDestinationPath,
                                    finalDestinationFolder);
                            if (!archiver.unarchive(finalDestinationPath, destinationStorageParameters.getPath()) ||
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
                                logger.cpmsError("Failed to clean up downloaded object [{}]",
                                        finalDestinationPath);
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
            default: {
                logger.cpmsError("An invalid StorageJob type [{}] was encountered", storageTask.getAction());
                return new StorageTaskResultBuilder()
                        .withSuccess(false)
                        .withErrorMessage(String.format("An invalid StorageJob type [%s] was encountered",
                                storageTask.getAction()))
                        .build();
            }
        }
    }

    public CPMSLogger getLogger() {
        return logger;
    }

    public void setLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(StorageEngine.class);
    }

    public void updateLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(StorageEngine.class);
    }
}
