package io.quarkiverse.rabbitmqclient;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.eclipse.microprofile.context.ManagedExecutor;

import io.quarkus.arc.DefaultBean;
import io.quarkus.runtime.TlsConfig;

/**
 * Producer class for creating in injecting a {@link RabbitMQClient} instance.
 *
 * @author b.passon
 */
@ApplicationScoped
public class RabbitMQClientProducer {

    private RabbitMQClientImpl rabbitMQClient;

    /**
     * Creates a singleton {@link RabbitMQClient}.
     *
     * @param config the {@link RabbitMQClientConfig} to use.
     * @param tlsConfig the {@link TlsConfig} to use.
     * @return a configured {@link RabbitMQClient}.
     */
    @DefaultBean
    @Singleton
    @Produces
    public RabbitMQClient rabbitMQClient(RabbitMQClientConfig config, TlsConfig tlsConfig, ManagedExecutor managedExecutor) {
        rabbitMQClient = new RabbitMQClientImpl(config, tlsConfig, managedExecutor);
        return rabbitMQClient;
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
        if (rabbitMQClient != null) {
            rabbitMQClient.disconnect();
        }
    }
}
