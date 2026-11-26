package io.quarkiverse.rabbitmqclient.runtime;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkiverse.rabbitmqclient.RabbitMQClientConfig;
import io.quarkiverse.rabbitmqclient.RabbitMQClients;
import io.quarkiverse.rabbitmqclient.core.runtime.BrokerAddress;
import io.quarkiverse.rabbitmqclient.core.runtime.ClientsHealthCheck;
import io.quarkus.arc.ClientProxy;

/**
 * RabbitMQ ready check which checks if at least one of the configured brokers is available.
 *
 * @author b.passon
 */
@Readiness
@ApplicationScoped
public class RabbitMQHealthCheck implements HealthCheck {

    public static final String HEALTH_CHECK_NAME = "quarkus-rabbitmq-client";

    @Inject
    RabbitMQClients clients;

    @Override
    public HealthCheckResponse call() {
        return ClientsHealthCheck.check(HEALTH_CHECK_NAME, clients.getClientIds().stream()
                .collect(Collectors.toMap(id -> id, id -> resolveBrokerAddresses(resolveConfig(id)))));
    }

    private RabbitMQClientConfig resolveConfig(String id) {
        RabbitMQClient client = clients.getClient(id);
        if (client == null) {
            throw new IllegalStateException("Could not find RabbitMQClientConfig for id: " + id);
        }
        return ((RabbitMQClientImpl) ClientProxy.unwrap(client)).getConfig();
    }

    private static List<BrokerAddress> resolveBrokerAddresses(RabbitMQClientConfig config) {
        return RabbitMQHelper.resolveBrokerAddresses(config).stream()
                .map(a -> new BrokerAddress(a.getHost(), a.getPort()))
                .toList();
    }
}
