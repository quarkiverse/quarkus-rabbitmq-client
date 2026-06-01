package io.quarkiverse.rabbitmqclient.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.util.TypeLiteral;

import com.rabbitmq.client.NoOpMetricsCollector;

import io.opentelemetry.api.OpenTelemetry;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkiverse.rabbitmqclient.RabbitMQClients;
import io.quarkiverse.rabbitmqclient.RabbitMQClientsConfig;
import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class RabbitMQRecorder {

    private final RuntimeValue<RabbitMQClientsConfig> config;

    public RabbitMQRecorder(RuntimeValue<RabbitMQClientsConfig> config) {
        this.config = config;
    }

    public Function<SyntheticCreationalContext<RabbitMQClients>, RabbitMQClients> initClients(String defaultId) {
        return ctx -> {
            Instance<RabbitMQClient> clients = ctx.getInjectedReference(new TypeLiteral<Instance<RabbitMQClient>>() {
            });
            Map<String, RabbitMQClient> resolvedClients = new HashMap<>();
            for (RabbitMQClient client : clients) {
                resolvedClients.put(client.getId(), client);
            }
            return new RabbitMQClientsImpl(defaultId, resolvedClients);
        };
    }

    // RUNTIME INIT
    public Function<SyntheticCreationalContext<RabbitMQClient>, RabbitMQClient> createClientWithNoopMetrics(String configId,
            String id, LaunchMode launchMode, ExecutorService executorService, boolean isDefault) {
        return ctx -> {
            RabbitMQClientParams params = new RabbitMQClientParams();
            params.setId(id);
            params.setExecutorService(executorService);
            params.setLaunchMode(launchMode);
            params.setConfig(this.config.getValue().clients().get(configId));
            params.setDefault(isDefault);

            return new RabbitMQClientImpl(params, new NoOpMetricsCollector());
        };
    }

    // RUNTIME INIT
    public Function<SyntheticCreationalContext<RabbitMQClient>, RabbitMQClient> createClientWithMicrometerMetrics(
            String configId, String id, LaunchMode launchMode, ExecutorService executorService,
            boolean isDefault) {
        return ctx -> {
            RabbitMQClientParams params = new RabbitMQClientParams();
            params.setId(id);
            params.setExecutorService(executorService);
            params.setLaunchMode(launchMode);
            params.setConfig(this.config.getValue().clients().get(configId));
            params.setDefault(isDefault);

            return new RabbitMQClientImpl(params, new QuarkusMicrometerMetricsCollector(Map.of("name", id)));
        };
    }

    // RUNTIME INIT
    public Function<SyntheticCreationalContext<RabbitMQClient>, RabbitMQClient> createClientWithOpenTelemetryMetrics(
            String configId, String id, LaunchMode launchMode, ExecutorService executorService,
            boolean isDefault) {
        return ctx -> {
            RabbitMQClientParams params = new RabbitMQClientParams();
            params.setId(id);
            params.setExecutorService(executorService);
            params.setLaunchMode(launchMode);
            params.setConfig(this.config.getValue().clients().get(configId));
            params.setDefault(isDefault);
            return new RabbitMQClientImpl(params, new QuarkusOpenTelemetryMetricsCollector(
                    ctx.getInjectedReference(OpenTelemetry.class), Map.of("name", id)));
        };
    }
}
