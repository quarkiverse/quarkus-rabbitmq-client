package io.quarkiverse.rabbitmqclient.util;

import java.util.Optional;

import io.quarkiverse.rabbitmqclient.RabbitMQClientConfig;
import io.quarkus.arc.config.ConfigProperties;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigProperties(prefix = "test")
public class TestConfig {

    @ConfigItem
    public int amqpsPort;

    @ConfigItem
    public int amqpPort;

    public void setupNonSll(RabbitMQClientConfig config) {
        config.port = amqpPort;
        config.virtualHost = "/";
        config.username = "guest";
        config.password = "guest";
        config.hostname = "localhost";
        config.tls.enabled = false;
    }

    public void setupBasicSsl(RabbitMQClientConfig config) {
        config.port = amqpsPort;
        config.virtualHost = "/";
        config.username = "guest";
        config.password = "guest";
        config.hostname = "localhost";
        config.tls.enabled = true;
        config.tls.keyStoreFile = Optional.empty();
        config.tls.keyStorePassword = Optional.empty();
        config.tls.trustStoreFile = Optional.of("classpath:/rabbitmq/ca/cacerts.jks");
        config.tls.trustStoreType = "JKS";
        config.tls.trustStorePassword = Optional.of("letmein");
    }

    public void setupClientCertSsl(RabbitMQClientConfig config) {
        config.port = amqpsPort;
        config.virtualHost = "/";
        config.username = "guest";
        config.password = "guest";
        config.hostname = "localhost";
        config.tls.enabled = true;
        config.tls.keyStoreFile = Optional.of("classpath:/rabbitmq/client/client.jks");
        config.tls.keyStoreType = "JKS";
        config.tls.keyStorePassword = Optional.of("letmein");
        config.tls.trustStoreFile = Optional.of("classpath:/rabbitmq/ca/cacerts.jks");
        config.tls.trustStoreType = "JKS";
        config.tls.trustStorePassword = Optional.of("letmein");
    }
}
