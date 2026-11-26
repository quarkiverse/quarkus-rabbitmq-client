package io.quarkiverse.rabbitmqclient.util;

import java.util.HashMap;
import java.util.Map;

import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class RabbitMQTestContainer implements QuarkusTestResourceLifecycleManager {

    private static final String AMQP_PORT = "test.amqp-port";
    private static final String HOSTNAME = "test.hostname";
    private static final String USERNAME = "test.username";
    private static final String PASSWORD = "test.password";

    private org.testcontainers.rabbitmq.RabbitMQContainer rabbitmq;

    @Override
    public Map<String, String> start() {
        // Keep in sync with deployment/src/test/java/io/quarkiverse/rabbitmqclient/util/RabbitMQTestContainer.java
        rabbitmq = new RabbitMQContainer(DockerImageName.parse("rabbitmq:4.4.1-alpine"));
        rabbitmq.start();
        Map<String, String> testConfig = new HashMap<>();

        testConfig.put(HOSTNAME, rabbitmq.getHost());
        testConfig.put(AMQP_PORT, rabbitmq.getAmqpPort().toString());
        testConfig.put(USERNAME, rabbitmq.getAdminUsername());
        testConfig.put(PASSWORD, rabbitmq.getAdminPassword());

        return testConfig;
    }

    @Override
    public void stop() {
        rabbitmq.close();
    }
}
