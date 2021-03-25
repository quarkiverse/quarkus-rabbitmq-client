package io.quarkiverse.rabbitmqclient;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.eclipse.microprofile.context.ManagedExecutor;

import io.quarkus.runtime.TlsConfig;

/**
 * This class is sort of a producer {@link RabbitMQClient} instances.
 * <p>
 * It isn't a CDI producer in the literal sense, but it is created by a synthetic bean
 * from {@code QuarkusRabbitMQClientProcessor}
 * The {@code createRabbitMQClient} method is called at runtime (see
 * {@link io.quarkiverse.rabbitmqclient.runtime.RabbitMQRecorder#rabbitMQClientSupplier(String)}
 * in order to produce the actual {@code RabbitMQClient} objects.
 * </p>
 *
 * @author b.passon
 */
@Singleton
public class RabbitMQClients {

    private RabbitMQClientImpl defaultClient;
    private final Map<String, RabbitMQClientImpl> namedClients;

    private final ManagedExecutor managedExecutor;
    private final TlsConfig tlsConfig;
    private final RabbitMQClientsConfig rabbitMQClientsConfig;

    public RabbitMQClients(RabbitMQClientsConfig rabbitMQClientsConfig, TlsConfig tlsConfig, ManagedExecutor managedExecutor) {
        this.rabbitMQClientsConfig = rabbitMQClientsConfig;
        this.tlsConfig = tlsConfig;
        this.managedExecutor = managedExecutor;
        this.namedClients = new HashMap<>();
    }

    /**
     * Creates a singleton {@link RabbitMQClient}.
     *
     * @param name the name of the rabbit mq client, if null the default is assumed.
     * @return a configured {@link RabbitMQClient}.
     */
    public RabbitMQClient getRabbitMQClient(String name) {
        if (name == null) {
            if (defaultClient == null) {
                defaultClient = new RabbitMQClientImpl(rabbitMQClientsConfig.defaultClient, tlsConfig, managedExecutor);
            }
            return defaultClient;
        } else {
            return namedClients.computeIfAbsent(name,
                    n -> new RabbitMQClientImpl(rabbitMQClientsConfig.namedClients.get(n), tlsConfig, managedExecutor));
        }
    }

    /**
     * Destroys the {@link RabbitMQClient} and closes all connections.
     *
     * <p>
     * Note: This is called by the Quarkus during shutdown through the ShutdownContext, it therefore does
     * not need @PreDestroy
     * </p>
     */
    public void destroy() {
        // check for null, the producer method might never have been called.
        if (defaultClient != null) {
            defaultClient.disconnect();
        }
        namedClients.forEach((k, v) -> {
            v.disconnect();
        });
    }
}
