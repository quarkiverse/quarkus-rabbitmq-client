package io.quarkiverse.rabbitmqclient.deployment;

import java.util.Map;

import io.quarkus.runtime.annotations.*;

/**
 * RabbitMQ client build time configuration.
 *
 * @author b.passon
 */
@ConfigRoot(name = "rabbitmqclient", phase = ConfigPhase.BUILD_TIME)
public class RabbitMQClientsBuildConfig {

    /**
     * The default client.
     */
    @ConfigItem(name = ConfigItem.PARENT)
    public RabbitMQClientBuildConfig defaultClient;

    /**
     * Additional named clients.
     */
    @ConfigDocSection
    @ConfigDocMapKey("client-name")
    @ConfigItem(name = ConfigItem.PARENT)
    public Map<String, RabbitMQClientBuildConfig> namedClients;

    /**
     * Disables health check
     */
    @ConfigItem(name = "health.enabled", defaultValue = "true")
    public boolean healthEnabled;

    /**
     * Disables metrics
     */
    @ConfigItem(name = "metrics.enabled", defaultValue = "true")
    public boolean metricsEnabled;

}
