package io.quarkiverse.rabbitmqclient;

import static io.quarkiverse.rabbitmqclient.util.ConsumerThreadProbe.consumeOnThread;
import static io.quarkiverse.rabbitmqclient.util.ConsumerThreadProbe.isVirtual;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.rabbitmqclient.util.ConsumerThreadProbe;
import io.quarkiverse.rabbitmqclient.util.RabbitMQTestContainer;
import io.quarkus.test.QuarkusExtensionTest;
import io.quarkus.test.common.WithTestResource;

@EnabledForJreRange(min = JRE.JAVA_21)
@WithTestResource(RabbitMQTestContainer.class)
public class QuarkusRabbitMQVirtualThreadsConsumerTest {

    @RegisterExtension
    static final QuarkusExtensionTest unitTest = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(ConsumerThreadProbe.class)
                    .addAsResource(
                            QuarkusRabbitMQVirtualThreadsConsumerTest.class
                                    .getResource("/rabbitmq/rabbitmq-virtual-threads-properties.properties"),
                            "application.properties"));

    @Inject
    RabbitMQClient rabbitMQClient;

    @Inject
    @NamedRabbitMQClient("other")
    RabbitMQClient otherRabbitMQClient;

    @Test
    public void testConsumerOnVirtualThread() throws Exception {
        Thread consumerThread = consumeOnThread(rabbitMQClient, "vt-enabled-queue");
        Assertions.assertTrue(isVirtual(consumerThread));
    }

    @Test
    public void testConsumerOnPlatformThread() throws Exception {
        Thread consumerThread = consumeOnThread(otherRabbitMQClient, "vt-disabled-queue");
        Assertions.assertFalse(isVirtual(consumerThread));
    }
}
