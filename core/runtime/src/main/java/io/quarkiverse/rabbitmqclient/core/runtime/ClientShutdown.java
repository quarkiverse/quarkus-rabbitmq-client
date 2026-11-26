package io.quarkiverse.rabbitmqclient.core.runtime;

import java.io.IOException;

import jakarta.enterprise.inject.Any;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import io.quarkus.arc.ClientProxy;
import io.quarkus.arc.InstanceHandle;

/**
 * Closes all {@link CloseableClient} beans on application shutdown.
 */
public final class ClientShutdown {

    private static final Logger log = LoggerFactory.getLogger(ClientShutdown.class);

    private ClientShutdown() {
    }

    /**
     * Closes all RabbitMQ clients. Invoked from a service stop handler, before the main executor is shut down.
     */
    public static void closeClients() {
        ArcContainer container = Arc.container();
        if (container == null) {
            return;
        }
        for (InstanceHandle<?> handle : container.select(CloseableClient.class, Any.Literal.INSTANCE).handles()) {
            // the client proxy only implements the protocol's client interface, so unwrap it before casting
            if (!(ClientProxy.unwrap(handle.get()) instanceof CloseableClient client)) {
                continue;
            }
            try {
                client.close();
            } catch (IOException | RuntimeException e) {
                log.warn("Failed to close RabbitMQ client {}.", client.getId(), e);
            }
        }
    }
}
