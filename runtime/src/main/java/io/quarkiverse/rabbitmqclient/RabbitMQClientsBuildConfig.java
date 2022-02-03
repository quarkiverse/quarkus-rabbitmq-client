package io.quarkiverse.rabbitmqclient;

import io.quarkus.runtime.annotations.*;

/**
 * RabbitMQ client build time configuration.
 *
 * @author b.passon
 */
@ConfigRoot(name = "rabbitmqclient", phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public class RabbitMQClientsBuildConfig {

    /**
     * Enables health check
     */
    @ConfigItem(defaultValue = "true")
    public boolean healthEnabled;

    /**
     * Enables metrics
     */
    @ConfigItem(defaultValue = "true")
    public boolean metricsEnabled;
}
