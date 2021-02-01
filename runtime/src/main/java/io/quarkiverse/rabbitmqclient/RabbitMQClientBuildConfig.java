package io.quarkiverse.rabbitmqclient;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

/**
 * RabbitMQ client build time configuration.
 *
 * @author b.passon
 */
@ConfigRoot(name = "rabbitmqclient", phase = ConfigPhase.BUILD_TIME)
public class RabbitMQClientBuildConfig {

    /**
     * Enables health check
     */
    @ConfigItem(defaultValue = "true")
    public boolean healthEnabled;
}
