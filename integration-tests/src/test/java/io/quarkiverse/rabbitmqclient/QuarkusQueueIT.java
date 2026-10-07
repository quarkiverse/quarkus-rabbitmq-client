package io.quarkiverse.rabbitmqclient;

import static io.restassured.RestAssured.given;

import jakarta.inject.Inject;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkiverse.rabbitmq.resource.RabbitMQSupport;
import io.quarkiverse.rabbitmqclient.util.RabbitMQTestContainer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@QuarkusTestResource(RabbitMQTestContainer.class)
public class QuarkusQueueIT {

    private static final String EXCHANGE = "sample-exchange";
    private static final String QUEUE = "sample-queue";

    @Inject
    RabbitMQSupport rabbitMQSupport;

    @BeforeEach
    void reset() {
        rabbitMQSupport.reset();
    }

    @Test
    void createExchangeGivesCreated() {
        given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("name", EXCHANGE)
                .post("/exchange")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, CoreMatchers.endsWith("/exchange/" + EXCHANGE));
    }

    @Test
    void createQueueGivesCreated() {
        given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("name", EXCHANGE)
                .post("/exchange")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, CoreMatchers.endsWith("/exchange/" + EXCHANGE));

        given()
                .contentType("application/x-www-form-urlencoded")
                .pathParam("exchange", EXCHANGE)
                .formParam("name", QUEUE)
                .post("/exchange/{exchange}/queue")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, CoreMatchers.endsWith("/exchange/" + EXCHANGE + "/queue/" + QUEUE));
    }

    @Test
    void sendMessageGivesAccepted() {
        given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("name", EXCHANGE)
                .post("/exchange")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, CoreMatchers.endsWith("/exchange/" + EXCHANGE));

        given()
                .contentType("application/x-www-form-urlencoded")
                .pathParam("exchange", EXCHANGE)
                .formParam("name", QUEUE)
                .post("/exchange/{exchange}/queue")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, CoreMatchers.endsWith("/exchange/" + EXCHANGE + "/queue/" + QUEUE));

        given()
                .contentType("application/x-www-form-urlencoded")
                .pathParam("exchange", EXCHANGE)
                .formParam("message", "Hello World")
                .post("/exchange/{exchange}")
                .then()
                .statusCode(HttpStatus.SC_ACCEPTED);

        given()
                .pathParam("name", QUEUE)
                .get("/queue/{name}/messages")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_OK)
                .body("$", Matchers.hasSize(1))
                .body("$", Matchers.everyItem(Matchers.equalTo("Hello World")));
    }
}
