package io.quarkiverse.rabbitmqclient;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThan;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.rabbitmq.client.Connection;

import io.quarkiverse.rabbitmqclient.util.RabbitMQTestContainer;
import io.quarkus.credentials.CredentialsProvider;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;

@QuarkusTestResource(RabbitMQTestContainer.class)
public class QuarkusRabbitMQCredentialsProviderRenewableTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest() // Start unit test with your extension loaded
            .setFlatClassPath(true)
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(
                            QuarkusRabbitMQConnectionTest.class
                                    .getResource("/rabbitmq/rabbitmq-default-credentials-provider-properties.properties"),
                            "application.properties"));

    @Inject
    RabbitMQClient rabbitMQClient;

    public static AtomicInteger counter = new AtomicInteger();

    @Test
    public void testConnectionFactoryProperties() {
        Connection conn = rabbitMQClient.connect();
        Assertions.assertNotNull(conn);
        Assertions.assertEquals(2, counter.get());

        await().pollInterval(Duration.ofSeconds(1)).untilAtomic(counter, greaterThan(2));
    }

    @SuppressWarnings("unused")
    @ApplicationScoped
    public static class MockableCredentialsProvider implements CredentialsProvider {
        @Override
        public Map<String, String> getCredentials(String credentialsProviderName) {
            if (credentialsProviderName.equalsIgnoreCase("custom")) {
                counter.incrementAndGet();
                Config config = ConfigProvider.getConfig();
                return Map.of(
                        CredentialsProvider.USER_PROPERTY_NAME, config.getValue("test.username", String.class),
                        CredentialsProvider.PASSWORD_PROPERTY_NAME, config.getValue("test.username", String.class),
                        CredentialsProvider.EXPIRATION_TIMESTAMP_PROPERTY_NAME, Instant.now().plusSeconds(10).toString());
            } else {
                throw new IllegalArgumentException("Credentials absent");
            }
        }
    }
}
