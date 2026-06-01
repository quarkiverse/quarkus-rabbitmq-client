package io.quarkiverse.rabbitmqclient.deployment;

import java.util.Objects;

import io.quarkiverse.rabbitmqclient.runtime.MetricsType;
import io.quarkus.builder.item.MultiBuildItem;

public final class QuarkusRabbitMQClientBuildItem extends MultiBuildItem {

    private final String configId;
    private final String id;
    private final MetricsType metricsType;
    private final boolean isDefaultClient;

    public QuarkusRabbitMQClientBuildItem(String configId, String id, MetricsType metricsType, boolean isDefaultClient) {
        this.configId = configId;
        this.id = id;
        this.metricsType = metricsType;
        this.isDefaultClient = isDefaultClient;
    }

    public String getId() {
        return id;
    }

    public MetricsType getMetricsType() {
        return metricsType;
    }

    public String getConfigId() {
        return configId;
    }

    public boolean isDefaultClient() {
        return isDefaultClient;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QuarkusRabbitMQClientBuildItem that))
            return false;
        return isDefaultClient == that.isDefaultClient && Objects.equals(configId, that.configId) && Objects.equals(id, that.id)
                && metricsType == that.metricsType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(configId, id, metricsType, isDefaultClient);
    }
}
