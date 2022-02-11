package io.quarkiverse.rabbitmqclient;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.eclipse.microprofile.context.ManagedExecutor;

import com.rabbitmq.client.MetricsCollector;

import io.quarkus.runtime.LaunchMode;
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

    public static final String DEFAULT_CLIENT_NAME = "default";
    private final Map<String, RabbitMQClientImpl> clients;

    private final ManagedExecutor executorService;
    private final TlsConfig tlsConfig;
    private final RabbitMQClientsConfig rabbitMQClientsConfig;
    private final LaunchMode launchMode;

    public RabbitMQClients(RabbitMQClientsConfig rabbitMQClientsConfig, TlsConfig tlsConfig, ManagedExecutor executorService,
            LaunchMode launchMode) {
        this.rabbitMQClientsConfig = rabbitMQClientsConfig;
        this.tlsConfig = tlsConfig;
        this.executorService = executorService;
        this.clients = new HashMap<>();
        this.launchMode = launchMode;
    }

    /**
     * Creates a singleton {@link RabbitMQClient}.
     *
     * @param name the name of the rabbit mq client, if null the default is assumed.
     * @return a configured {@link RabbitMQClient}.
     */
    public RabbitMQClient getRabbitMQClient(String name) {
        return getRabbitMQClient(name, null);
    }

    /**
     * Creates a singleton {@link RabbitMQClient}.
     *
     * @param name the name of the rabbit mq client, if null the default is assumed.
     * @param mc a metrics collector to use.
     * @return a configured {@link RabbitMQClient}.
     */
    public RabbitMQClient getRabbitMQClient(String name, MetricsCollector mc) {
        RabbitMQClientParams params = params(name);
        return clients.computeIfAbsent(name,
                n -> new RabbitMQClientImpl(params, mc));
    }

    private RabbitMQClientParams params(String name) {
        RabbitMQClientParams params = new RabbitMQClientParams();
        params.setName(name);
        params.setExecutorService(executorService);
        params.setLaunchMode(launchMode);
        params.setTlsConfig(tlsConfig);
        params.setConfig(DEFAULT_CLIENT_NAME.equals(name) ? rabbitMQClientsConfig.defaultClient
                : rabbitMQClientsConfig.namedClients.get(name));
        return params;
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
        clients.forEach((k, v) -> {
            v.disconnect();
        });
    }
}
