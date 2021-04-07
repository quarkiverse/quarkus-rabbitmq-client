package io.quarkiverse.rabbitmqclient;

import java.util.Properties;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.runtime.TlsConfig;
import io.quarkus.test.QuarkusUnitTest;

public class QuarkusRabbitmqNonDefaultClientConfigTest extends RabbitMQConfigTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest() // Start unit test with your extension loaded
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(
                            QuarkusRabbitmqNonDefaultClientConfigTest.class.getResource("/non-default-properties.properties"),
                            "application.properties"));

    @Inject
    RabbitMQClientsConfig config;

    @Inject
    TlsConfig tlsConfig;

    @Test
    public void testConnectionFactoryProperties() {
        RabbitMQClientParams params = new RabbitMQClientParams();
        params.setConfig(config.defaultClient);
        params.setTlsConfig(tlsConfig);

        Properties properties = RabbitMQHelper.newProperties(params);
        assertRabbitMQConfig(config.defaultClient, tlsConfig, properties);
        Assertions.assertEquals(config.defaultClient.connectionCloseTimeout, 200);
    }

}
