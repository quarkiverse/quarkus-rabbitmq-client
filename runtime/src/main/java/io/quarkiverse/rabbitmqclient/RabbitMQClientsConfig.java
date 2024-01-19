package io.quarkiverse.rabbitmqclient;

import java.util.Map;

import io.quarkus.runtime.annotations.*;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithParentName;
import io.smallrye.config.WithUnnamedKey;

@ConfigMapping(prefix = "quarkus.rabbitmqclient")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface RabbitMQClientsConfig {

    /**
     * RabbitMQ clients.
     */
    @ConfigDocSection
    @ConfigDocMapKey("client-name")
    @WithParentName
    @WithUnnamedKey(RabbitMQClients.DEFAULT_CLIENT_NAME)
    Map<String, RabbitMQClientConfig> clients();
}
