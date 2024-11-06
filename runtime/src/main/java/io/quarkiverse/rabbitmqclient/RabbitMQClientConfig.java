package io.quarkiverse.rabbitmqclient;

import java.util.Map;
import java.util.Optional;

import com.rabbitmq.client.ConnectionFactory;

import io.quarkus.runtime.annotations.*;
import io.smallrye.config.WithDefault;

/**
 * RabbitMQ client configuration.
 *
 * @author b.passon
 */
@ConfigGroup
public interface RabbitMQClientConfig {

    /**
     * URI for connecting, formatted as amqp://userName:password@hostName:portNumber/virtualHost
     */
    Optional<String> uri();

    /**
     * Broker addresses for creating connections.
     * <p>
     * When specified, {@code quarkus.rabbitmqclient.hostname} and
     * {@code quarkus.rabbitmqclient.port} are ignored.
     * </p>
     */
    @ConfigDocMapKey("broker-name")
    @ConfigDocSection
    Map<String, Address> addresses();

    /**
     * Username for authentication
     */
    @WithDefault(ConnectionFactory.DEFAULT_USER)
    String username();

    /**
     * Password for authentication
     */
    @WithDefault(ConnectionFactory.DEFAULT_PASS)
    String password();

    /**
     * Hostname for connecting
     */
    @WithDefault(ConnectionFactory.DEFAULT_HOST)
    String hostname();

    /**
     * Virtual host
     */
    @WithDefault(ConnectionFactory.DEFAULT_VHOST)
    String virtualHost();

    /**
     * Port number for connecting
     */
    @WithDefault("" + ConnectionFactory.USE_DEFAULT_PORT)
    int port();

    /**
     * Connection timeout in milliseconds
     */
    @WithDefault("" + ConnectionFactory.DEFAULT_CONNECTION_TIMEOUT)
    int connectionTimeout();

    /**
     * Connection close timeout in milliseconds
     */
    @WithDefault("-1")
    int connectionCloseTimeout();

    /**
     * Heartbeat interval in seconds
     */
    @WithDefault("" + ConnectionFactory.DEFAULT_HEARTBEAT)
    int requestedHeartbeat();

    /**
     * Handshake timeout in milliseconds
     */
    @WithDefault("" + ConnectionFactory.DEFAULT_HANDSHAKE_TIMEOUT)
    int handshakeTimeout();

    /**
     * Shutdown timeout in milliseconds
     */
    @WithDefault("" + ConnectionFactory.DEFAULT_SHUTDOWN_TIMEOUT)
    int shutdownTimeout();

    /**
     * Maximum number of channels per connection
     */
    @WithDefault("" + ConnectionFactory.DEFAULT_CHANNEL_MAX)
    int requestedChannelMax();

    /**
     * Maximum frame size
     */
    @WithDefault("" + ConnectionFactory.DEFAULT_FRAME_MAX)
    int requestedFrameMax();

    /**
     * Maximum body size of inbound (received) messages in bytes.
     *
     * <p>
     * Default value is 67,108,864 (64 MiB).
     */
    @WithDefault("67108864")
    int maxInboundMessageBodySize();

    /**
     * Network recovery interval in milliseconds
     */
    @WithDefault("" + ConnectionFactory.DEFAULT_NETWORK_RECOVERY_INTERVAL)
    int networkRecoveryInterval();

    /**
     * Channel RPC timeout in milliseconds
     */
    @WithDefault("600000")
    int channelRpcTimeout();

    /**
     * Validate channel RPC response type
     */
    @WithDefault("false")
    boolean channelRpcResponseTypeCheck();

    /**
     * Recover connection on failure
     */
    @WithDefault("true")
    boolean connectionRecovery();

    /**
     * Recover topology on failure
     */
    @WithDefault("true")
    boolean topologyRecovery();

    /**
     * SASL authentication mechanisms
     */
    @WithDefault("plain")
    SaslType sasl();

    /**
     * Tls configuration
     */
    @ConfigDocSection
    TlsConfig tls();

    /**
     * Non-blocking IO configuration
     */
    @ConfigDocSection
    NioConfig nio();

    /**
     * Client properties
     */
    @ConfigDocMapKey("property-name")
    @ConfigDocSection
    Map<String, String> properties();

    @ConfigGroup
    interface Address {

        /**
         * Hostname for connecting
         */
        String hostname();

        /**
         * Port number for connecting
         */
        int port();

    }

    @ConfigGroup
    interface NioConfig {

        /**
         * Enables non blocking IO
         */
        @WithDefault("false")
        boolean enabled();

        /**
         * Read buffer size in bytes
         */
        @WithDefault("32768")
        int readByteBufferSize();

        /**
         * Write buffer size in bytes
         */
        @WithDefault("32768")
        int writeByteBufferSize();

        /**
         * Number of non blocking IO threads
         */
        @WithDefault("1")
        int threads();

        /**
         * Write enqueuing timeout in milliseconds
         */
        @WithDefault("10000")
        int writeEnqueuingTimeout();

        /**
         * Write queue capacity.
         */
        @WithDefault("10000")
        int writeQueueCapacity();

    }

    @ConfigGroup
    interface TlsConfig {

        /**
         * Enables TLS
         */
        @WithDefault("false")
        boolean enabled();

        /**
         * TLS Algorithm to use
         */
        @WithDefault("TLSv1.2")
        String algorithm();

        /**
         * Trust store file
         */
        Optional<String> trustStoreFile();

        /**
         * Trust store type
         */
        @WithDefault("JKS")
        String trustStoreType();

        /**
         * Trust store algorithm
         */
        @WithDefault("SunX509")
        String trustStoreAlgorithm();

        /**
         * Trust store password
         */
        Optional<String> trustStorePassword();

        /**
         * Key store file
         */
        Optional<String> keyStoreFile();

        /**
         * Key store password
         */
        Optional<String> keyStorePassword();

        /**
         * Key store type
         */
        @WithDefault("PKCS12")
        String keyStoreType();

        /**
         * Key store algorithm
         */
        @WithDefault("SunX509")
        String keyStoreAlgorithm();

        /**
         * Validate server certificate
         */
        @WithDefault("true")
        boolean validateServerCertificate();

        /**
         * Verify hostname
         */
        @WithDefault("true")
        boolean verifyHostname();

    }
}