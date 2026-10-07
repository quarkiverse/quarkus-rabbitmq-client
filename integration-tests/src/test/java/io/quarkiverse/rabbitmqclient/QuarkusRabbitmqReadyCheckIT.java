package io.quarkiverse.rabbitmqclient;

import static io.restassured.RestAssured.given;

import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

import io.quarkiverse.rabbitmqclient.util.RabbitMQTestContainer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@QuarkusTestResource(RabbitMQTestContainer.class)
public class QuarkusRabbitmqReadyCheckIT {

    @Test
    void readyCheckGivesUp() {

        given()
                .get("/q/health/ready")
                .then()
                .contentType(ContentType.JSON)
                .statusCode(200)
                .body("status", IsEqual.equalTo("UP"));
    }
}
