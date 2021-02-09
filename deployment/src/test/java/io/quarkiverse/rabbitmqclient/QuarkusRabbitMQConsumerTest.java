package io.quarkiverse.rabbitmqclient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.rabbitmqclient.util.RabbitMQTestContainer;
import io.quarkiverse.rabbitmqclient.util.RabbitMQTestHelper;
import io.quarkiverse.rabbitmqclient.util.TestConfig;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;

@QuarkusTestResource(RabbitMQTestContainer.class)
public class QuarkusRabbitMQConsumerTest extends RabbitMQConfigTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest() // Start unit test with your extension loaded
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestConfig.class, RabbitMQTestHelper.class)
                    .addAsResource(
                            QuarkusRabbitMQConsumerTest.class
                                    .getResource("/testcontainer/rabbitmq/rabbitmq-properties.properties"),
                            "application.properties")
                    .addAsResource(QuarkusRabbitMQConsumerTest.class.getResource("/testcontainer/rabbitmq/ca/cacerts.jks"),
                            "rabbitmq/ca/cacerts.jks")
                    .addAsResource(QuarkusRabbitMQConsumerTest.class.getResource("/testcontainer/rabbitmq/client/client.jks"),
                            "rabbitmq/client/client.jks"));

    @Inject
    RabbitMQTestHelper rabbitMQTestHelper;

    @BeforeEach
    public void setup() throws IOException {
        rabbitMQTestHelper.connectClientServerSsl();
        rabbitMQTestHelper.declareExchange("receive-test");
        rabbitMQTestHelper.declareQueue("receive-test-queue", "receive-test");
    }

    @AfterEach
    public void cleanup() throws IOException {
        rabbitMQTestHelper.deleteQueue("receive-test-queue");
        rabbitMQTestHelper.deleteExchange("receive-test");
    }

    @Test
    public void testRabbitMQSSLClientCert() throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);
        boolean utActive = false;
        rabbitMQTestHelper.basicConsume("receive-test-queue", false, (tag, envelope, properties, body) -> {
            System.out.println(new String(body, StandardCharsets.UTF_8));
            cdl.countDown();
        });

        rabbitMQTestHelper.send("receive-test", "{'foo':'bar'}");
        cdl.await(1, TimeUnit.SECONDS);

    }

}
