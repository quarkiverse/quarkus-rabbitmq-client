package io.quarkiverse.rabbitmqclient;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import com.rabbitmq.client.Address;

/**
 * RabbitMQ ready check which checks if at least one of the configured brokers is available.
 *
 * @author b.passon
 */
@Readiness
@ApplicationScoped
public class RabbitMQReadyCheck implements HealthCheck {

    public static final String HEALTH_CHECK_NAME = "quarkus-rabbitmq-client";
    @Inject
    RabbitMQClientsConfig config;

    @Override
    public HealthCheckResponse call() {
        return checkAllBrokers();
    }

    private HealthCheckResponse checkAllBrokers() {
        Map<String, HealthCheckResponse.State> data = new HashMap<>();
        appendClientState(data, null, config.defaultClient);
        config.namedClients.forEach((n, c) -> {
            appendClientState(data, n, c);
        });

        HealthCheckResponseBuilder builder = HealthCheckResponse.builder();
        builder.name(HEALTH_CHECK_NAME);
        builder.state(data.values().stream().allMatch(s -> s == HealthCheckResponse.State.UP));
        data.forEach((a, s) -> {
            builder.withData(a, s.name());
        });
        return builder.build();
    }

    private void appendClientState(Map<String, HealthCheckResponse.State> data, String name, RabbitMQClientConfig config) {
        RabbitMQHelper.resolveBrokerAddresses(config)
                .forEach((a) -> {
                    String prefix = name == null ? "" : name + "|";
                    if (isBrokerAvailable(a)) {
                        data.put(prefix + a.toString(), HealthCheckResponse.State.UP);
                    } else {
                        data.put(prefix + a.toString(), HealthCheckResponse.State.DOWN);
                    }
                });
    }

    private boolean isBrokerAvailable(Address address) {
        try {
            new Socket(address.getHost(), address.getPort());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
