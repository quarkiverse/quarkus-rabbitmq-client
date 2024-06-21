package io.quarkiverse.rabbitmqclient;

import java.util.Properties;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class QuarkusRabbitmqMinimalClientConfigTest extends RabbitMQConfigTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest() // Start unit test with your extension loaded
            .setFlatClassPath(true)
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(QuarkusRabbitmqMinimalClientConfigTest.class.getResource("/minimal-properties.properties"),
                            "application.properties"));

    @Inject
    RabbitMQClientsConfig config;

    @Test
    public void testConnectionFactoryProperties() {
        config.clients().forEach((n, c) -> {
            assertRabbitMQConfig(c);
        });
    }

    private void assertRabbitMQConfig(RabbitMQClientConfig config) {
        RabbitMQClientParams params = new RabbitMQClientParams();
        params.setConfig(config);

        Properties properties = RabbitMQHelper.newProperties(params);
        assertRabbitMQConfig(config, properties);
    }
}
