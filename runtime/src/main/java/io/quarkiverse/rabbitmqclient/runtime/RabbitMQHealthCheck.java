package io.quarkiverse.rabbitmqclient.runtime;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import com.rabbitmq.client.Address;

import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkiverse.rabbitmqclient.RabbitMQClientConfig;
import io.quarkiverse.rabbitmqclient.RabbitMQClients;

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
        return checkAllBrokers();
    }

    private HealthCheckResponse checkAllBrokers() {
        // We can report UP if all brokers are UP.
        // A broker is considered UP if one or more of its associated
        //   addresses is reachable.

        Map<String, List<ClientStatus>> data = new HashMap<>();
        resolveEnabledClients()
                .forEach((n, c) -> appendClientState(data, n, c));

        HealthCheckResponseBuilder builder = HealthCheckResponse.builder();
        builder.name(HEALTH_CHECK_NAME);
        builder.status(data.values().stream()
                .allMatch(clientStatuses -> clientStatuses.stream().anyMatch(ClientStatus::isUp)));
        data.forEach((name, clientStatuses) -> {
            String prefix = !name.isEmpty() ? name + "|" : name;
            clientStatuses.forEach(cs -> {
                builder.withData(prefix + cs.getAddress(), cs.getStatus());
            });
        });
        return builder.build();
    }

    private Map<String, RabbitMQClientConfig> resolveEnabledClients() {
        return clients.getClientIds().stream()
                .collect(Collectors.toMap(e -> e, this::resolveConfig));
    }

    private RabbitMQClientConfig resolveConfig(String id) {
        for (RabbitMQClient client : ((RabbitMQClientsImpl) clients).getClients().values()) {
            RabbitMQClientImpl ci = (RabbitMQClientImpl) client;
            if (ci.getId().equals(id)) {
                return ci.getConfig();
            }
        }
        throw new IllegalStateException("Could not find RabbitMQClientConfig for id: " + id);
    }

    private void appendClientState(Map<String, List<ClientStatus>> data, String name, RabbitMQClientConfig config) {
        data.putIfAbsent(name, new ArrayList<>());
        RabbitMQHelper.resolveBrokerAddresses(config)
                .forEach((a) -> {
                    if (isBrokerAvailable(a)) {
                        data.get(name).add(new ClientStatus(a, true));
                    } else {
                        data.get(name).add(new ClientStatus(a, false));
                    }
                });
    }

    private boolean isBrokerAvailable(Address address) {
        try (Socket s = new Socket(address.getHost(), address.getPort())) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static final class ClientStatus {
        private final Address address;
        private final boolean isUp;

        public ClientStatus(Address address, boolean isUp) {
            this.address = address;
            this.isUp = isUp;
        }

        public boolean isUp() {
            return isUp;
        }

        public Address getAddress() {
            return address;
        }

        public String getStatus() {
            return isUp ? HealthCheckResponse.Status.UP.name() : HealthCheckResponse.Status.DOWN.name();
        }
    }
}
