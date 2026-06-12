package io.cresco.cpms.storage.transfer;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.util.ProgressListener;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.*;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import io.cresco.cpms.logging.CPMSLogger;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

public class AzureBlobStorage implements TransferAdapter {
    private final String endpoint;
    private final TokenCredential tokenCredential;

    private CPMSLogger logger;

    /*
        Constructors
     */

    public AzureBlobStorage(AzureBlobStorageBuilder builder) {
        this.endpoint = builder.getEndpoint();
        this.tokenCredential = builder.getTokenCredential();
        setLogger(builder.getLogger());
    }

    private BlobServiceClient getBlobServiceClient() {
        return new BlobServiceClientBuilder()
                .endpoint(endpoint)
                .credential(tokenCredential)
                .buildClient();
    }

    /*
        Internal Azure-Specific Methods to Wrap
     */

    private List<BlobContainerItem> listContainers() {
        logger.debug("listContainers()");
        return getBlobServiceClient().listBlobContainers().stream().toList();
    }

    private boolean doesBlobContainerExist(String container) {
        logger.debug("doesContainerExist({})", container);
        return getBlobServiceClient().getBlobContainerClient(container).exists();
    }

    private boolean createContainer(String container) {
        logger.debug("createContainer({})", container);
        return getBlobServiceClient().getBlobContainerClient(container).createIfNotExists();
    }

    private boolean doesBlobExist(String container, String key) {
        logger.debug("doesBlobItemExist({}, {})", container, key);
        if (!doesBlobContainerExist(container))
            return false;
        return getBlobServiceClient().getBlobContainerClient(container).getBlobClient(key).exists();
    }

    private List<BlobItem> listContainerBlobs(String container) {
        logger.debug("listContainerBlobs({})", container);
        return getBlobServiceClient().getBlobContainerClient(container).listBlobs().stream()
                .collect(Collectors.toList());
    }

    private List<BlobItem> listContainerBlobs(String container, String prefix) {
        logger.debug("listContainerBlobs({}, {})", container, prefix);
        ListBlobsOptions listBlobsOptions = new ListBlobsOptions().setPrefix(prefix);
        return getBlobServiceClient().getBlobContainerClient(container).listBlobs(listBlobsOptions, null)
                .stream().collect(Collectors.toList());
    }

