package io.quarkiverse.rabbitmqclient;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

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
public class QuarkusRabbitMQCredentialsProviderNamedTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest() // Start unit test with your extension loaded
            .setFlatClassPath(true)
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(
                            QuarkusRabbitMQConnectionTest.class
                                    .getResource("/rabbitmq/rabbitmq-named-credentials-provider-properties.properties"),
                            "application.properties"));

    @Inject
    RabbitMQClient rabbitMQClient;

    @Test
    public void testConnectionFactoryProperties() {
        Connection conn = rabbitMQClient.connect();
        Assertions.assertNotNull(conn);
    }

    @ApplicationScoped
    public static class DefaultCredentialsProvider implements CredentialsProvider {

        @Override
        public Map<String, String> getCredentials(String credentialsProviderName) {
            throw new IllegalStateException("Incorrect CredentialsProvider selected");
        }
    }

    @ApplicationScoped
    @Named("rabbitmq-credentials-provider")
    public static class MockableCredentialsProvider implements CredentialsProvider {
        @Override
        public Map<String, String> getCredentials(String credentialsProviderName) {
            if (credentialsProviderName.equalsIgnoreCase("custom")) {
                Config config = ConfigProvider.getConfig();
                return Map.of(
                        CredentialsProvider.USER_PROPERTY_NAME, config.getValue("test.username", String.class),
                        CredentialsProvider.PASSWORD_PROPERTY_NAME, config.getValue("test.username", String.class));
            } else {
                throw new IllegalArgumentException("Credentials absent");
            }
        }
    }
}
