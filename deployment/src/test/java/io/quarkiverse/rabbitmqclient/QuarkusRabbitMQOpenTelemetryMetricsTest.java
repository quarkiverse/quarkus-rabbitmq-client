package io.quarkiverse.rabbitmqclient;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

import org.awaitility.Awaitility;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter;
import io.quarkiverse.rabbitmqclient.util.OpenTelemetryInMemoryProducer;
import io.quarkiverse.rabbitmqclient.util.RabbitMQTestContainer;
import io.quarkiverse.rabbitmqclient.util.RabbitMQTestHelper;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;

@QuarkusTestResource(RabbitMQTestContainer.class)
public class QuarkusRabbitMQOpenTelemetryMetricsTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest() // Start unit test with your extension loaded
            .setFlatClassPath(true)
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(RabbitMQTestHelper.class, OpenTelemetryInMemoryProducer.class)
                    .addAsResource(
                            QuarkusRabbitMQOpenTelemetryMetricsTest.class
                                    .getResource("/rabbitmq/rabbitmq-opentelemetry-properties.properties"),
                            "application.properties")
                    .addAsResource(
                            QuarkusRabbitMQOpenTelemetryMetricsTest.class.getResource("/rabbitmq/ca/cacerts.jks"),
                            "rabbitmq/ca/cacerts.jks")
                    .addAsResource(
                            QuarkusRabbitMQOpenTelemetryMetricsTest.class
                                    .getResource("/rabbitmq/client/client.jks"),
                            "rabbitmq/client/client.jks"));

    @Inject
    RabbitMQTestHelper rabbitMQTestHelper;

    @Inject
    InMemoryMetricExporter exporter;

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

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> !exporter.getFinishedMetricItems().isEmpty());
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> !getMetrics("rabbitmq.consumed", "default").isEmpty());

        Optional<Long> v = getLastPointValue("rabbitmq.consumed", "default");
        Assertions.assertTrue(v.isPresent());
        Assertions.assertEquals(5, v.get());

        v = getLastPointValue("rabbitmq.consumed", "ssl");
        Assertions.assertTrue(v.isPresent());
        Assertions.assertEquals(5, v.get());

        v = getLastPointValue("rabbitmq.connections", "default");
        Assertions.assertTrue(v.isPresent());
        Assertions.assertEquals(1, v.get());

        v = getLastPointValue("rabbitmq.connections", "ssl");
        Assertions.assertTrue(v.isPresent());
        Assertions.assertEquals(1, v.get());

    }

    private List<MetricData> getMetrics(String name, String client) {
        return exporter.getFinishedMetricItems().stream()
                .filter(metricData -> name == null || metricData.getName().equals(name))
                .filter(metricData -> client == null || metricData.getData()
                        .getPoints().stream()
                        .anyMatch(point -> attributeMatches("name", client, point.getAttributes())))
                .toList();
    }

    private <T> Optional<T> getLastPointValue(String name, String client) {
        var items = getMetrics(name, client);
        if (items.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(items.get(items.size() - 1))
                .flatMap(m -> findValue(m, "name", client));
    }

    private static boolean attributeMatches(String key, String value, Attributes attributes) {
        if (value == null) {
            return true;// any match
        }
        Object v = attributes.asMap().get(AttributeKey.stringKey(key));
        if (v == null) {
            return false;
        }
        return v.toString().equals(value);
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> findValue(MetricData m, String key, String value) {
        return (Optional<T>) Optional.ofNullable(m.getData())
                .flatMap(d -> d.getPoints().stream()
                        .filter(p -> attributeMatches(key, value, p.getAttributes()))
                        .map(p -> getValue(m.getType(), p))
                        .filter(Objects::nonNull)
                        .findAny());
    }

    @SuppressWarnings("unchecked")
    private <T> T getValue(MetricDataType type, PointData p) {
        return switch (type) {
            case LONG_GAUGE, LONG_SUM -> (T) Long.valueOf(((LongPointData) p).getValue());
            case DOUBLE_GAUGE -> null;
            case DOUBLE_SUM -> null;
            case SUMMARY -> null;
            case HISTOGRAM -> null;
            case EXPONENTIAL_HISTOGRAM -> null;
        };
    }
}
