package io.quarkiverse.rabbitmqclient;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.rabbitmq.client.ConnectionFactory;

import io.quarkus.runtime.annotations.*;

/**
 * RabbitMQ client configuration.
 *
 * @author b.passon
 */
@ConfigGroup
public class RabbitMQClientConfig {

    /**
     * URI for connecting, formatted as amqp://userName:password@hostName:portNumber/virtualHost
     */
    @ConfigItem
    public Optional<String> uri;

    /**
     * Broker addresses for creating connections.
     * <p>
     * When specified, {@code quarkus.rabbitmqclient.hostname} and
     * {@code quarkus.rabbitmqclient.port} are ignored.
     * </p>
     */
    @ConfigItem
    @ConfigDocMapKey("broker-name")
    @ConfigDocSection
    public Map<String, Address> addresses = Collections.emptyMap();

    /**
     * Username for authentication
     */
    @ConfigItem(defaultValue = ConnectionFactory.DEFAULT_USER)
    public String username;

    /**
     * Password for authentication
     */
    @ConfigItem(defaultValue = ConnectionFactory.DEFAULT_PASS)
    public String password;

    /**
     * Hostname for connecting
     */
    @ConfigItem(defaultValue = ConnectionFactory.DEFAULT_HOST)
    public String hostname;

    /**
     * Virtual host
     */
    @ConfigItem(defaultValue = ConnectionFactory.DEFAULT_VHOST)
    public String virtualHost;

    /**
     * Port number for connecting
     */
    @ConfigItem(defaultValue = "" + ConnectionFactory.USE_DEFAULT_PORT)
    public int port;

    /**
     * Connection timeout in milliseconds
     */
    @ConfigItem(defaultValue = "" + ConnectionFactory.DEFAULT_CONNECTION_TIMEOUT)
    public int connectionTimeout;

    /**
     * Connection close timeout in milliseconds
     */
    @ConfigItem(defaultValue = "-1")
    public int connectionCloseTimeout;

    /**
     * Heartbeat interval in seconds
     */
    @ConfigItem(defaultValue = "" + ConnectionFactory.DEFAULT_HEARTBEAT)
    public int requestedHeartbeat;

    /**
     * Handshake timeout in milliseconds
     */
    @ConfigItem(defaultValue = "" + ConnectionFactory.DEFAULT_HANDSHAKE_TIMEOUT)
    public int handshakeTimeout;

    /**
     * Shutdown timeout in milliseconds
     */
    @ConfigItem(defaultValue = "" + ConnectionFactory.DEFAULT_SHUTDOWN_TIMEOUT)
    public int shutdownTimeout;

    /**
     * Maximum number of channels per connection
     */
    @ConfigItem(defaultValue = "" + ConnectionFactory.DEFAULT_CHANNEL_MAX)
    public int requestedChannelMax;

    /**
     * Maximum frame size
     */
    @ConfigItem(defaultValue = "" + ConnectionFactory.DEFAULT_FRAME_MAX)
    public int requestedFrameMax;

    /**
     * Network recovery interval in milliseconds
     */
    @ConfigItem(defaultValue = "" + ConnectionFactory.DEFAULT_NETWORK_RECOVERY_INTERVAL)
    public int networkRecoveryInterval;

    /**
     * Channel RPC timeout in milliseconds
     */
    @ConfigItem(defaultValue = "600000")
    public int channelRpcTimeout;

    /**
     * Validate channel RPC response type
     */
    @ConfigItem(defaultValue = "false")
    public boolean channelRpcResponseTypeCheck;

    /**
     * Recover connection on failure
     */
    @ConfigItem(defaultValue = "true")
    public boolean connectionRecovery;

    /**
     * Recover topology on failure
     */
    @ConfigItem(defaultValue = "true")
    public boolean topologyRecovery;

    /**
     * SASL authentication mechanisms
     */
    @ConfigItem(defaultValue = "plain")
    public SaslType sasl;

    /**
     * Tls configuration
     */
    @ConfigItem
    @ConfigDocSection
    public TlsConfig tls;

    /**
     * Non blocking IO configuration
     */
    @ConfigItem
    @ConfigDocSection
    public NioConfig nio;

    /**
     * Client properties
     */
    @ConfigItem
    @ConfigDocMapKey("property-name")
    @ConfigDocSection
    public Map<String, String> properties;

    @ConfigGroup
    public static class Address {

        /**
         * Hostname for connecting
         */
        @ConfigItem
        public String hostname;

        /**
         * Port number for connecting
         */
        @ConfigItem
        public int port;

    }

    @ConfigGroup
    public static class NioConfig {

        /**
         * Enables non blocking IO
         */
        @ConfigItem(defaultValue = "false")
        public boolean enabled;

        /**
         * Read buffer size in bytes
         */
        @ConfigItem(defaultValue = "32768")
        public int readByteBufferSize;

        /**
         * Write buffer size in bytes
         */
        @ConfigItem(defaultValue = "32768")
        public int writeByteBufferSize;

        /**
         * Number of non blocking IO threads
         */
        @ConfigItem(defaultValue = "1")
        public int threads;

        /**
         * Write enqueuing timeout in milliseconds
         */
        @ConfigItem(defaultValue = "10000")
        public int writeEnqueuingTimeout;

        /**
         * Write queue capacity.
         */
        @ConfigItem(defaultValue = "10000")
        public int writeQueueCapacity;

    }

    @ConfigGroup
    public static class TlsConfig {

        /**
         * Enables TLS
         */
        @ConfigItem(defaultValue = "false")
        public boolean enabled;

        /**
         * TLS Algorithm to use
         */
        @ConfigItem(defaultValue = "TLSv1.2")
        public String algorithm;

        /**
         * Trust store file
         */
        @ConfigItem
        public Optional<String> trustStoreFile;

        /**
         * Trust store type
         */
        @ConfigItem(defaultValue = "JKS")
        public String trustStoreType;

        /**
         * Trust store algorithm
         */
        @ConfigItem(defaultValue = "SunX509")
        public String trustStoreAlgorithm;

        /**
         * Trust store password
         */
        @ConfigItem
        public Optional<String> trustStorePassword;

        /**
         * Key store file
         */
        @ConfigItem
        public Optional<String> keyStoreFile;

        /**
         * Key store password
         */
        @ConfigItem
        public Optional<String> keyStorePassword;

        /**
         * Key store type
         */
        @ConfigItem(defaultValue = "PKCS12")
        public String keyStoreType;

        /**
         * Key store algorithm
         */
        @ConfigItem(defaultValue = "SunX509")
        public String keyStoreAlgorithm;

        /**
         * Validate server certificate
         */
        @ConfigItem(defaultValue = "true")
        public boolean validateServerCertificate;

        /**
         * Verify hostname
         */
        @ConfigItem(defaultValue = "true")
        public boolean verifyHostname;

    }
}
