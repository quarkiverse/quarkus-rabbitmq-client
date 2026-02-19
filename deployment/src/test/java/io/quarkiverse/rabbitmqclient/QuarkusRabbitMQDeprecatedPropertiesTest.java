package io.quarkiverse.rabbitmqclient;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.rabbitmq.client.Connection;

import io.quarkiverse.rabbitmqclient.util.RabbitMQTestContainer;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;

@QuarkusTestResource(RabbitMQTestContainer.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class QuarkusRabbitMQDeprecatedPropertiesTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest() // Start unit test with your extension loaded
            .setFlatClassPath(true)
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(
                            QuarkusRabbitMQDeprecatedPropertiesTest.class
                                    .getResource("/rabbitmq/rabbitmq-deprecated-properties.properties"),
                            "application.properties")
                    .addAsResource(QuarkusRabbitMQDeprecatedPropertiesTest.class.getResource("/rabbitmq/ca/cacerts.jks"),
                            "rabbitmq/ca/cacerts.jks")
                    .addAsResource(QuarkusRabbitMQDeprecatedPropertiesTest.class.getResource("/rabbitmq/client/client.jks"),
                            "rabbitmq/client/client.jks"));

    @Inject
    RabbitMQClient rabbitMQClient;

    @Inject
    @NamedRabbitMQClient("ssl")
    RabbitMQClient sslRabbitMQClient;

    @Inject
    @NamedRabbitMQClient("mtls")
    RabbitMQClient mtlsRabbitMQClient;

    @Test
    @Order(1)
    public void testNonSSL() {
        Connection conn = rabbitMQClient.connect("test-connection-non-ssl");
        Assertions.assertNotNull(conn);
    }

    @Test
    @Order(2)
    public void testRabbitMQSSLDefault() {
        Connection conn = sslRabbitMQClient.connect("test-connection-ssl");
        Assertions.assertNotNull(conn);
    }

    @Test
    @Order(3)
    public void testRabbitMQSSLClientCert() {
        Connection conn = mtlsRabbitMQClient.connect("test-connection-ssl-client-cert");
        Assertions.assertNotNull(conn);
    }

}
