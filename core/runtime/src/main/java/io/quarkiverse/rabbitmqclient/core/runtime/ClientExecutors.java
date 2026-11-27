package io.quarkiverse.rabbitmqclient.core.runtime;

import java.util.concurrent.ExecutorService;

import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.virtual.threads.VirtualThreads;

/**
 * Resolves the {@link ExecutorService} a RabbitMQ client dispatches work on.
 */
public final class ClientExecutors {

    private ClientExecutors() {
    }

    /**
     * Resolves the executor for a client.
     *
     * @param ctx the creational context of the client bean; it must declare an {@code ExecutorService}
     *        injection point qualified with {@link VirtualThreads} when virtual threads are enabled.
     * @param virtualThreadsEnabled whether the client is configured to use virtual threads.
     * @param executorService the Quarkus executor, used when virtual threads are disabled.
     * @return the executor for the client.
     */
    public static ExecutorService resolveExecutorService(SyntheticCreationalContext<?> ctx,
            boolean virtualThreadsEnabled, ExecutorService executorService) {
        if (virtualThreadsEnabled) {
            return ctx.getInjectedReference(ExecutorService.class, VirtualThreads.Literal.INSTANCE);
        }
        return executorService;
    }
}
