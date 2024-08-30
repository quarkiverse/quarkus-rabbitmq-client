package io.quarkiverse.rabbitmqclient;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.quarkiverse.rabbitmqclient.util.RabbitMQTestContainer;
import io.quarkiverse.rabbitmqclient.util.RabbitMQTestHelper;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.WithTestResource;

@WithTestResource(RabbitMQTestContainer.class)
public class QuarkusRabbitMQMicrometerMetricsTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest() // Start unit test with your extension loaded
            .setFlatClassPath(true)
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(RabbitMQTestHelper.class)
                    .addAsResource(
                            QuarkusRabbitMQMicrometerMetricsTest.class
                                    .getResource("/rabbitmq/rabbitmq-properties.properties"),
                            "application.properties")
                    .addAsResource(
                            QuarkusRabbitMQMicrometerMetricsTest.class.getResource("/rabbitmq/ca/cacerts.jks"),
                            "rabbitmq/ca/cacerts.jks")
                    .addAsResource(
                            QuarkusRabbitMQMicrometerMetricsTest.class
                                    .getResource("/rabbitmq/client/client.jks"),
                            "rabbitmq/client/client.jks"));

    @Inject
    RabbitMQTestHelper rabbitMQTestHelper;

    final static SimpleMeterRegistry registry = new SimpleMeterRegistry();

    @BeforeAll
    static void setRegistry() {
        Metrics.addRegistry(registry);
    }

    @AfterAll()
    static void removeRegistry() {
        Metrics.removeRegistry(registry);
    }

    @BeforeEach
    public void setup() throws IOException {
        rabbitMQTestHelper.def().declareExchange("receive-test");
        rabbitMQTestHelper.def().declareQueue("receive-test-queue-1", "receive-test");
        rabbitMQTestHelper.def().declareQueue("receive-test-queue-2", "receive-test");
    }

    @AfterEach
    public void cleanup() throws IOException {
        rabbitMQTestHelper.def().deleteQueue("receive-test-queue-1");
        rabbitMQTestHelper.def().deleteQueue("receive-test-queue-2");
        rabbitMQTestHelper.def().deleteExchange("receive-test");
    }

    @Test
    public void testRabbitMQConsumer() throws Exception {
        CountDownLatch cdl1 = new CountDownLatch(5);
        rabbitMQTestHelper.def().basicConsume("receive-test-queue-1", false, (tag, envelope, properties, body) -> {
            cdl1.countDown();
        });

        CountDownLatch cdl2 = new CountDownLatch(5);
        rabbitMQTestHelper.ssl().basicConsume("receive-test-queue-2", false, (tag, envelope, properties, body) -> {
            cdl2.countDown();
        });

        rabbitMQTestHelper.def().send("receive-test", "{'foo':'bar'}");
        rabbitMQTestHelper.def().send("receive-test", "{'foo':'bar'}");
        rabbitMQTestHelper.def().send("receive-test", "{'foo':'bar'}");
        rabbitMQTestHelper.def().send("receive-test", "{'foo':'bar'}");
        rabbitMQTestHelper.def().send("receive-test", "{'foo':'bar'}");

        Assertions.assertTrue(cdl1.await(5, TimeUnit.SECONDS));
        Assertions.assertTrue(cdl2.await(5, TimeUnit.SECONDS));

        Assertions.assertEquals(1, registry.find("rabbitmq.consumed").tags("name", "<default>").counters().size());
        Assertions.assertEquals(5.0,
                registry.find("rabbitmq.consumed").tags("name", "<default>").counters().iterator().next().count());
        Assertions.assertEquals(1, registry.find("rabbitmq.consumed").tag("name", "ssl").counters().size());
        Assertions.assertEquals(5.0,
                registry.find("rabbitmq.consumed").tag("name", "ssl").counters().iterator().next().count());

        Assertions.assertEquals(1, registry.find("rabbitmq.connections").tags("name", "<default>").gauges().size());
        Assertions.assertEquals(1.0,
                registry.find("rabbitmq.connections").tags("name", "<default>").gauges().iterator().next().value());
        Assertions.assertEquals(1, registry.find("rabbitmq.connections").tag("name", "ssl").gauges().size());
        Assertions.assertEquals(1.0,
                registry.find("rabbitmq.connections").tag("name", "ssl").gauges().iterator().next().value());
    }
}
