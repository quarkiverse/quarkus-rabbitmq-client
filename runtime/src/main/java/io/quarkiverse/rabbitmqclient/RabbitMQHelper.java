package io.quarkiverse.rabbitmqclient;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConnectionFactoryConfigurator;

import io.quarkus.runtime.TlsConfig;

/**
 * Helper class with RabbitMQClient utility methods.
 *
 * @author b.passon
 */
class RabbitMQHelper {

    static final String CLIENT_PROPERTY_PREFIX = "client-property.";

    /**
     * Opens a new connection to a RabbitMQ broker using the given configuration and name.
     *
     * @param config the {@link RabbitMQClientConfig}.
     * @param tlsConfig the {@link TlsConfig}.
     * @param name the name for the connection.
     * @return a {@link Connection} connected to a configured RabbitMQ broker.
     * @throws RabbitMQClientException if a failure occurs.
     */
    public static Connection newConnection(RabbitMQClientConfig config, TlsConfig tlsConfig, ExecutorService executorService,
            String name) {
        try {
            ConnectionFactory cf = newConnectionFactory(config, tlsConfig, executorService);
            List<Address> addresses = config.addresses.isEmpty()
                    ? Collections.singletonList(new Address(config.hostname, config.port))
                    : convertAddresses(config.addresses);

            return addresses == null ? cf.newConnection(name)
                    : cf.newConnection(addresses, name);
        } catch (Exception e) {
            throw new RabbitMQClientException("Failed to connect to RabbitMQ broker", e);
        }
    }

    private static ConnectionFactory newConnectionFactory(RabbitMQClientConfig config, TlsConfig tlsConfig,
            ExecutorService executorService) {
        ConnectionFactory cf = new ConnectionFactory();
        cf.setTopologyRecoveryExecutor(executorService);
        cf.setShutdownExecutor(executorService);
        cf.setSharedExecutor(executorService);
        ConnectionFactoryConfigurator.load(cf, newProperties(config, tlsConfig), "");

        String uri = config.uri.orElse(null);
        if (uri != null) {
            try {
                cleanUriConnectionProperties(cf);
                cf.setUri(uri);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid RabbitMQ connection URI " + uri);
            }
        }

        return cf;
    }

    /**
     * Resolves the available broker addresses based on the given configuration.
     * <p>
     * If multiple named brokers are supplied, the {@link RabbitMQClientConfig} {@code hostname}
     * and {@code port} settings are ignored.
     * </p>
     *
     * @param config the {@link RabbitMQClientConfig}.
     * @return a list of RabbitMQ broker addresses.
     */
    public static List<Address> resolveBrokerAddresses(RabbitMQClientConfig config) {
        return config.addresses.isEmpty()
                ? Collections.singletonList(new Address(config.hostname, config.port))
                : convertAddresses(config.addresses);
    }

    private static List<Address> convertAddresses(Map<String, RabbitMQClientConfig.Address> addresses) {
        return addresses.values()
                .stream()
                .map(a -> new Address(a.hostname, a.port))
                .collect(Collectors.toList());
    }

    private static void cleanUriConnectionProperties(ConnectionFactory cf) {
        cf.setUsername(null);
        cf.setPassword(null);
        cf.setHost(null);
        cf.setPort(-1);
        cf.setVirtualHost(null);
    }

