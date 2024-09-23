package io.cresco.cpms.storage.transfer;

import com.azure.core.credential.BasicAuthenticationCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;

public class AzureBlobStorageBuilder {
    private String endpoint;
    private TokenCredential tokenCredential;

    private final CPMSLogger logger;

    public AzureBlobStorageBuilder() {
        this.logger = new BasicCPMSLoggerBuilder().withClass(AzureBlobStorageBuilder.class).build();
    }

    public AzureBlobStorageBuilder(CPMSLogger existingLogger) {
        this.logger = existingLogger.cloneLogger(AzureBlobStorageBuilder.class);
    }

    public AzureBlobStorageBuilder withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public AzureBlobStorageBuilder withStaticCredentials(String username, String password) {
        this.tokenCredential = new BasicAuthenticationCredential(username, password);
        return this;
    }

    public AzureBlobStorageBuilder withStaticCredentials(String clientId, String username, String password) {
        this.tokenCredential = new UsernamePasswordCredentialBuilder()
                .clientId(clientId)
                .username(username)
                .password(password)
                .build();
        return this;
    }

    public AzureBlobStorage build() {
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

    public TokenCredential getTokenCredential() {
        return tokenCredential;
    }

    public CPMSLogger getLogger() {
        return logger;
    }
}
