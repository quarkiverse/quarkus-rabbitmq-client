package io.quarkiverse.rabbitmqclient.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.util.TypeLiteral;

import com.rabbitmq.client.MetricsCollector;
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

    public Function<SyntheticCreationalContext<RabbitMQClient>, RabbitMQClient> createClient(String configId, String id,
            LaunchMode launchMode,
            ExecutorService executorService,
            MetricsType metricsType, boolean isDefault) {
        return ctx -> {
            RabbitMQClientParams params = new RabbitMQClientParams();
            params.setId(id);
            params.setExecutorService(executorService);
            params.setLaunchMode(launchMode);
            params.setConfig(this.config.getValue().clients().get(configId));
            params.setDefault(isDefault);

            return new RabbitMQClientImpl(params, createMetricsCollector(ctx, metricsType, Map.of("name", id)));
        };
    }

    private MetricsCollector createMetricsCollector(SyntheticCreationalContext<RabbitMQClient> ctx, MetricsType type,
            Map<String, String> metricsTags) {
        return switch (type) {
            case MICROMETER -> new QuarkusMicrometerMetricsCollector(metricsTags);
            case OPEN_TELEMETRY ->
                new QuarkusOpenTelemetryMetricsCollector(ctx.getInjectedReference(OpenTelemetry.class), metricsTags);
            case NOOP -> new NoOpMetricsCollector();
        };
    }
}
