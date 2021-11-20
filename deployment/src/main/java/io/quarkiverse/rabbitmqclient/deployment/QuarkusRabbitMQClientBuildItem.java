package io.quarkiverse.rabbitmqclient.deployment;

import java.util.Objects;

import io.quarkus.builder.item.MultiBuildItem;

public final class QuarkusRabbitMQClientBuildItem extends MultiBuildItem {

    private final String name;

    public QuarkusRabbitMQClientBuildItem() {
        this.name = null;
    }

    public QuarkusRabbitMQClientBuildItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuarkusRabbitMQClientBuildItem))
            return false;
        QuarkusRabbitMQClientBuildItem that = (QuarkusRabbitMQClientBuildItem) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
