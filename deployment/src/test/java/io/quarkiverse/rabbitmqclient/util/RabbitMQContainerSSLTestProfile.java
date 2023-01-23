package io.quarkiverse.rabbitmqclient.util;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.List;
import java.util.Map;

public class RabbitMQContainerSSLTestProfile implements QuarkusTestProfile {

    @Override
    public List<TestResourceEntry> testResources() {
        return List.of(new TestResourceEntry(RabbitMQTestContainerManager.class));
    }

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("io.quarkiverse.rabbitmqclient.devservice.useSsl", "true");
    }

}