    private boolean uploadFileToBlob(Path uploadPath, String container, String key) throws IOException {
        logger.debug("uploadFileToBlob({}, {}, {})", uploadPath, container, key);
        if (!Files.exists(uploadPath))
            throw new IOException("file to upload does not exist");
        if (!doesContainerExist(container))
            throw new IOException("target container does not exist");
        logger.debug("Computing MD5 checksum of file to upload");
        String localChecksum;
        try (InputStream in = new FileInputStream(uploadPath.toFile())) {
            localChecksum = DigestUtils.md5Hex(in);
        } catch (Exception e) {
            logger.error("Failed to compute the local MD5 checksum of file to upload: {}", e);
            return false;
        }
        logger.trace("Local MD5 checksum: {}", localChecksum);
        BlobClient blobClient = getBlobServiceClient()
                .getBlobContainerClient(container)
                .getBlobClient(key);
        CrescoAzureLoggingTransferListener listener = new CrescoAzureLoggingTransferListener(logger,
                uploadPath.toFile().length());
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setProgressListener(listener);
        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(uploadPath.toFile()))) {
            BlobParallelUploadOptions uploadOptions = new BlobParallelUploadOptions(stream)
                    .setParallelTransferOptions(parallelTransferOptions);
            logger.cpmsInfo("Starting upload of {} to {}/{}", uploadPath, container, key);
            Response<BlockBlobItem> response = blobClient.uploadWithResponse(uploadOptions, null, null);
            String azureChecksum = HexFormat.of().formatHex(response.getValue().getContentMd5());
            logger.trace("Response getContentMd5: {}", azureChecksum);
            return azureChecksum.equals(localChecksum);
        } catch (Exception e) {
            logger.cpmsError("Failed to upload file: {}", e.getMessage());
            return false;
        }
    }

    private boolean downloadBlobToFile(String container, String key, Path destinationDirectory) throws IOException {
        logger.debug("downloadBlob({}, {}, {})", container, key, destinationDirectory);
        if (!doesContainerExist(container))
            throw new IOException("target container does not exist");
        if (!doesBlobExist(container, key))
            throw new IOException("target object does not exist");
        try {
            BlobClient blobClient = getBlobServiceClient()
                    .getBlobContainerClient(container)
                    .getBlobClient(key);
            BlobProperties blobProperties = blobClient.getProperties();
            String azureChecksum = HexFormat.of().formatHex(blobProperties.getContentMd5());
            if (!Files.exists(destinationDirectory)) {
                try {
                    Files.createDirectories(destinationDirectory);
                } catch (IOException e) {
                    logger.cpmsError("Output directory [{}] does not exist and could not be created",
                            destinationDirectory.toAbsolutePath());
                    return false;
                }
            }
            int prefixLength = key.lastIndexOf("/") + 1;
            Path outFile = destinationDirectory.resolve(key.substring(prefixLength));
            CrescoAzureLoggingTransferListener listener = new CrescoAzureLoggingTransferListener(logger,
                    blobProperties.getBlobSize());
            com.azure.storage.common.ParallelTransferOptions parallelTransferOptions =
                    new com.azure.storage.common.ParallelTransferOptions().setProgressListener(listener);
            BlobDownloadToFileOptions downloadOptions = new BlobDownloadToFileOptions(outFile.toString())
                    .setParallelTransferOptions(parallelTransferOptions);
            blobClient.downloadToFileWithResponse(downloadOptions, null, null);
            String localChecksum;
            try (InputStream in = new FileInputStream(outFile.toFile())) {
                localChecksum = DigestUtils.md5Hex(in);
            } catch (Exception e) {
                logger.error("Failed to compute the local MD5 checksum of downloaded file: {}", e);
                return false;
            }
            return localChecksum.equals(azureChecksum);
        } catch (Exception e) {
            logger.cpmsError("Failed to download file: {}", e.getMessage());
            return false;
        }
    }

    /*
        Logging Setup
     */

    public CPMSLogger getLogger() {
        return logger;
    }

    public void setLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(AzureBlobStorage.class);
    }

    public void updateLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(AzureBlobStorage.class);
    }

    /*
        Getters
     */

    public String getEndpoint() {
        return endpoint;
    }

    public TokenCredential getTokenCredential() {
        return tokenCredential;
    }

    /*
        Interface Methods
     */

    /**
     * Lists the top level container objects associated with the provided cloud provider credentials
     *
     * @return A String list of the top level container objects
     */
    @Override
    public List<String> listTopLevelContainers() {
        logger.debug("listTopLevelContainers()");
        return listContainers().stream().map(BlobContainerItem::getName).collect(Collectors.toList());
    }

    /**
     *
     * @param containerName The name of the top level container object
     * @return A boolean indicating the existence of a container with a matching name
     */
    @Override
    public boolean doesContainerExist(String containerName) {
        logger.debug("doesContainerExist({})", containerName);
        return doesBlobContainerExist(containerName);
    }

    /**
     * List the storage objects associated with a top level container name
     *
     * @param containerName The name of the top level container object to list contained objects
     * @return A String list of the objects contained within a cloud storage container
     */
    @Override
    public List<String> listObjectsInContainer(String containerName) {
        logger.debug("listObjectsInContainer({})", containerName);
        return listContainerBlobs(containerName).stream().map(BlobItem::getName).collect(Collectors.toList());
    }

    /**
     * List the storage objects associated with a top level container name with a matching name prefix
     *
     * @param containerName The name of the top level container object to list contained objects
     * @param prefix        A String prefix to select objects within the supplied container
     * @return A String list of the objects contained within a cloud storage container with matching prefix
     */
    @Override
    public List<String> listObjectsInContainer(String containerName, String prefix) {
        logger.debug("listObjectsInContainer({}, {})", containerName, prefix);
        return listContainerBlobs(containerName, prefix).stream().map(BlobItem::getName).collect(Collectors.toList());
    }

    /**
     * Uploads a local file to the indicated container
     *
     * @param uploadPath Path of local file to upload
     * @param container  Name of container in which to upload file
     * @param key        Key to use inside container
     * @return Whether the file was successfully uploaded
     * @throws IOException if uploadPath doesn't exist locally or container doesn't exist remotely
     */
    @Override
    public boolean uploadFile(Path uploadPath, String container, String key) throws IOException {
        return uploadFileToBlob(uploadPath, container, key);
    }

    /**
     * Uploads a local file to the indicated container
     *
     * @param container         Name of container in which to upload file
     * @param key               Key to use inside container
     * @param destinationFolder The folder in which to download the remote object
     * @return Whether the object was successfully downloaded
     * @throws IOException if the object doesn't exist remotely or local download fails
     */
    @Override
    public boolean downloadObject(String container, String key, Path destinationFolder) throws IOException {
        return downloadBlobToFile(container, key, destinationFolder);
    }

    private static class CrescoAzureLoggingTransferListener implements ProgressListener {
        private final CPMSLogger logger;
        private final int updatePercentStep = 5;
        private final Long totalBytes;
        private long lastTimestamp;
        private long lastTransferred = 0L;
        private int nextUpdate = updatePercentStep;
        private boolean notifiedOfCompletion = false;

        public CrescoAzureLoggingTransferListener(CPMSLogger logger, long totalBytes) {
            this.lastTimestamp = System.currentTimeMillis();
            this.totalBytes = totalBytes;
            this.logger = logger.cloneLogger(CrescoAzureLoggingTransferListener.class);
        }

        /**
         * The callback function invoked as progress is reported.
         *
         * <p>
         * The callback can be called concurrently from multiple threads if reporting spans across multiple
         * requests. The implementor must not perform thread blocking operations in the handler code.
         * </p>
         *
         * @param progress The total progress at the current point of time.
         */
        @Override
        public void handleProgress(long progress) {
            float currentTransferPercentage = ((float)progress / (float)totalBytes) * (float)100;
            if (currentTransferPercentage > (float)nextUpdate - 0.01) {
                long transferredSinceLastUpdate = progress - lastTransferred;
                long currentTimestamp = System.currentTimeMillis();
                logger.cpmsInfo("Transferring ({}/{} {}%) at {}",
                        humanReadableByteCount(progress, true),
                        humanReadableByteCount(totalBytes, true),
                        (int)(currentTransferPercentage + 0.01),
                        humanReadableTransferRate(transferredSinceLastUpdate, currentTimestamp - lastTimestamp));
                lastTransferred = progress;
                lastTimestamp = currentTimestamp;
                nextUpdate += updatePercentStep;
                if (currentTransferPercentage >= (float)(100.0 - 0.01) && !notifiedOfCompletion) {
                    notifiedOfCompletion = true;
                    logger.cpmsInfo("Completing/closing parallel transfers");
                }
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
