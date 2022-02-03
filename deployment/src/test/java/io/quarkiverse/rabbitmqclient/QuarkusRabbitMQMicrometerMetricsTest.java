package io.quarkiverse.rabbitmqclient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.micrometer.core.instrument.MeterRegistry;
import io.quarkiverse.rabbitmqclient.util.RabbitMQTestContainer;
import io.quarkiverse.rabbitmqclient.util.RabbitMQTestHelper;
import io.quarkiverse.rabbitmqclient.util.TestConfig;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;

@QuarkusTestResource(RabbitMQTestContainer.class)
public class QuarkusRabbitMQMicrometerMetricsTest extends RabbitMQConfigTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest() // Start unit test with your extension loaded
            .setFlatClassPath(true)
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestConfig.class, RabbitMQTestHelper.class)
                    .addAsResource(
                            QuarkusRabbitMQMicrometerMetricsTest.class
                                    .getResource("/rabbitmq/rabbitmq-properties.properties"),
                            "application.properties")
                    .addAsResource(QuarkusRabbitMQMicrometerMetricsTest.class.getResource("/rabbitmq/ca/cacerts.jks"),
                            "rabbitmq/ca/cacerts.jks")
                    .addAsResource(QuarkusRabbitMQMicrometerMetricsTest.class.getResource("/rabbitmq/client/client.jks"),
                            "rabbitmq/client/client.jks"));

    @Inject
    RabbitMQTestHelper rabbitMQTestHelper;

    @Inject
    MeterRegistry meterRegistry;

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
    public void testRabbitMQConsumer() throws Exception {
        CountDownLatch cdl = new CountDownLatch(5);
        rabbitMQTestHelper.basicConsume("receive-test-queue", false, (tag, envelope, properties, body) -> {
            System.out.println(new String(body, StandardCharsets.UTF_8));
            cdl.countDown();
        });

        rabbitMQTestHelper.send("receive-test", "{'foo':'bar'}");
        rabbitMQTestHelper.send("receive-test", "{'foo':'bar'}");
        rabbitMQTestHelper.send("receive-test", "{'foo':'bar'}");
        rabbitMQTestHelper.send("receive-test", "{'foo':'bar'}");
        rabbitMQTestHelper.send("receive-test", "{'foo':'bar'}");

        Assertions.assertTrue(cdl.await(5, TimeUnit.SECONDS));

        meterRegistry.forEachMeter(m -> {
            if (m.getId().getName().startsWith("rabbitmq")) {
                System.out.println(m.getId().getName());
                m.measure().forEach(mm -> {
                    System.out.println("  " + mm.toString());
                });
            }
        });
    }
}
