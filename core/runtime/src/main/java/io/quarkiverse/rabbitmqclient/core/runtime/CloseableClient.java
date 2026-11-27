package io.quarkiverse.rabbitmqclient.core.runtime;

import java.io.Closeable;

/**
 * A RabbitMQ client, of any protocol, that is closed on application shutdown by {@link ClientShutdown}.
 * <p>
 * A client bean is only closed if {@code CloseableClient} is one of its bean types and its contextual instance
 * implements it. The bean's client proxy does not need to implement it.
 * </p>
 */
public interface CloseableClient extends Closeable {

    /**
     * Gets the unique identifier of the client.
     *
     * @return the unique identifier of the client.
     */
    String getId();
}
