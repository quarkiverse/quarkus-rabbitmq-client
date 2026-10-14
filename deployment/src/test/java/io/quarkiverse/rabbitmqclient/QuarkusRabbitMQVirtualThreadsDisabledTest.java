package io.quarkiverse.rabbitmqclient;

import static io.quarkiverse.rabbitmqclient.util.ConsumerThreadProbe.consumeOnThread;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.rabbitmqclient.util.ConsumerThreadProbe;
import io.quarkiverse.rabbitmqclient.util.RabbitMQTestContainer;
import io.quarkus.test.QuarkusExtensionTest;
import io.quarkus.test.common.WithTestResource;

@WithTestResource(RabbitMQTestContainer.class)
public class QuarkusRabbitMQVirtualThreadsDisabledTest {

    @RegisterExtension
    static final QuarkusExtensionTest unitTest = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(ConsumerThreadProbe.class)
                    .addAsResource(
                            QuarkusRabbitMQVirtualThreadsDisabledTest.class
                                    .getResource("/rabbitmq/rabbitmq-virtual-threads-disabled-properties.properties"),
                            "application.properties"));

    @Inject
    RabbitMQClient rabbitMQClient;

    @Test
    public void testConsumerFallsBackToPlatformThread() throws Exception {
        Thread consumerThread = consumeOnThread(rabbitMQClient, "vt-globally-disabled-queue");
        Assertions.assertFalse(consumerThread.isVirtual());
    }
}
