package io.quarkiverse.rabbitmqclient.deployment;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

/**
 * RabbitMQ client configuration.
 *
 * @author b.passon
 */
@ConfigGroup
public interface RabbitMQClientBuildConfig {

    /**
     * Disable the client.
     */
    @WithDefault("true")
    boolean enabled();
}
