package io.quarkiverse.rabbitmqclient;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        // We can report UP if all brokers are UP.
        // A broker is considered UP if one or more of its associated
        //   addresses is reachable.

        Map<String, List<ClientStatus>> data = new HashMap<>();
        if (config.defaultClient != null) {
            appendClientState(data, null, config.defaultClient);
        }
        config.namedClients.forEach((n, c) -> appendClientState(data, n, c));

        HealthCheckResponseBuilder builder = HealthCheckResponse.builder();
        builder.name(HEALTH_CHECK_NAME);
        builder.status(data.values().stream()
                .allMatch(clientStatuses -> clientStatuses.stream().anyMatch(ClientStatus::isUp)));
        data.forEach((name, clientStatuses) -> {
            String prefix = name.length() > 0 ? name + "|" : name;
            clientStatuses.forEach(cs -> {
                builder.withData(prefix + cs.getAddress(), cs.getStatus());
            });
        });
        return builder.build();
    }

    private void appendClientState(Map<String, List<ClientStatus>> data, String name, RabbitMQClientConfig config) {
        String clientName = name == null ? "" : name;
        data.putIfAbsent(clientName, new ArrayList<>());
        RabbitMQHelper.resolveBrokerAddresses(config)
                .forEach((a) -> {
                    if (isBrokerAvailable(a)) {
                        data.get(clientName).add(new ClientStatus(a, true));
                    } else {
                        data.get(clientName).add(new ClientStatus(a, false));
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
