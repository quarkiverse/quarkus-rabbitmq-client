package io.quarkiverse.rabbitmqclient.devmode;

import io.quarkiverse.rabbitmqclient.RabbitMQConfigTest;
import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

public class QuarkusRabbitMQDevModeMessagingTest extends RabbitMQConfigTest {

    @Testcontainers(disabledWithoutDocker = true)
    static class DevServicesShouldStartRabbitMQTest {

        @RegisterExtension
        static final QuarkusUnitTest unitTest = new QuarkusUnitTest() // Start unit test with your extension loaded
                .withEmptyApplication();
//                .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
//                        .addClasses(TestConfig.class, RabbitMQTestHelper.class, MessageResource.class, MessageService.class)
//                        .addAsResource(
//                                io.quarkiverse.rabbitmqclient.QuarkusRabbitMQConsumerTest.class
//                                        .getResource("/only-default.properties"),
//                                "application.properties")
//                        .addAsResource(
//                                io.quarkiverse.rabbitmqclient.QuarkusRabbitMQConsumerTest.class
//                                        .getResource("/rabbitmq/ca/cacerts.jks"),
//                                "rabbitmq/ca/cacerts.jks")
//                        .addAsResource(
//                                io.quarkiverse.rabbitmqclient.QuarkusRabbitMQConsumerTest.class
//                                        .getResource("/rabbitmq/client/client.jks"),
//                                "rabbitmq/client/client.jks"));

        @Test
        public void testRabbitMQConsumer() throws Exception {

            RestAssured.given().queryParam("msg", "WORLD").when().post("/send").then().statusCode(HttpStatus.SC_NO_CONTENT);

//            unitTest.modifySourceFile(MessageResource.class, s -> s.replaceAll("HELLO", "GOODBYE"));

            RestAssured.given().queryParam("msg", "WORLD").when().post("/send").then().statusCode(HttpStatus.SC_NO_CONTENT);
        }

    }
}