    /**
     * Compute the {@link Properties} for use with {@link ConnectionFactoryConfigurator}.
     *
     * @param config the {@link RabbitMQClient} config.
     * @param tlsConfig the Quarkus generic {@link TlsConfig}.
     * @return the computed properties.
     */
    static Properties newProperties(RabbitMQClientConfig config, TlsConfig tlsConfig) {
        Properties properties = new Properties();
        properties.setProperty(ConnectionFactoryConfigurator.USERNAME, config.username);
        properties.setProperty(ConnectionFactoryConfigurator.PASSWORD, config.password);
        properties.setProperty(ConnectionFactoryConfigurator.VIRTUAL_HOST, config.virtualHost);
        properties.setProperty(ConnectionFactoryConfigurator.HOST, config.hostname);
        properties.setProperty(ConnectionFactoryConfigurator.PORT, Integer.toString(config.port));
        properties.setProperty(ConnectionFactoryConfigurator.CONNECTION_CHANNEL_MAX,
                Integer.toString(config.requestedChannelMax));
        properties.setProperty(ConnectionFactoryConfigurator.CONNECTION_FRAME_MAX, Integer.toString(config.requestedFrameMax));
        properties.setProperty(ConnectionFactoryConfigurator.CONNECTION_HEARTBEAT, Integer.toString(config.requestedHeartbeat));
        properties.setProperty(ConnectionFactoryConfigurator.CONNECTION_TIMEOUT, Integer.toString(config.connectionTimeout));
        properties.setProperty(ConnectionFactoryConfigurator.HANDSHAKE_TIMEOUT, Integer.toString(config.handshakeTimeout));
        properties.setProperty(ConnectionFactoryConfigurator.SHUTDOWN_TIMEOUT, Integer.toString(config.shutdownTimeout));

        // client properties
        properties.setProperty(ConnectionFactoryConfigurator.CLIENT_PROPERTIES_PREFIX, CLIENT_PROPERTY_PREFIX);
        config.properties.forEach((name, value) -> {
            properties.setProperty(CLIENT_PROPERTY_PREFIX + name, value);
        });

        properties.setProperty(ConnectionFactoryConfigurator.CONNECTION_RECOVERY_ENABLED,
                Boolean.toString(config.connectionRecovery));
        properties.setProperty(ConnectionFactoryConfigurator.TOPOLOGY_RECOVERY_ENABLED,
                Boolean.toString(config.topologyRecovery));
        properties.setProperty(ConnectionFactoryConfigurator.CONNECTION_RECOVERY_INTERVAL,
                Integer.toString(config.networkRecoveryInterval));
        properties.setProperty(ConnectionFactoryConfigurator.CHANNEL_RPC_TIMEOUT, Integer.toString(config.channelRpcTimeout));
        properties.setProperty(ConnectionFactoryConfigurator.CHANNEL_SHOULD_CHECK_RPC_RESPONSE_TYPE,
                Boolean.toString(config.channelRpcResponseTypeCheck));

        // NIO
        properties.setProperty(ConnectionFactoryConfigurator.USE_NIO, Boolean.toString(config.nio.enabled));
        properties.setProperty(ConnectionFactoryConfigurator.NIO_READ_BYTE_BUFFER_SIZE,
                Integer.toString(config.nio.readByteBufferSize));
        properties.setProperty(ConnectionFactoryConfigurator.NIO_WRITE_BYTE_BUFFER_SIZE,
                Integer.toString(config.nio.writeByteBufferSize));
        properties.setProperty(ConnectionFactoryConfigurator.NIO_NB_IO_THREADS, Integer.toString(config.nio.threads));
        properties.setProperty(ConnectionFactoryConfigurator.NIO_WRITE_ENQUEUING_TIMEOUT_IN_MS,
                Integer.toString(config.nio.writeEnqueuingTimeout));
        properties.setProperty(ConnectionFactoryConfigurator.NIO_WRITE_QUEUE_CAPACITY,
                Integer.toString(config.nio.writeQueueCapacity));

        // TLS Configuration
        if (config.tls != null) {
            properties.setProperty(ConnectionFactoryConfigurator.SSL_ALGORITHM, config.tls.algorithm);
            properties.setProperty(ConnectionFactoryConfigurator.SSL_ENABLED, Boolean.toString(config.tls.enabled));

            if (tlsConfig.trustAll) {
                properties.setProperty(ConnectionFactoryConfigurator.SSL_VALIDATE_SERVER_CERTIFICATE, Boolean.FALSE.toString());
                properties.setProperty(ConnectionFactoryConfigurator.SSL_VERIFY_HOSTNAME, Boolean.FALSE.toString());
            } else {
                properties.setProperty(ConnectionFactoryConfigurator.SSL_VALIDATE_SERVER_CERTIFICATE,
                        Boolean.toString(config.tls.validateServerCertificate));
                properties.setProperty(ConnectionFactoryConfigurator.SSL_VERIFY_HOSTNAME,
                        Boolean.toString(config.tls.verifyHostname));

                // TLS Keys
                config.tls.keyStoreFile.ifPresent(s -> properties.setProperty(ConnectionFactoryConfigurator.SSL_KEY_STORE, s));
                config.tls.keyStorePassword
                        .ifPresent(s -> properties.setProperty(ConnectionFactoryConfigurator.SSL_KEY_STORE_PASSWORD, s));
                properties.setProperty(ConnectionFactoryConfigurator.SSL_KEY_STORE_TYPE, config.tls.keyStoreType);
                properties.setProperty(ConnectionFactoryConfigurator.SSL_KEY_STORE_ALGORITHM, config.tls.keyStoreAlgorithm);

                // TLS Trust
                config.tls.trustStoreFile
                        .ifPresent(s -> properties.setProperty(ConnectionFactoryConfigurator.SSL_TRUST_STORE, s));
                config.tls.trustStorePassword
                        .ifPresent(s -> properties.setProperty(ConnectionFactoryConfigurator.SSL_TRUST_STORE_PASSWORD, s));
                properties.setProperty(ConnectionFactoryConfigurator.SSL_TRUST_STORE_TYPE, config.tls.trustStoreType);
                properties.setProperty(ConnectionFactoryConfigurator.SSL_TRUST_STORE_ALGORITHM, config.tls.trustStoreAlgorithm);
            }
        }

        return properties;
    }
}
