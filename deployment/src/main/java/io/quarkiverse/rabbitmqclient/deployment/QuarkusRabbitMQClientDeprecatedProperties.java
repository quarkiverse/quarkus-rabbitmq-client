package io.quarkiverse.rabbitmqclient.deployment;

import java.util.Optional;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

public class QuarkusRabbitMQClientDeprecatedProperties {

    private static final Logger LOG = Logger.getLogger(QuarkusRabbitMQClientProcessor.class);

    public static boolean clientEnabled(String name) {
        String n = name != null ? name + "." : "";
        return resolveDeprecatedConfigProperty("quarkus.rabbitmqclient." + n + "enabled",
                "quarkus.rabbitmqclient." + n + "client-enabled", true, Boolean.class);
    }

    private static <T> T resolveDeprecatedConfigProperty(String deprecated, String replacement, T defaultValue, Class<T> type) {
        Config config = ConfigProvider.getConfig();
        Optional<T> deprecatedValue = config.getOptionalValue(deprecated, type);
        Optional<T> replacementValue = config.getOptionalValue(replacement, type);
        if (deprecatedValue.isPresent()) {
            LOG.warnf(
                    "The configuration property '%s' is deprecated and will be removed in a future release. Please use '%s' instead.",
                    deprecated, replacement);
        }
        return replacementValue.or(() -> deprecatedValue).orElse(defaultValue);
    }
}
