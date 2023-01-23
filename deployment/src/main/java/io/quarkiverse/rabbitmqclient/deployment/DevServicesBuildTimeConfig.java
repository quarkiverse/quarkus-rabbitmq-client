package io.quarkiverse.rabbitmqclient.deployment;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

@ConfigGroup
public class DevServicesBuildTimeConfig {

    /**
     * If DevServices has been explicitly enabled or disabled. DevServices is generally enabled
     * by default, unless there is an existing configuration present.
     * When DevServices is enabled Quarkus will attempt to automatically configure and start
     * a database when running in Dev or Test mode.
     */
    @ConfigItem
    public Optional<Boolean> enabled = Optional.empty();

    /**
     * The container image name to use, for container based DevServices providers.
     */
    @ConfigItem(defaultValue = "rabbitmq:3.11.3-management-alpine")
    public String imageName;

    /**
     * Additional environment entries that can be added to the container before its start.
     */
    @ConfigItem
    public Map<String, String> additionalEnv;

    /**
     * This value can be used to specify the port to which the management port of the container is exposed.
     */
    @ConfigItem(defaultValue = "15672")
    public OptionalInt managementPort = OptionalInt.empty();

    /**
     * This value can be used to specify the port to which the http-port of the container is exposed. It must be a free
     * port, otherwise startup will fail.
     */
    @ConfigItem(defaultValue = "5672")
    public OptionalInt httpPort = OptionalInt.empty();
}
