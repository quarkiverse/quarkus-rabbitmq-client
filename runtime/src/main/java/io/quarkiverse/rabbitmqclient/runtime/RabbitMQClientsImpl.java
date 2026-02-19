package io.quarkiverse.rabbitmqclient.runtime;

import java.util.List;
import java.util.Map;

import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkiverse.rabbitmqclient.RabbitMQClients;

public class RabbitMQClientsImpl implements RabbitMQClients {

    private final String defaultClientId;
    private final Map<String, RabbitMQClient> clients;

    public RabbitMQClientsImpl(String defaultClientId, Map<String, RabbitMQClient> clients) {
        this.defaultClientId = defaultClientId;
        this.clients = clients;

    }

    /**
     * @inheritDoc
     */
    @Override
    public RabbitMQClient getClient() {
        return getClient(defaultClientId);
    }

    /**
     * @inheritDoc
     */
    @Override
    public RabbitMQClient getClient(String id) {
        return clients.get(id);
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<String> getClientIds() {
        return List.copyOf(clients.keySet());
    }

    public Map<String, RabbitMQClient> getClients() {
        return clients;
    }
}
