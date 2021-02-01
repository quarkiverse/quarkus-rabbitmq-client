package io.quarkiverse.rabbitmqclient;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.rabbitmq.client.Connection;

import io.quarkiverse.rabbitmqclient.util.RabbitMQTestContainer;
import io.quarkiverse.rabbitmqclient.util.TestConfig;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;

@QuarkusTestResource(RabbitMQTestContainer.class)
public class QuarkusRabbitMQConnectionTest extends RabbitMQConfigTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest() // Start unit test with your extension loaded
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestConfig.class)
                    .addAsResource(
                            QuarkusRabbitMQConnectionTest.class
                                    .getResource("/testcontainer/rabbitmq/rabbitmq-properties.properties"),
                            "application.properties")
                    .addAsResource(QuarkusRabbitMQConnectionTest.class.getResource("/testcontainer/rabbitmq/ca/cacerts.jks"),
                            "rabbitmq/ca/cacerts.jks")
                    .addAsResource(QuarkusRabbitMQConnectionTest.class.getResource("/testcontainer/rabbitmq/client/client.jks"),
                            "rabbitmq/client/client.jks"));

    @Inject
    RabbitMQClientConfig config;

    @Inject
    TestConfig testConfig;

    @Inject
    RabbitMQClient rabbitMQClient;

    @Test
    public void testNonSSL() {
        testConfig.setupNonSll(config);
        Connection conn = rabbitMQClient.connect("test-connection-non-ssl");
        Assertions.assertNotNull(conn);
    }

    @Test
    public void testRabbitMQSSLDefault() {
        testConfig.setupBasicSsl(config);
        Connection conn = rabbitMQClient.connect("test-connection-ssl");
        Assertions.assertNotNull(conn);
    }

    @Test
    public void testRabbitMQSSLClientCert() {
        testConfig.setupClientCertSsl(config);
        Connection conn = rabbitMQClient.connect("test-connection-ssl-client-cert");
        Assertions.assertNotNull(conn);
    }

}
