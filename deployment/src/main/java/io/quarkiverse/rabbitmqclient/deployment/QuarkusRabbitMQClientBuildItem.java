package io.quarkiverse.rabbitmqclient.deployment;

import java.util.Objects;

import io.quarkus.builder.item.MultiBuildItem;

public final class QuarkusRabbitMQClientBuildItem extends MultiBuildItem {

    private final String name;
    private final boolean metricsEnabled;

    public QuarkusRabbitMQClientBuildItem(String name, boolean metricsEnabled) {
        this.name = name;
        this.metricsEnabled = metricsEnabled;
    }

    public String getName() {
        return name;
    }

    public boolean isMetricsEnabled() {
        return metricsEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuarkusRabbitMQClientBuildItem))
            return false;
        QuarkusRabbitMQClientBuildItem that = (QuarkusRabbitMQClientBuildItem) o;
        return isMetricsEnabled() == that.isMetricsEnabled() && getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), isMetricsEnabled());
    }
}
