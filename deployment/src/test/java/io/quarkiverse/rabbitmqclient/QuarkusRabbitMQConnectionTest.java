package io.quarkiverse.rabbitmqclient;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.rabbitmq.client.Connection;

import io.quarkiverse.rabbitmqclient.util.RabbitMQTestContainer;
import io.quarkiverse.rabbitmqclient.util.TestConfig;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;

@QuarkusTestResource(RabbitMQTestContainer.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class QuarkusRabbitMQConnectionTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest() // Start unit test with your extension loaded
            .setFlatClassPath(true)
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestConfig.class)
                    .addAsResource(
                            QuarkusRabbitMQConnectionTest.class
                                    .getResource("/rabbitmq/rabbitmq-properties.properties"),
                            "application.properties")
                    .addAsResource(QuarkusRabbitMQConnectionTest.class.getResource("/rabbitmq/ca/cacerts.jks"),
                            "rabbitmq/ca/cacerts.jks")
                    .addAsResource(QuarkusRabbitMQConnectionTest.class.getResource("/rabbitmq/client/client.jks"),
                            "rabbitmq/client/client.jks"));

    @Inject
    RabbitMQClientsConfig configs;

    @Inject
    TestConfig testConfig;

    @Inject
    RabbitMQClient rabbitMQClient;

    @Inject
    @NamedRabbitMQClient("other")
    RabbitMQClient otherRabbitMQClient;

    @Test
    @Order(1)
    public void testNonSSL() {
        testConfig.setupNonSll(configs);
        testConfig.setupNonSll("other", configs);
        Connection conn = rabbitMQClient.connect("test-connection-non-ssl");
        Connection other = otherRabbitMQClient.connect("test-other-connection-non-ssl");
        Assertions.assertNotNull(conn);
        Assertions.assertNotNull(other);
    }

    @Test
    @Order(2)
    public void testRabbitMQSSLDefault() {
        testConfig.setupBasicSsl(configs);
        testConfig.setupBasicSsl("other", configs);
        Connection conn = rabbitMQClient.connect("test-connection-ssl");
        Connection other = otherRabbitMQClient.connect("test-other-connection-ssl");
        Assertions.assertNotNull(conn);
        Assertions.assertNotNull(other);
    }

    @Test
    @Order(3)
    public void testRabbitMQSSLClientCert() {
        testConfig.setupClientCertSsl(configs);
        testConfig.setupClientCertSsl("other", configs);
        Connection conn = rabbitMQClient.connect("test-connection-ssl-client-cert");
        Connection other = otherRabbitMQClient.connect("test-other-connection-ssl-client-cert");
        Assertions.assertNotNull(conn);
        Assertions.assertNotNull(other);
    }

}
