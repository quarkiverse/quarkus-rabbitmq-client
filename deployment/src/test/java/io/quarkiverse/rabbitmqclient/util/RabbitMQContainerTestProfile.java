package io.quarkiverse.rabbitmqclient.util;

import io.quarkus.test.junit.QuarkusTestProfile;
import org.testcontainers.containers.RabbitMQContainer;

import java.util.List;

public class RabbitMQContainerTestProfile implements QuarkusTestProfile {

    @Override
    public List<TestResourceEntry> testResources() {
        return List.of(new TestResourceEntry(RabbitMQTestContainerManager.class));
    }

}
