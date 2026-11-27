package io.quarkiverse.rabbitmqclient.core.deployment;

import io.quarkus.builder.item.MultiBuildItem;

/**
 * Produced by a RabbitMQ client extension whose client beans have
 * {@link io.quarkiverse.rabbitmqclient.core.runtime.CloseableClient} as a bean type, to have them closed on shutdown
 * before the Quarkus executor is shut down.
 */
public final class CloseClientsOnShutdownBuildItem extends MultiBuildItem {
}
