package io.quarkiverse.rabbitmqclient.util;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.eclipse.microprofile.config.ConfigProvider;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.util.Map;

public class RabbitMQTestContainerManager implements QuarkusTestResourceLifecycleManager {

    private final MountableFile keyFile = MountableFile.forClasspathResource("/rabbitmq/server/key.pem");
    private final MountableFile certFile = MountableFile.forClasspathResource("/rabbitmq/server/cert.pem");
    private final MountableFile caFile = MountableFile.forClasspathResource("/rabbitmq/ca/cacert.pem");
    private RabbitMQContainer rabbitmq;

    @Override
    public Map<String, String> start() {
        rabbitmq = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.11.3-management-alpine"));
        if (useSsl()) {
            rabbitmq = rabbitmq.withSSL(keyFile, certFile, caFile, RabbitMQContainer.SslVerification.VERIFY_PEER, false);
        }
        rabbitmq.start();
        Map<String, String> resultingConfig = Map.of(
        "quarkus.rabbitmqclient.virtual-host", "/vhost",
        "quarkus.rabbitmqclient.username", "guest",
        "quarkus.rabbitmqclient.password", "guest",
        "quarkus.rabbitmqclient.hostname", rabbitmq.getHost(),
        "quarkus.rabbitmqclient.port", useSsl() ? rabbitmq.getAmqpsPort().toString() : rabbitmq.getAmqpPort().toString()
        );
        return resultingConfig;
    }

    private boolean useSsl() {
        return "true".equalsIgnoreCase(ConfigProvider.getConfig().getConfigValue("io.quarkiverse.rabbitmqclient.devservice.useSsl").getValue());
    }

    @Override
    public void stop() {
        rabbitmq.close();
    }
}
