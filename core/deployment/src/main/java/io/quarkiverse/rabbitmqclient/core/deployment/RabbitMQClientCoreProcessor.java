package io.quarkiverse.rabbitmqclient.core.deployment;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import io.quarkiverse.rabbitmqclient.core.runtime.ClientShutdown;
import io.quarkus.core.Phase;
import io.quarkus.core.deployment.service.ServiceRegistrar;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.ExecutorBuildItem;

/**
 * Build steps shared by the RabbitMQ client extensions.
 */
class RabbitMQClientCoreProcessor {

    /**
     * Closes all clients while the main executor is still running. Closing a connection dispatches shutdown signals
     * to consumers on that executor; bean destruction ({@code autoClose}) happens after it has been shut down.
     * The service stops after the {@code ShutdownEvent} has been fired and before the executor is shut down.
     * Only registered if a client extension requested it.
     */
    // executorBuildItem is unused: it only orders this step after the executor has been created
    @BuildStep
    void closeClientsOnShutdown(List<CloseClientsOnShutdownBuildItem> requests, ServiceRegistrar reg,
            ExecutorBuildItem executorBuildItem) {
        if (requests.isEmpty()) {
            return;
        }
        reg.forService("io.quarkiverse.rabbitmqclient.close-clients")
                .atPhase(Phase.INFRASTRUCTURE)
                .after(ScheduledExecutorService.class)
                .before("io.quarkus.arc.startup-event")
                .onStart(ctx -> ctx.onStop(ClientShutdown::closeClients));
    }
}
