package io.quarkiverse.rabbitmqclient.deployment;

import java.util.Map;

import io.quarkiverse.rabbitmqclient.RabbitMQClients;
import io.quarkus.runtime.annotations.*;
import io.smallrye.config.*;

/**
 * RabbitMQ client build time configuration.
 *
 * @author b.passon
 */
@ConfigMapping(prefix = "quarkus.rabbitmqclient")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface RabbitMQClientsBuildConfig {

    /**
     * RabbitMQ clients.
     */
    @ConfigDocSection
    @ConfigDocMapKey("client-name")
    @WithParentName
    @WithUnnamedKey(RabbitMQClients.DEFAULT_CLIENT_NAME)
    Map<String, RabbitMQClientBuildConfig> clients();

    /**
     * Disables health check
     */
    @WithName("health.enabled")
    @WithDefault("true")
    boolean healthEnabled();

    /**
     * Disables metrics
     */
    @WithName("metrics.enabled")
    @WithDefault("true")
    boolean metricsEnabled();

}
