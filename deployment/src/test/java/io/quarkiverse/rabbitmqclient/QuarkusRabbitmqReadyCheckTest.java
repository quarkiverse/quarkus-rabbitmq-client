package io.quarkiverse.rabbitmqclient;

import javax.inject.Inject;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class QuarkusRabbitmqReadyCheckTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest() // Start unit test with your extension loaded
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(DummyServer.class)
                    .addAsResource(QuarkusRabbitmqReadyCheckTest.class.getResource("/ready-check-properties.properties"),
                            "application.properties"));

    @Inject
    RabbitMQClientConfig config;

    @Readiness
    @Inject
    RabbitMQReadyCheck readyCheck;

    private DummyServer dummyServer;

    @BeforeEach
    public void before() {
        dummyServer = DummyServer.newDummyServer();
        config.port = dummyServer.getPort();
    }

    @AfterEach
    public void after() {
        dummyServer.close();
    }

    @Test
    public void testHealthEndpointUp() {
        HealthCheckResponse resp = readyCheck.call();
        Assertions.assertEquals(HealthCheckResponse.State.UP, resp.getState());
    }

    @Test
    public void testHealthEndpointDown() {
        dummyServer.close();
        HealthCheckResponse resp = readyCheck.call();
        Assertions.assertEquals(HealthCheckResponse.State.DOWN, resp.getState());
    }
}
