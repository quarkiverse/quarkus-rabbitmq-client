package io.quarkiverse.rabbitmqclient;

import java.net.Socket;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
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

    @Inject
    RabbitMQClientConfig config;

    @Override
    public HealthCheckResponse call() {
        if (atLeastOneBrokerIsAlive()) {
            return HealthCheckResponse.up("At least one RabbitMQ broker is available.");
        }
        return HealthCheckResponse.down("No RabbitMQ broker is available.");
    }

    private boolean atLeastOneBrokerIsAlive() {
        return RabbitMQHelper.resolveBrokerAddresses(config)
                .stream()
                .anyMatch(this::isBrokerAvailable);
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
