package com.chtrembl.petstore;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.ServiceBusQueueTrigger;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Azure Functions with Service Bus Queue Trigger.
 */
public class OrderItemsReserver {
    private static final boolean BLOB_OVERRIDE_MODE = true;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private final BlobContainerClient containerClient;
    private final ServiceBusSenderClient deadLetterSender;

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

        // Initialize Service Bus Dead-Letter Queue sender
        String serviceBusConnectionString = System.getenv("ServiceBusConnection");
        String deadLetterQueueName = "orders/$DeadLetterQueue";
        this.deadLetterSender = new ServiceBusClientBuilder()
                .connectionString(serviceBusConnectionString)
                .sender()
                .queueName(deadLetterQueueName)
                .buildClient();
    }

    @FunctionName("orders")
    public void run(
            @ServiceBusQueueTrigger(name = "orderMessage", queueName = "orders", connection = "ServiceBusConnection")
            String orderJson,
            final ExecutionContext context) {

        context.getLogger().info("Processing order reservation from Service Bus queue.");

        try {
            String sessionId = extractSessionId(orderJson);

            if (sessionId == null || sessionId.isEmpty()) {
                context.getLogger().severe("Session ID is missing in the order message.");
                return;
            }

            boolean uploadSuccess = uploadToBlobWithRetry(sessionId, orderJson, context);

            if (!uploadSuccess) {
                sendToDeadLetterQueue(orderJson, context);
            } else {
                context.getLogger().info("Order saved successfully.");
            }
        } catch (Exception e) {
            context.getLogger().severe("Error processing order: " + e.getMessage());
        }
    }

    private boolean uploadToBlobWithRetry(String sessionId, String orderJson, ExecutionContext context) {
        int attempt = 0;

        while (attempt < MAX_RETRY_ATTEMPTS) {
            try {
                attempt++;
                uploadToBlob(sessionId, orderJson);
                return true; // Upload successful
            } catch (Exception e) {
                context.getLogger().warning("Attempt " + attempt + " to upload failed: " + e.getMessage());
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    context.getLogger().severe("Max retry attempts reached. Upload failed.");
                }
            }
        }

        return false; // All attempts failed
    }

    private void uploadToBlob(String sessionId, String orderJson) throws RuntimeException {
        BlobClient blobClient = containerClient.getBlobClient(sessionId + ".json");

        try (InputStream dataStream = new ByteArrayInputStream(orderJson.getBytes(StandardCharsets.UTF_8))) {
            blobClient.upload(dataStream, orderJson.length(), BLOB_OVERRIDE_MODE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload to blob storage", e);
        }
    }

    private void sendToDeadLetterQueue(String orderJson, ExecutionContext context) {
        try {
            deadLetterSender.sendMessage(new com.azure.messaging.servicebus.ServiceBusMessage(orderJson));
            context.getLogger().info("Message sent to Dead-Letter Queue.");
        } catch (Exception e) {
            context.getLogger().severe("Failed to send message to Dead-Letter Queue: " + e.getMessage());
        }
    }

    private String extractSessionId(String orderJson) {
        try {
            JSONObject jsonObject = new JSONObject(orderJson);
            return jsonObject.getString("id");
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract session ID from order JSON", e);
        }
    }
}