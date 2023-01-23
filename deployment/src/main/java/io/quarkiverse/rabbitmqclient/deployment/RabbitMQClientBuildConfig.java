package io.quarkiverse.rabbitmqclient.deployment;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

/**
 * RabbitMQ client configuration.
 *
 * @author b.passon
 */
@ConfigGroup
public class RabbitMQClientBuildConfig {

    /**
     * Disable the client.
     */
    @ConfigItem(defaultValue = "true")
    public boolean enabled;

}
