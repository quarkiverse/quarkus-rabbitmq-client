package io.quarkiverse.rabbitmqclient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Connection;

import io.quarkus.runtime.TlsConfig;

/**
 * RabbitMQ client implementation for {@link RabbitMQClient}
 *
 * @author b.passon
 */
class RabbitMQClientImpl implements RabbitMQClient {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQClientImpl.class);

    private final RabbitMQClientConfig config;
    private final TlsConfig tlsConfig;
    private final ManagedExecutor managedExecutor;
    private final Map<String, Connection> connections;

    RabbitMQClientImpl(RabbitMQClientConfig config, TlsConfig tlsConfig, ManagedExecutor managedExecutor) {
        this.config = config;
        this.tlsConfig = tlsConfig;
        this.managedExecutor = managedExecutor;
        this.connections = new HashMap<>();
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
                RabbitMQHelper.resolveBrokerAddresses(config));
        return this.connections.computeIfAbsent(name, n -> RabbitMQHelper.newConnection(config, tlsConfig, managedExecutor, n));
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
                    connection.close();
                }
                log.debug("Closed connection {} with RabbitMQ broker.", name);
            } catch (AlreadyClosedException ex) {
                log.debug("Already closed connection {} with RabbitMQ broker.", name);
            } catch (IOException e) {
                log.debug("Failed to close connection {} with RabbitMQ broker, ignoring.", name);
            }
        });

    }
}
