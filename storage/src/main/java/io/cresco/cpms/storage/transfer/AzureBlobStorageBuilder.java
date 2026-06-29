package io.cresco.cpms.storage.transfer;

import com.azure.core.credential.BasicAuthenticationCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import io.cresco.cpms.exceptions.StorageExecutionException;
import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;

@SuppressWarnings("unused")
public class AzureBlobStorageBuilder {
    private String endpoint;
    private TokenCredential tokenCredential;

    private CPMSLogger logger;

    public AzureBlobStorageBuilder() {
        this.logger = new BasicCPMSLoggerBuilder().withClass(AzureBlobStorageBuilder.class).build();
        if (System.getenv("AZURE_STORAGE_ACCOUNT_NAME") != null)
            this.endpoint = String.format("https://%s.blob.core.windows.net",
                    System.getenv("AZURE_STORAGE_ACCOUNT_NAME"));
        this.tokenCredential = new DefaultAzureCredentialBuilder().build();
    }

    public AzureBlobStorageBuilder withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public AzureBlobStorageBuilder withLogger(CPMSLogger logger) {
        setLogger(logger);
        return this;
    }

    public AzureBlobStorageBuilder withStaticCredentials(String username, String password) {
        this.tokenCredential = new BasicAuthenticationCredential(username, password);
        return this;
    }

    public AzureBlobStorage build() throws StorageExecutionException {
        AzureBlobStorage azureBlobStorage = new AzureBlobStorage(this);
        validateAzureBlobStorageObject(azureBlobStorage);
        return azureBlobStorage;
    }

    public void validateAzureBlobStorageObject(AzureBlobStorage azureBlobStorage) {
        //Todo: Add some validation here
    }

    public String getEndpoint() {
        return endpoint;
    }

    public CPMSLogger getLogger() {
        return logger;
    }

    public void setLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(AzureBlobStorageBuilder.class);
    }

    public TokenCredential getTokenCredential() {
        return tokenCredential;
    }
}
