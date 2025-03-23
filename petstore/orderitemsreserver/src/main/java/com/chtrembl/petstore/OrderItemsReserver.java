package com.chtrembl.petstore;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class OrderItemsReserver {
    private static final boolean BLOB_OVERRIDE_MODE = true;
    private final BlobContainerClient containerClient;

    public OrderItemsReserver() {
        // Get connection string and container name from application settings
        String connectionString = System.getenv("AzureWebJobsStorage");
        String containerName = System.getenv("BLOB_CONTAINER_NAME");

        // Initialize blob client
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        // Initialize container client
        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);

        // Ensure container exists
        if (!containerClient.exists()) {
            containerClient.create();
        }
    }

    @FunctionName("orders")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Processing order reservation request.");

        String sessionId = request.getHeaders().get("session-id");
        if (sessionId == null || sessionId.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Session ID is required")
                    .build();
        }

        String orderJson = request.getBody().orElse("");
        if (orderJson.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Please provide order details")
                    .build();
        }
        try {

            uploadToBlob(sessionId, orderJson);

            return request.createResponseBuilder(HttpStatus.OK)
                    .body("Order saved successfully")
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error processing order: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing order")
                    .build();
        }

    }

    private void uploadToBlob(String sessionId, String orderJson) throws RuntimeException {
        BlobClient blobClient = containerClient.getBlobClient(sessionId + ".json");

        try (InputStream dataStream = new ByteArrayInputStream(orderJson.getBytes(StandardCharsets.UTF_8))) {
            blobClient.upload(dataStream, orderJson.length(), BLOB_OVERRIDE_MODE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload to blob storage", e);
        }
    }
}