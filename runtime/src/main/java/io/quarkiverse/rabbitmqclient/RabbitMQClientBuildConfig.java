package io.quarkiverse.rabbitmqclient;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * RabbitMQ client configuration.
 *
 * @author b.passon
 */
@ConfigGroup
public interface RabbitMQClientBuildConfig {

    /**
     * A unique RabbitMQ client identifier.
     */
    Optional<String> id();

    /**
     * Enables the client.
     */
    @WithName("client-enabled")
    @WithDefault("true")
    boolean clientEnabled();
}
