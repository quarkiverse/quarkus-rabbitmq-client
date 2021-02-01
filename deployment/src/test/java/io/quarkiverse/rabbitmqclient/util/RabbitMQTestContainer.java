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

    private final MountableFile keyFile = MountableFile.forClasspathResource("/testcontainer/rabbitmq/server/key.pem");
    private final MountableFile certFile = MountableFile.forClasspathResource("/testcontainer/rabbitmq/server/cert.pem");
    private final MountableFile caFile = MountableFile.forClasspathResource("/testcontainer/rabbitmq/ca/cacert.pem");
    private final MountableFile configFile = MountableFile.forClasspathResource("/testcontainer/rabbitmq/rabbit.conf");
    private RabbitMQContainer rabbitmq;

    @Override
    public Map<String, String> start() {
        rabbitmq = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.8.11-management-alpine"))
                .withCopyFileToContainer(keyFile, "/etc/rabbitmq/rabbitmq_key.pem")
                .withCopyFileToContainer(certFile, "/etc/rabbitmq/rabbitmq_cert.pem")
                .withCopyFileToContainer(caFile, "/etc/rabbitmq/ca_cert.pem")
                .withRabbitMQConfig(configFile);

        rabbitmq.start();
        Map<String, String> testConfig = new HashMap<>();
        testConfig.put(AMQP_PORT, rabbitmq.getAmqpPort().toString());
        testConfig.put(AMQPS_PORT, rabbitmq.getAmqpsPort().toString());
        return testConfig;
    }

    @Override
    public void stop() {
        rabbitmq.close();
    }
}
