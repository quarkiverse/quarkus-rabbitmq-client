package io.quarkiverse.rabbitmqclient;

import java.util.Properties;

import org.junit.jupiter.api.Assertions;

import com.rabbitmq.client.ConnectionFactoryConfigurator;

import io.quarkus.runtime.TlsConfig;

public abstract class RabbitMQConfigTest {

    protected void assertRabbitMQConfig(RabbitMQClientConfig config, TlsConfig tlsConfig, Properties properties) {
        Assertions.assertEquals(config.username, properties.getProperty(ConnectionFactoryConfigurator.USERNAME));
        Assertions.assertEquals(config.password, properties.getProperty(ConnectionFactoryConfigurator.PASSWORD));
        Assertions.assertEquals(config.virtualHost, properties.getProperty(ConnectionFactoryConfigurator.VIRTUAL_HOST));
        Assertions.assertEquals(config.hostname, properties.getProperty(ConnectionFactoryConfigurator.HOST));
        Assertions.assertEquals(asString(config.port), properties.getProperty(ConnectionFactoryConfigurator.PORT));
        Assertions.assertEquals(asString(config.requestedChannelMax),
                properties.getProperty(ConnectionFactoryConfigurator.CONNECTION_CHANNEL_MAX));
        Assertions.assertEquals(asString(config.requestedFrameMax),
                properties.getProperty(ConnectionFactoryConfigurator.CONNECTION_FRAME_MAX));
        Assertions.assertEquals(asString(config.requestedHeartbeat),
                properties.getProperty(ConnectionFactoryConfigurator.CONNECTION_HEARTBEAT));
        Assertions.assertEquals(asString(config.connectionTimeout),
                properties.getProperty(ConnectionFactoryConfigurator.CONNECTION_TIMEOUT));
        Assertions.assertEquals(asString(config.handshakeTimeout),
                properties.getProperty(ConnectionFactoryConfigurator.HANDSHAKE_TIMEOUT));
        Assertions.assertEquals(asString(config.shutdownTimeout),
                properties.getProperty(ConnectionFactoryConfigurator.SHUTDOWN_TIMEOUT));
        Assertions.assertEquals(asString(config.connectionRecovery),
                properties.getProperty(ConnectionFactoryConfigurator.CONNECTION_RECOVERY_ENABLED));
        Assertions.assertEquals(asString(config.topologyRecovery),
                properties.getProperty(ConnectionFactoryConfigurator.TOPOLOGY_RECOVERY_ENABLED));
        Assertions.assertEquals(asString(config.networkRecoveryInterval),
                properties.getProperty(ConnectionFactoryConfigurator.CONNECTION_RECOVERY_INTERVAL));
        Assertions.assertEquals(asString(config.channelRpcTimeout),
                properties.getProperty(ConnectionFactoryConfigurator.CHANNEL_RPC_TIMEOUT));
        Assertions.assertEquals(asString(config.channelRpcResponseTypeCheck),
                properties.getProperty(ConnectionFactoryConfigurator.CHANNEL_SHOULD_CHECK_RPC_RESPONSE_TYPE));

        // NIO configuration
        if (config.nio.enabled) {
            Assertions.assertEquals(asString(config.nio.threads),
                    properties.getProperty(ConnectionFactoryConfigurator.NIO_NB_IO_THREADS));
            Assertions.assertEquals(asString(config.nio.readByteBufferSize),
                    properties.getProperty(ConnectionFactoryConfigurator.NIO_READ_BYTE_BUFFER_SIZE));
            Assertions.assertEquals(asString(config.nio.writeByteBufferSize),
                    properties.getProperty(ConnectionFactoryConfigurator.NIO_WRITE_BYTE_BUFFER_SIZE));
            Assertions.assertEquals(asString(config.nio.writeQueueCapacity),
                    properties.getProperty(ConnectionFactoryConfigurator.NIO_WRITE_QUEUE_CAPACITY));
            Assertions.assertEquals(asString(config.nio.writeEnqueuingTimeout),
                    properties.getProperty(ConnectionFactoryConfigurator.NIO_WRITE_ENQUEUING_TIMEOUT_IN_MS));
        }

        if (config.tls.enabled) {
            Assertions.assertEquals(config.tls.algorithm, properties.getProperty(ConnectionFactoryConfigurator.SSL_ALGORITHM));
            Assertions.assertEquals(asString(config.tls.enabled),
                    properties.getProperty(ConnectionFactoryConfigurator.SSL_ENABLED));

            if (tlsConfig.trustAll) {
                Assertions.assertEquals(Boolean.FALSE.toString(),
                        properties.getProperty(ConnectionFactoryConfigurator.SSL_VALIDATE_SERVER_CERTIFICATE));
                Assertions.assertEquals(Boolean.FALSE.toString(),
                        properties.getProperty(ConnectionFactoryConfigurator.SSL_VERIFY_HOSTNAME));
            } else {
                Assertions.assertEquals(asString(config.tls.validateServerCertificate),
                        properties.getProperty(ConnectionFactoryConfigurator.SSL_VALIDATE_SERVER_CERTIFICATE));
                Assertions.assertEquals(asString(config.tls.verifyHostname),
                        properties.getProperty(ConnectionFactoryConfigurator.SSL_VERIFY_HOSTNAME));

                Assertions.assertEquals(config.tls.keyStoreFile.orElse(null),
                        properties.getProperty(ConnectionFactoryConfigurator.SSL_KEY_STORE));
                Assertions.assertEquals(config.tls.keyStorePassword.orElse(null),
                        properties.getProperty(ConnectionFactoryConfigurator.SSL_KEY_STORE_PASSWORD));
                Assertions.assertEquals(config.tls.keyStoreType,
                        properties.getProperty(ConnectionFactoryConfigurator.SSL_KEY_STORE_TYPE));
                Assertions.assertEquals(config.tls.keyStoreAlgorithm,
                        properties.getProperty(ConnectionFactoryConfigurator.SSL_KEY_STORE_ALGORITHM));

                Assertions.assertEquals(config.tls.trustStoreFile.orElse(null),
                        properties.getProperty(ConnectionFactoryConfigurator.SSL_TRUST_STORE));
                Assertions.assertEquals(config.tls.trustStorePassword.orElse(null),
                        properties.getProperty(ConnectionFactoryConfigurator.SSL_TRUST_STORE_PASSWORD));
                Assertions.assertEquals(config.tls.trustStoreType,
                        properties.getProperty(ConnectionFactoryConfigurator.SSL_TRUST_STORE_TYPE));
                Assertions.assertEquals(config.tls.trustStoreAlgorithm,
                        properties.getProperty(ConnectionFactoryConfigurator.SSL_TRUST_STORE_ALGORITHM));
            }
        }

        // client properties
        Assertions.assertEquals(RabbitMQHelper.CLIENT_PROPERTY_PREFIX,
                properties.getProperty(ConnectionFactoryConfigurator.CLIENT_PROPERTIES_PREFIX));
        config.properties.forEach((name, value) -> {
            Assertions.assertEquals(value, properties.getProperty(RabbitMQHelper.CLIENT_PROPERTY_PREFIX + name));
        });
    }

    private String asString(int value) {
        return Integer.toString(value);
    }

    private String asString(boolean value) {
        return Boolean.toString(value);
    }
}
