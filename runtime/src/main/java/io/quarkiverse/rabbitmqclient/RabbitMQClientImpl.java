package io.quarkiverse.rabbitmqclient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Connection;

import io.quarkus.runtime.LaunchMode;

/**
 * RabbitMQ client implementation for {@link RabbitMQClient}
 *
 * @author b.passon
 */
class RabbitMQClientImpl implements RabbitMQClient {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQClientImpl.class);
    private static final int DEFAULT_CONNECTION_CLOSE_TIME_OUT_DEV_MODE = 100;

    private final Map<String, Connection> connections;
    private final RabbitMQClientParams params;

    RabbitMQClientImpl(RabbitMQClientParams params) {
        this.connections = new HashMap<>();
        this.params = params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection connect() {
        return connect(UUID.randomUUID().toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection connect(String name) {
        log.debug("Opening connection {} with a RabbitMQ broker. Configured brokers: {}", name,
                RabbitMQHelper.resolveBrokerAddresses(params.getConfig()));
        return this.connections.computeIfAbsent(name,
                n -> RabbitMQHelper.newConnection(params, n));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect() {
        connections.forEach((name, connection) -> {
            try {
                log.debug("Closing connection {} with RabbitMQ broker.", name);
                // This will close all channels related to this connection
                if (connection != null) {
                    // Set a positive connection close timeout in dev mode due to thread deadlock, see
                    // https://github.com/quarkiverse/quarkus-rabbitmq-client/issues/19
                    if (params.getLaunchMode() == LaunchMode.DEVELOPMENT && params.getConfig().connectionCloseTimeout < 0) {
                        connection.close(DEFAULT_CONNECTION_CLOSE_TIME_OUT_DEV_MODE);
                    }
                    connection.close(params.getConfig().connectionCloseTimeout);
                }
                log.debug("Closed connection {} with RabbitMQ broker.", name);
            } catch (AlreadyClosedException ex) {
                log.debug("Already closed connection {} with RabbitMQ broker.", name);
            } catch (IOException e) {
                log.debug("Failed to close connection {} with RabbitMQ broker, ignoring.", name);
            }
        });
    }

    /**
     * Gets the name of the client.
     */
    @Override
    public String getName() {
        return params.getName();
    }
}
