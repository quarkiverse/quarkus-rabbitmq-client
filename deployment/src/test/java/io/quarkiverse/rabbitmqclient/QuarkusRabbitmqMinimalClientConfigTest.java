package io.quarkiverse.rabbitmqclient;

import java.util.Properties;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.runtime.TlsConfig;
import io.quarkus.test.QuarkusUnitTest;

public class QuarkusRabbitmqMinimalClientConfigTest extends RabbitMQConfigTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest() // Start unit test with your extension loaded
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(QuarkusRabbitmqMinimalClientConfigTest.class.getResource("/minimal-properties.properties"),
                            "application.properties"));

    @Inject
    RabbitMQClientConfig config;

    @Inject
    TlsConfig tlsConfig;

    @Test
    public void testConnectionFactoryProperties() {
        Properties properties = RabbitMQHelper.newProperties(config, tlsConfig);
        assertRabbitMQConfig(config, tlsConfig, properties);
    }
}
