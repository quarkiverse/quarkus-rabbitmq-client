package io.quarkiverse.rabbitmqclient;

import javax.annotation.PreDestroy;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import io.quarkus.arc.DefaultBean;
import io.quarkus.runtime.TlsConfig;

/**
 * Producer class for creating in injecting a {@link RabbitMQClient} instance.
 *
 * @author b.passon
 */
@Singleton
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
    public RabbitMQClient rabbitMQClient(RabbitMQClientConfig config, TlsConfig tlsConfig) {
        if (rabbitMQClient == null) {
            rabbitMQClient = new RabbitMQClientImpl(config, tlsConfig);
        }
        return rabbitMQClient;
    }

    /**
     * Destroys the {@link RabbitMQClient} and closes all connections.
     */
    @PreDestroy
    public void destroy() {
        rabbitMQClient.disconnect();
    }
}
