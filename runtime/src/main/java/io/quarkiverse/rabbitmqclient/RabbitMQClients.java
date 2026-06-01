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

    /**
     * Gets a named {@link RabbitMQClient}.
     *
     * @param name the name of the rabbit mq client, if null the default is assumed.
     * @return a configured {@link RabbitMQClient}.
     * @deprecated use {@link #getClient(String)} instead.
     */
    @Deprecated(forRemoval = true, since = "3.3.0")
    default RabbitMQClient getRabbitMQClient(String name) {
        if (name == null) {
            return getClient();
        }
        return getClient(name);
    }
}
