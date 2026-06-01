package io.quarkiverse.rabbitmqclient;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithParentName;
import io.smallrye.config.WithUnnamedKey;

@ConfigMapping(prefix = "quarkus.rabbitmqclient")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface RabbitMQClientsConfig {

    String DEFAULT_CLIENT_NAME = "default";

    /**
     * RabbitMQ clients.
     */
    @ConfigDocSection
    @ConfigDocMapKey("client-name")
    @WithParentName
    @WithUnnamedKey(DEFAULT_CLIENT_NAME)
    Map<String, RabbitMQClientConfig> clients();

    static RabbitMQClientConfig getDefaultClient(RabbitMQClientsConfig config) {
        return config.clients().get(DEFAULT_CLIENT_NAME);
    }
}
