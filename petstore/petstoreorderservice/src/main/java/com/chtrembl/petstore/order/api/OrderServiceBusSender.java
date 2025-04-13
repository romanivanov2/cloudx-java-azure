package com.chtrembl.petstore.order.api;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderServiceBusSender {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceBusSender.class);

    private final ServiceBusSenderClient senderClient;

    public OrderServiceBusSender(
            @Value("${azure.servicebus.connection-string}")
            String connectionString,
            @Value("${azure.servicebus.queue-name}")
            String queueName
    ) {
        this.senderClient = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .queueName(queueName)
                .buildClient();
    }

    public void sendOrderToQueue(String orderJson, String sessionId) {
        try {
            ServiceBusMessage message = new ServiceBusMessage(orderJson).setSessionId(sessionId);
            senderClient.sendMessage(message);
            log.info("Order sent to Service Bus queue successfully. Session ID: {}", sessionId);
        } catch (Exception e) {
            log.error("Failed to send order to Service Bus queue", e);
        }
    }
}