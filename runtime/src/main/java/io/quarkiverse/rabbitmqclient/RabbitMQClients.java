package io.quarkiverse.rabbitmqclient;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
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

    private RabbitMQClientImpl defaultClient;
    private final Map<String, RabbitMQClientImpl> namedClients;

    private final ManagedExecutor executorService;
    private final TlsConfig tlsConfig;
    private final RabbitMQClientsConfig rabbitMQClientsConfig;
    private final LaunchMode launchMode;
    private final Config config;

    public RabbitMQClients(RabbitMQClientsConfig rabbitMQClientsConfig, TlsConfig tlsConfig, ManagedExecutor executorService,
            LaunchMode launchMode) {
        this.rabbitMQClientsConfig = rabbitMQClientsConfig;
        this.tlsConfig = tlsConfig;
        this.executorService = executorService;
        this.namedClients = new HashMap<>();
        this.launchMode = launchMode;
        this.config = ConfigProvider.getConfig();
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
        if (name == null) {
            if (defaultClient == null) {
                defaultClient = new RabbitMQClientImpl(params, mc);
            }
            return defaultClient;
        } else {
            return namedClients.computeIfAbsent(name,
                    n -> new RabbitMQClientImpl(params, mc));
        }
    }

    private RabbitMQClientParams params(String name) {
        RabbitMQClientParams params = new RabbitMQClientParams();
        params.setName(name);
        params.setExecutorService(executorService);
        params.setLaunchMode(launchMode);
        params.setTlsConfig(tlsConfig);
        params.setConfig(name == null ? rabbitMQClientsConfig.defaultClient : rabbitMQClientsConfig.namedClients.get(name));
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
        if (defaultClient != null) {
            defaultClient.disconnect();
        }
        namedClients.forEach((k, v) -> {
            v.disconnect();
        });
    }
}
