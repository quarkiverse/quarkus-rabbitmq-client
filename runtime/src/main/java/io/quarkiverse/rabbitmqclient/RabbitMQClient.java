package io.quarkiverse.rabbitmqclient;

import java.io.Closeable;

import com.rabbitmq.client.Connection;

/**
 * RabbitMQ client for handling connections with RabbitMQ brokers using the AMQP 0.9.1 protocol.
 *
 * @author b.passon
 */
public interface RabbitMQClient extends Closeable {

    /**
     * Opens a connection to the configured RabbitMQ broker.
     *
     * @return a new randomly named connection.
     */
    Connection connect();

    /**
     * Opens a connection to the configured RabbitMQ broker with a given name.
     *
     * @param name the name of the connection.
     * @return a new connection if none exists with the given name, else the exiting one.
     */
    Connection connect(String name);

    /**
     * Explicitly disconnects the client from the RabbitMQ broker.
     *
     * @deprecated Use {@link #close()} instead.
     */
    @Deprecated(forRemoval = true, since = "3.3.0")
    default void disconnect() {
        try {
            close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the name of the client.
     *
     * @deprecated use {@link #getId()} instead.
     */
    @Deprecated(forRemoval = true, since = "3.3.0")
    default String getName() {
        return getId();
    }

    /**
     * Gets the unique identifier of the client.
     *
     * @return the unique identifier of the client.
     */
    String getId();
}
