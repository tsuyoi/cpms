package io.cresco.cpms.storage.transfer;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import io.cresco.cpms.logging.CPMSLogger;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class AzureBlobStorage {
    private final String endpoint;
    private final TokenCredential tokenCredential;

    private CPMSLogger logger;

    public AzureBlobStorage(AzureBlobStorageBuilder builder) {
        this.endpoint = builder.getEndpoint();
        this.tokenCredential = builder.getTokenCredential();
    }

    private BlobServiceClient getBlobServiceClient() {
        if (tokenCredential == null)
            return getBlobServiceClientWithDefaultCredentials();
        return getBlobServiceClientWithCredentials();
    }

    private BlobServiceClient getBlobServiceClientWithDefaultCredentials() {
        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();
        return new BlobServiceClientBuilder()
                .endpoint(endpoint)
                .credential(defaultAzureCredential)
                .buildClient();
    }

    private BlobServiceClient getBlobServiceClientWithCredentials() {
        return new BlobServiceClientBuilder()
                .endpoint(endpoint)
                .credential(tokenCredential)
                .buildClient();
    }

    public List<BlobContainerItem> listContainers() {
        logger.debug("listContainers()");
        BlobServiceClient blobServiceClient = getBlobServiceClient();
        if (blobServiceClient != null) {
            return blobServiceClient.listBlobContainers().stream().collect(Collectors.toList());
        } else {
            logger.error("listContainers Error: Failed to generate client");
            return new ArrayList<>();
        }
    }

    public boolean doesContainerExist(String container) {
        logger.debug("doesContainerExist({})", container);
        BlobServiceClient blobServiceClient = getBlobServiceClient();
        if (blobServiceClient != null) {
            return blobServiceClient.getBlobContainerClient(container).exists();
        } else {
            logger.error("doesContainerExist Error: Failed to generate client");
            return false;
        }
    }

    public boolean createContainer(String container) {
        logger.debug("createContainer({})", container);
        BlobServiceClient blobServiceClient = getBlobServiceClient();
        if (blobServiceClient != null) {
            return blobServiceClient.getBlobContainerClient(container).createIfNotExists();
        } else {
            logger.error("doesContainerExist Error: Failed to generate client");
            return false;
        }
    }

    public List<BlobItem> listContainerBlobs(String container) {
        logger.debug("listContainerBlobs({})", container);
        BlobServiceClient blobServiceClient = getBlobServiceClient();
        if (blobServiceClient != null) {
            return blobServiceClient.getBlobContainerClient(container).listBlobs().stream().collect(Collectors.toList());
        } else {
            logger.error("listContainerBlobs Error: Failed to generate client");
            return new ArrayList<>();
        }
    }

    public List<BlobItem> listContainerBlobs(String container, String prefix) {
        logger.debug("listContainerBlobs({}, {})", container, prefix);
        BlobServiceClient blobServiceClient = getBlobServiceClient();
        if (blobServiceClient != null) {
            ListBlobsOptions listBlobsOptions = new ListBlobsOptions().setPrefix(prefix);
            return blobServiceClient.getBlobContainerClient(container).listBlobs(listBlobsOptions, null)
                    .stream().collect(Collectors.toList());
        } else {
            logger.error("listContainerBlobs Error: Failed to generate client");
            return new ArrayList<>();
        }
    }

    public boolean uploadFile(Path uploadPath, String container, String key) {
        logger.debug("uploadFile({}, {}, {})", uploadPath, container, key);
        BlobServiceClient blobServiceClient = getBlobServiceClient();
        if (blobServiceClient != null) {
            try {
                blobServiceClient
                        .getBlobContainerClient(container)
                        .getBlobClient(key)
                        .uploadFromFile(uploadPath.toString());
                return true;
            } catch (UncheckedIOException e) {
                logger.error("uploadFile Error: {}", e.getMessage());
                return false;
            }
        } else {
            logger.error("uploadFile Error: Failed to generate client");
            return false;
        }
    }

    public boolean downloadBlob(String container, String key, Path destinationDirectory) {
        logger.debug("downloadBlob({}, {}, {})", container, key, destinationDirectory);
        BlobServiceClient blobServiceClient = getBlobServiceClient();
        if (blobServiceClient != null) {
            try {
                //Todo: Parse out end of key to build final filename;
                Path downloadFile = destinationDirectory.resolve("download.file");
                blobServiceClient
                        .getBlobContainerClient(container)
                        .getBlobClient(key)
                        .downloadToFile(downloadFile.toString());
                return true;
            } catch (UncheckedIOException e) {
                logger.error("downloadFile Error: {}", e.getMessage());
                return false;
            }
        } else {
            logger.error("uploadFile Error: Failed to generate client");
            return false;
        }
    }
}
