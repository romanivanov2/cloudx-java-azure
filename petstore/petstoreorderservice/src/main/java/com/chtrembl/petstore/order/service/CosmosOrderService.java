package com.chtrembl.petstore.order.service;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.chtrembl.petstore.order.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CosmosOrderService {
    private static final Logger log = LoggerFactory.getLogger(CosmosOrderService.class);
    private final CosmosContainer container;

    public CosmosOrderService(CosmosContainer container) {
        this.container = container;
    }

    public Order saveOrder(String sessionId, Order order) {
        order.setSessionId(sessionId);
        try {
            CosmosItemResponse<Order> response = container.upsertItem(
                    order,
                    new PartitionKey(sessionId),
                    new CosmosItemRequestOptions()
            );
            return response.getItem();
        } catch (CosmosException e) {
            log.error("Error saving order: {}", e.getMessage());
            throw new RuntimeException("Failed to save order", e);
        }
    }

    public Order getOrder(String sessionId) {
        try {
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            options.setPartitionKey(new PartitionKey(sessionId));
            String query = "SELECT * FROM c WHERE c.sessionId = @sessionId";
            SqlQuerySpec querySpec = new SqlQuerySpec(query)
                    .setParameters(Collections.singletonList(
                            new SqlParameter("@sessionId", sessionId)));
            CosmosPagedIterable<Order> queryResult = container.queryItems(
                    querySpec,
                    options,
                    Order.class
            );
            List<Order> orders = queryResult.stream().collect(Collectors.toList());
            return orders.isEmpty() ? new Order() : orders.get(0);
        } catch (CosmosException e) {
            log.error("Error retrieving order: {}", e.getMessage());
            return new Order();
        }
    }
}