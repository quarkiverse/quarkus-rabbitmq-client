package io.quarkiverse.rabbitmqclient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MetricsCollector;
import com.rabbitmq.client.ShutdownListener;

/**
 * RabbitMQ client implementation for {@link RabbitMQClient}
 *
 * @author b.passon
 */
class RabbitMQClientImpl implements RabbitMQClient {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQClientImpl.class);

    private final Map<String, Connection> connections;
    private final RabbitMQClientParams params;
    private final MetricsCollector metricsCollector;

    RabbitMQClientImpl(RabbitMQClientParams params, MetricsCollector metricsCollector) {
        this.connections = new HashMap<>();
        this.params = params;
        this.metricsCollector = metricsCollector;
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
                n -> RabbitMQHelper.newConnection(params, n, metricsCollector));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect() {

        CountDownLatch cdl = new CountDownLatch(connections.size());
        ShutdownListener l = cause -> {
            cdl.countDown();
        };

        int closeTimeOut = params.getConfig().connectionCloseTimeout;
        connections.forEach((name, connection) -> {
            try {
                connection.addShutdownListener(l);
                log.debug("Closing connection {} with RabbitMQ broker.", name);
                connection.close(params.getConfig().connectionCloseTimeout);
                log.debug("Closed connection {} with RabbitMQ broker.", name);
            } catch (AlreadyClosedException ex) {
                log.debug("Already closed connection {} with RabbitMQ broker.", name);
            } catch (IOException e) {
                log.debug("Failed to close connection {} with RabbitMQ broker, ignoring.", name);
            }
        });
        try {
            if (closeTimeOut < 0) {
                cdl.await();
            } else {
                if (!cdl.await((long) params.getConfig().connectionCloseTimeout * connections.size(), TimeUnit.MILLISECONDS)) {
                    log.warn("Disconnecting RabbitMQ client connections timed out.");
                }
            }
        } catch (InterruptedException ie) {
            log.warn("Disconnecting RabbitMQ client was interrupted.", ie);
        }
    }

    /**
     * Gets the name of the client.
     */
    @Override
    public String getName() {
        return params.getName();
    }
}
