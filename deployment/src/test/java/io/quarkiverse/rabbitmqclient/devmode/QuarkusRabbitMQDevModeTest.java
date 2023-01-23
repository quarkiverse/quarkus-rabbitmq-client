package io.quarkiverse.rabbitmqclient.devmode;

import io.quarkus.test.junit.TestProfile;
import org.apache.http.HttpStatus;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.rabbitmqclient.RabbitMQConfigTest;
import io.quarkiverse.rabbitmqclient.util.RabbitMQContainerTestProfile;
import io.quarkiverse.rabbitmqclient.util.RabbitMQTestHelper;
import io.quarkiverse.rabbitmqclient.util.TestConfig;
import io.quarkus.test.QuarkusDevModeTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.restassured.RestAssured;

@TestProfile(RabbitMQContainerTestProfile.class)
public class QuarkusRabbitMQDevModeTest extends RabbitMQConfigTest {

    @RegisterExtension
    static final QuarkusDevModeTest unitTest = new QuarkusDevModeTest() // Start unit test with your extension loaded
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestConfig.class, RabbitMQTestHelper.class, MessageResource.class, MessageService.class)
                    .addAsResource(
                            io.quarkiverse.rabbitmqclient.QuarkusRabbitMQConsumerTest.class
                                    .getResource("/rabbitmq/rabbitmq-properties.properties"),
                            "application.properties")
                    .addAsResource(
                            io.quarkiverse.rabbitmqclient.QuarkusRabbitMQConsumerTest.class
                                    .getResource("/rabbitmq/ca/cacerts.jks"),
                            "rabbitmq/ca/cacerts.jks")
                    .addAsResource(
                            io.quarkiverse.rabbitmqclient.QuarkusRabbitMQConsumerTest.class
                                    .getResource("/rabbitmq/client/client.jks"),
                            "rabbitmq/client/client.jks"));

    @Test
    public void testRabbitMQConsumer() throws Exception {

        RestAssured.given().queryParam("msg", "WORLD").when().post("/send").then().statusCode(HttpStatus.SC_NO_CONTENT);

        unitTest.modifySourceFile(MessageResource.class, s -> s.replaceAll("HELLO", "GOODBYE"));

        RestAssured.given().queryParam("msg", "WORLD").when().post("/send").then().statusCode(HttpStatus.SC_NO_CONTENT);
    }
}
