package io.quarkiverse.rabbitmqclient;

import java.util.List;

public interface RabbitMQClients {

    /**
     * Returns a default {@link RabbitMQClient}.
     *
     * @return {@link RabbitMQClient}
     */
    RabbitMQClient getClient();

    /**
     * Returns an {@link RabbitMQClient} with a specific id.
     *
     * @param id {@link RabbitMQClient} id
     * @return {@link RabbitMQClient}
     */
    RabbitMQClient getClient(String id);

    /**
     * Returns the list of configured client ids.
     *
     * @return a list of client ids
     */
    List<String> getClientIds();
}
