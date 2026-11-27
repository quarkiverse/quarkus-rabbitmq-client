package io.quarkiverse.rabbitmqclient.core.runtime;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

/**
 * Protocol-neutral readiness check of RabbitMQ clients.
 * <p>
 * Reports UP if all clients are UP. A client is UP if one or more of its broker addresses accepts a TCP connection.
 * The response data maps {@code <client name>|<address>} to {@code UP} or {@code DOWN}.
 * </p>
 */
public final class ClientsHealthCheck {

    private ClientsHealthCheck() {
    }

    /**
     * Probes the broker addresses of all clients.
     *
     * @param name the name of the health check.
     * @param brokersByClient the broker addresses per client name.
     * @return the health check response.
     */
    public static HealthCheckResponse check(String name,
            Map<String, ? extends Collection<BrokerAddress>> brokersByClient) {
        Map<String, List<ClientStatus>> data = new HashMap<>();
        brokersByClient.forEach((client, addresses) -> appendClientState(data, client, addresses));

        HealthCheckResponseBuilder builder = HealthCheckResponse.builder();
        builder.name(name);
        builder.status(data.values().stream()
                .allMatch(clientStatuses -> clientStatuses.stream().anyMatch(ClientStatus::isUp)));
        data.forEach((client, clientStatuses) -> {
            String prefix = !client.isEmpty() ? client + "|" : client;
            clientStatuses.forEach(cs -> {
                builder.withData(prefix + cs.address(), cs.getStatus());
            });
        });
        return builder.build();
    }

    private static void appendClientState(Map<String, List<ClientStatus>> data, String name,
            Collection<BrokerAddress> addresses) {
        data.putIfAbsent(name, new ArrayList<>());
        addresses.forEach(a -> data.get(name).add(new ClientStatus(a, isBrokerAvailable(a))));
    }

    private static boolean isBrokerAvailable(BrokerAddress address) {
        try (Socket s = new Socket(address.host(), address.port())) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private record ClientStatus(BrokerAddress address, boolean isUp) {

        String getStatus() {
            return isUp ? HealthCheckResponse.Status.UP.name() : HealthCheckResponse.Status.DOWN.name();
        }
    }
}
