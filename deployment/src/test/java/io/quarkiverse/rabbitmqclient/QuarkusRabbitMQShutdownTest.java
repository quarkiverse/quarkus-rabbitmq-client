package io.quarkiverse.rabbitmqclient;

import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.ShutdownSignalException;

import io.quarkiverse.rabbitmqclient.util.RabbitMQTestContainer;
import io.quarkus.test.QuarkusExtensionTest;
import io.quarkus.test.common.WithTestResource;

/**
 * Verifies that clients are closed on shutdown while the Quarkus executor still runs, so consumers receive their
 * shutdown signal.
 */
@WithTestResource(RabbitMQTestContainer.class)
@Timeout(value = 60, unit = TimeUnit.SECONDS)
public class QuarkusRabbitMQShutdownTest {

    // The application and the undeploy listener see different class loaders, so a static field would not be shared.
    // A system property is visible to both.
    private static final String SHUTDOWN_SIGNAL_RECEIVED = "quarkus-rabbitmq-client.test.shutdown-signal-received";

    @RegisterExtension
    static final QuarkusExtensionTest unitTest = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(
                            QuarkusRabbitMQShutdownTest.class.getResource("/rabbitmq/rabbitmq-properties.properties"),
                            "application.properties"))
            // the default (-1) waits forever for a connection close, so a regression would hang the shutdown
            .overrideConfigKey("quarkus.rabbitmqclient.connection-close-timeout", "5000")
            .setAfterUndeployListener(QuarkusRabbitMQShutdownTest::assertShutdownSignalReceived);

    @Inject
    RabbitMQClient rabbitMQClient;

    @Test
    public void testConsumerReceivesShutdownSignalOnShutdown() throws Exception {
        System.clearProperty(SHUTDOWN_SIGNAL_RECEIVED);
        Connection connection = rabbitMQClient.connect("shutdown-test");
        Channel channel = connection.createChannel();
        // RabbitMQ 4 denies transient non-exclusive queues
        String queue = channel.queueDeclare("", false, true, true, null).getQueue();
        channel.basicConsume(queue, true, new DefaultConsumer(channel) {
            @Override
            public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
                System.setProperty(SHUTDOWN_SIGNAL_RECEIVED, "true");
            }
        });
    }

    private static void assertShutdownSignalReceived() {
        try {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (!Boolean.getBoolean(SHUTDOWN_SIGNAL_RECEIVED) && System.nanoTime() < deadline) {
                Thread.sleep(50);
            }
            Assertions.assertTrue(Boolean.getBoolean(SHUTDOWN_SIGNAL_RECEIVED),
                    "consumer did not receive a shutdown signal on application shutdown");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } finally {
            System.clearProperty(SHUTDOWN_SIGNAL_RECEIVED);
        }
    }
}
