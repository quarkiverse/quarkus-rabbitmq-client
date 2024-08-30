package io.quarkiverse.rabbitmqclient.devmode;

import io.quarkiverse.rabbitmqclient.RabbitMQConfigTest;
import io.quarkiverse.rabbitmqclient.util.RabbitMQTestContainer;
import io.quarkiverse.rabbitmqclient.util.RabbitMQTestHelper;
import io.quarkus.test.QuarkusDevModeTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@QuarkusTestResource(RabbitMQTestContainer.class)
public class QuarkusRabbitMQDevModeTest extends RabbitMQConfigTest {

    @RegisterExtension
    static final QuarkusDevModeTest unitTest = new QuarkusDevModeTest() // Start unit test with your extension loaded
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(RabbitMQTestHelper.class, MessageResource.class, MessageService.class)
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
    @Disabled
    public void testRabbitMQConsumer() throws Exception {

        System.out.println("0 Checking resource!!!");
        RestAssured.given().queryParam("msg", "WORLD").when().post("/send").then().statusCode(HttpStatus.SC_NO_CONTENT);

        System.out.println("1 Modify class");
        unitTest.modifySourceFile(MessageResource.class, s -> s.replaceAll("HELLO", "GOODBYE"));

        System.out.println("2 Checking resource!!!");
        RestAssured.given().queryParam("msg", "WORLD").when().post("/send").then().statusCode(HttpStatus.SC_NO_CONTENT);
    }
}
