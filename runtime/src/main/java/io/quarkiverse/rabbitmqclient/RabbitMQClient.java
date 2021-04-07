package io.quarkiverse.rabbitmqclient;

import com.rabbitmq.client.Connection;

/**
 * RabbitMQ client for handling connections with RabbitMQ brokers using the AMQP 0.9.1 protocol.
 *
 * @author b.passon
 */
public interface RabbitMQClient {

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
     * <p>
     * Clients are also closed during Quarkus shutdown.
     * </p>
     */
    void disconnect();

    /**
     * Gets the name of the client.
     */
    String getName();
}
