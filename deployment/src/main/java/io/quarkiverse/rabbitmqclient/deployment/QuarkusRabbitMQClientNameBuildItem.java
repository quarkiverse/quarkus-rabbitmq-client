package io.quarkiverse.rabbitmqclient.deployment;

import io.quarkus.builder.item.MultiBuildItem;

public final class QuarkusRabbitMQClientNameBuildItem extends MultiBuildItem {

    private final String name;

    public QuarkusRabbitMQClientNameBuildItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
