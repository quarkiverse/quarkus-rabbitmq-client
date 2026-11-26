package io.quarkiverse.rabbitmqclient.util;

import java.util.HashMap;
import java.util.Map;

import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class RabbitMQTestContainer implements QuarkusTestResourceLifecycleManager {

    private static final String AMQPS_PORT = "test.amqps-port";
    private static final String AMQP_PORT = "test.amqp-port";
    private static final String HOSTNAME = "test.hostname";
    private static final String USERNAME = "test.username";
    private static final String PASSWORD = "test.password";

    private final MountableFile keyFile = MountableFile.forClasspathResource("/rabbitmq/server/key.pem");
    private final MountableFile certFile = MountableFile.forClasspathResource("/rabbitmq/server/cert.pem");
    private final MountableFile caFile = MountableFile.forClasspathResource("/rabbitmq/ca/cacert.pem");
    private final MountableFile tlsConfFile = MountableFile.forClasspathResource("/rabbitmq/tls.conf");
    private org.testcontainers.rabbitmq.RabbitMQContainer rabbitmq;

    @Override
    public Map<String, String> start() {
        // Keep in sync with integration-tests/src/test/java/io/quarkiverse/rabbitmqclient/util/RabbitMQTestContainer.java
        rabbitmq = new RabbitMQContainer(DockerImageName.parse("rabbitmq:4.4.1-alpine"))
                .withCopyFileToContainer(tlsConfFile, "/etc/rabbitmq/conf.d/20-tls.conf")
                .withCopyFileToContainer(certFile, "/etc/rabbitmq/rabbitmq_cert.pem")
                .withCopyFileToContainer(caFile, "/etc/rabbitmq/ca_cert.pem")
                .withCopyFileToContainer(keyFile, "/etc/rabbitmq/rabbitmq_key.pem");
        rabbitmq.start();
        Map<String, String> testConfig = new HashMap<>();

        testConfig.put(HOSTNAME, rabbitmq.getHost());
        testConfig.put(AMQP_PORT, rabbitmq.getAmqpPort().toString());
        testConfig.put(AMQPS_PORT, rabbitmq.getAmqpsPort().toString());
        testConfig.put(USERNAME, rabbitmq.getAdminUsername());
        testConfig.put(PASSWORD, rabbitmq.getAdminPassword());

        return testConfig;
    }

    @Override
    public void stop() {
        rabbitmq.close();
    }
}
