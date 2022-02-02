package io.quarkiverse.rabbitmqclient.util;

import java.util.HashMap;
import java.util.Map;

import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class RabbitMQTestContainer implements QuarkusTestResourceLifecycleManager {

    private static final String AMQPS_PORT = "test.amqps-port";
    private static final String AMQP_PORT = "test.amqp-port";
    private static final String HOSTNAME = "test.hostname";

    private final MountableFile keyFile = MountableFile.forClasspathResource("/rabbitmq/server/key.pem");
    private final MountableFile certFile = MountableFile.forClasspathResource("/rabbitmq/server/cert.pem");
    private final MountableFile caFile = MountableFile.forClasspathResource("/rabbitmq/ca/cacert.pem");
    private final MountableFile configFile = MountableFile.forClasspathResource("/rabbitmq/rabbit.conf");
    private RabbitMQContainer rabbitmq;

    @Override
    public Map<String, String> start() {
        rabbitmq = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.7.25-management-alpine"))
                .withSSL(keyFile, certFile, caFile, RabbitMQContainer.SslVerification.VERIFY_PEER, false);
        rabbitmq.start();
        Map<String, String> testConfig = new HashMap<>();
        testConfig.put(HOSTNAME, rabbitmq.getHost());
        testConfig.put(AMQP_PORT, rabbitmq.getAmqpPort().toString());
        testConfig.put(AMQPS_PORT, rabbitmq.getAmqpsPort().toString());
        return testConfig;
    }

    @Override
    public void stop() {
        rabbitmq.close();
    }
}
