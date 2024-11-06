package io.quarkiverse.rabbitmqclient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import com.rabbitmq.client.*;

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
     * @param params the {@link RabbitMQClientParams} for the connection.
     * @param name the name for the connection.
     * @return a {@link Connection} connected to a configured RabbitMQ broker.
     * @throws RabbitMQClientException if a failure occurs.
     */
    public static Connection newConnection(RabbitMQClientParams params, String name, MetricsCollector mc) {
        try {
            RabbitMQClientConfig config = params.getConfig();
            ConnectionFactory cf = newConnectionFactory(params);
            if (mc != null) {
                cf.setMetricsCollector(mc);
            }
            List<Address> addresses = config.addresses().isEmpty()
                    ? Collections.singletonList(new Address(config.hostname(), config.port()))
                    : convertAddresses(config.addresses());

            return addresses == null ? cf.newConnection(name)
                    : cf.newConnection(addresses, name);
        } catch (Exception e) {
            throw new RabbitMQClientException("Failed to connect to RabbitMQ broker", e);
        }
    }

    private static ConnectionFactory newConnectionFactory(RabbitMQClientParams params) {
        ConnectionFactory cf = new ConnectionFactory();
        cf.setSharedExecutor(params.getExecutorService());
        cf.setSaslConfig(params.getConfig().sasl().getSaslConfig());

        ConnectionFactoryConfigurator.load(cf, newProperties(params), "");
        cf.setMaxInboundMessageBodySize(params.getConfig().maxInboundMessageBodySize());

        String uri = params.getConfig().uri().orElse(null);
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
        return config.addresses().isEmpty()
                ? Collections.singletonList(new Address(config.hostname(),
                        ConnectionFactory.portOrDefault(config.port(), config.tls() != null && config.tls().enabled())))
                : convertAddresses(config.addresses());
    }

    private static List<Address> convertAddresses(Map<String, RabbitMQClientConfig.Address> addresses) {
        return addresses.values()
                .stream()
                .map(a -> new Address(a.hostname(), a.port()))
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
     * Compute the {@link Properties} for use with
     * {@link ConnectionFactoryConfigurator}.
     *
     * @param params the {@link RabbitMQClientParams}.
     * @return the computed properties.
     */
    static Properties newProperties(RabbitMQClientParams params) {
        Properties properties = new Properties();
        properties.setProperty(ConnectionFactoryConfigurator.USERNAME, params.getConfig().username());
        properties.setProperty(ConnectionFactoryConfigurator.PASSWORD, params.getConfig().password());
        properties.setProperty(ConnectionFactoryConfigurator.VIRTUAL_HOST, params.getConfig().virtualHost());
        properties.setProperty(ConnectionFactoryConfigurator.HOST, params.getConfig().hostname());
        properties.setProperty(ConnectionFactoryConfigurator.PORT, Integer.toString(params.getConfig().port()));
        properties.setProperty(ConnectionFactoryConfigurator.CONNECTION_CHANNEL_MAX,
                Integer.toString(params.getConfig().requestedChannelMax()));
        properties.setProperty(ConnectionFactoryConfigurator.CONNECTION_FRAME_MAX,
                Integer.toString(params.getConfig().requestedFrameMax()));
        properties.setProperty(ConnectionFactoryConfigurator.CONNECTION_HEARTBEAT,
                Integer.toString(params.getConfig().requestedHeartbeat()));
        properties.setProperty(ConnectionFactoryConfigurator.CONNECTION_TIMEOUT,
                Integer.toString(params.getConfig().connectionTimeout()));
        properties.setProperty(ConnectionFactoryConfigurator.HANDSHAKE_TIMEOUT,
                Integer.toString(params.getConfig().handshakeTimeout()));
        properties.setProperty(ConnectionFactoryConfigurator.SHUTDOWN_TIMEOUT,
                Integer.toString(params.getConfig().shutdownTimeout()));

        // client properties
        properties.setProperty(ConnectionFactoryConfigurator.CLIENT_PROPERTIES_PREFIX, CLIENT_PROPERTY_PREFIX);
        params.getConfig().properties().forEach((name, value) -> {
            properties.setProperty(CLIENT_PROPERTY_PREFIX + name, value);
        });

        properties.setProperty(ConnectionFactoryConfigurator.CONNECTION_RECOVERY_ENABLED,
                Boolean.toString(params.getConfig().connectionRecovery()));
        properties.setProperty(ConnectionFactoryConfigurator.TOPOLOGY_RECOVERY_ENABLED,
                Boolean.toString(params.getConfig().topologyRecovery()));
        properties.setProperty(ConnectionFactoryConfigurator.CONNECTION_RECOVERY_INTERVAL,
                Integer.toString(params.getConfig().networkRecoveryInterval()));
        properties.setProperty(ConnectionFactoryConfigurator.CHANNEL_RPC_TIMEOUT,
                Integer.toString(params.getConfig().channelRpcTimeout()));
        properties.setProperty(ConnectionFactoryConfigurator.CHANNEL_SHOULD_CHECK_RPC_RESPONSE_TYPE,
                Boolean.toString(params.getConfig().channelRpcResponseTypeCheck()));

        // NIO
        properties.setProperty(ConnectionFactoryConfigurator.USE_NIO, Boolean.toString(params.getConfig().nio().enabled()));
        properties.setProperty(ConnectionFactoryConfigurator.NIO_READ_BYTE_BUFFER_SIZE,
                Integer.toString(params.getConfig().nio().readByteBufferSize()));
        properties.setProperty(ConnectionFactoryConfigurator.NIO_WRITE_BYTE_BUFFER_SIZE,
                Integer.toString(params.getConfig().nio().writeByteBufferSize()));
        properties.setProperty(ConnectionFactoryConfigurator.NIO_NB_IO_THREADS,
                Integer.toString(params.getConfig().nio().threads()));
        properties.setProperty(ConnectionFactoryConfigurator.NIO_WRITE_ENQUEUING_TIMEOUT_IN_MS,
                Integer.toString(params.getConfig().nio().writeEnqueuingTimeout()));
        properties.setProperty(ConnectionFactoryConfigurator.NIO_WRITE_QUEUE_CAPACITY,
                Integer.toString(params.getConfig().nio().writeQueueCapacity()));

        // TLS Configuration
        if (params.getConfig().tls() != null && params.getConfig().tls().enabled()) {
            properties.setProperty(ConnectionFactoryConfigurator.SSL_ALGORITHM, params.getConfig().tls().algorithm());
            properties.setProperty(ConnectionFactoryConfigurator.SSL_ENABLED,
                    Boolean.toString(params.getConfig().tls().enabled()));

            properties.setProperty(ConnectionFactoryConfigurator.SSL_VALIDATE_SERVER_CERTIFICATE,
                    Boolean.toString(params.getConfig().tls().validateServerCertificate()));
            properties.setProperty(ConnectionFactoryConfigurator.SSL_VERIFY_HOSTNAME,
                    Boolean.toString(params.getConfig().tls().verifyHostname()));

            // TLS Keys
            params.getConfig().tls().keyStoreFile()
                    .ifPresent(s -> properties.setProperty(ConnectionFactoryConfigurator.SSL_KEY_STORE, s));
            params.getConfig().tls().keyStorePassword()
                    .ifPresent(s -> properties.setProperty(ConnectionFactoryConfigurator.SSL_KEY_STORE_PASSWORD, s));
            properties.setProperty(ConnectionFactoryConfigurator.SSL_KEY_STORE_TYPE,
                    params.getConfig().tls().keyStoreType());
            properties.setProperty(ConnectionFactoryConfigurator.SSL_KEY_STORE_ALGORITHM,
                    params.getConfig().tls().keyStoreAlgorithm());

            // TLS Trust
            params.getConfig().tls().trustStoreFile()
                    .ifPresent(s -> properties.setProperty(ConnectionFactoryConfigurator.SSL_TRUST_STORE, s));
            params.getConfig().tls().trustStorePassword()
                    .ifPresent(s -> properties.setProperty(ConnectionFactoryConfigurator.SSL_TRUST_STORE_PASSWORD, s));
            properties.setProperty(ConnectionFactoryConfigurator.SSL_TRUST_STORE_TYPE,
                    params.getConfig().tls().trustStoreType());
            properties.setProperty(ConnectionFactoryConfigurator.SSL_TRUST_STORE_ALGORITHM,
                    params.getConfig().tls().trustStoreAlgorithm());
        }

        return properties;
    }
}
