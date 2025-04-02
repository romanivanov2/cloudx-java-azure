package com.chtrembl.petstore.order.config;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CosmosDbConfig {
    @Value("${azure.cosmos.uri}")
    private String uri;

    @Value("${azure.cosmos.key}")
    private String key;

    @Value("${azure.cosmos.database}")
    private String databaseName;

    @Value("${azure.cosmos.container.name}")
    private String containerName;

    @Bean
    public CosmosClient cosmosClient() {
        return new CosmosClientBuilder()
                .endpoint(uri)
                .key(key)
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .buildClient();
    }

    @Bean
    public CosmosDatabase cosmosDatabase(CosmosClient client) {
        CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists(databaseName);
        return client.getDatabase(databaseResponse.getProperties().getId());
    }

    @Bean
    public CosmosContainer cosmosContainer(CosmosDatabase database) {
        CosmosContainerProperties containerProperties =
                new CosmosContainerProperties(containerName, "/sessionId");

        CosmosContainerResponse containerResponse =
                database.createContainerIfNotExists(containerProperties);

        return database.getContainer(containerResponse.getProperties().getId());
    }
}
