package io.quarkiverse.rabbitmqclient.runtime;

import java.util.Properties;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.rabbitmqclient.RabbitMQClientsConfig;
import io.quarkus.test.QuarkusUnitTest;

public class QuarkusRabbitmqNonDefaultClientConfigTest extends RabbitMQConfigTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest() // Start unit test with your extension loaded
            .setFlatClassPath(true)
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(
                            QuarkusRabbitmqNonDefaultClientConfigTest.class.getResource("/non-default-properties.properties"),
                            "application.properties"));

    @Inject
    RabbitMQClientsConfig config;

    @Test
    public void testConnectionFactoryProperties() {
        RabbitMQClientParams params = new RabbitMQClientParams();
        params.setConfig(config.clients().get(RabbitMQClientsConfig.DEFAULT_CLIENT_NAME));

        Properties properties = RabbitMQHelper.newProperties(params);
        assertRabbitMQConfig(config.clients().get(RabbitMQClientsConfig.DEFAULT_CLIENT_NAME), properties);
        Assertions.assertEquals(200, config.clients().get(RabbitMQClientsConfig.DEFAULT_CLIENT_NAME).connectionCloseTimeout());
    }

}
