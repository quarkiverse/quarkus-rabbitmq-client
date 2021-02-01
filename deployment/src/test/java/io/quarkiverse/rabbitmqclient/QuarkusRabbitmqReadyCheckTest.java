package io.quarkiverse.rabbitmqclient;

import java.util.ArrayList;
import java.util.List;

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

import io.quarkiverse.rabbitmqclient.util.DummyServer;
import io.quarkiverse.rabbitmqclient.util.RabbitMQTestContainer;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;

@QuarkusTestResource(RabbitMQTestContainer.class)
public class QuarkusRabbitmqReadyCheckTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest() // Start unit test with your extension loaded
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(DummyServer.class)
                    .addAsResource(QuarkusRabbitmqReadyCheckTest.class.getResource("/empty-properties.properties"),
                            "application.properties"));

    @Inject
    RabbitMQClientConfig config;

    @Readiness
    @Inject
    RabbitMQReadyCheck readyCheck;

    private List<DummyServer> dummyServers;

    @BeforeEach
    public void setup() {
        dummyServers = new ArrayList<>();
    }

    @AfterEach
    public void cleanup() {
        dummyServers.forEach(DummyServer::close);
    }

    @Test
    public void testHealthEndpointAllBrokersUp() {
        setupDummyServers(config, 2, 0);
        HealthCheckResponse resp = readyCheck.call();
        Assertions.assertEquals(HealthCheckResponse.State.UP, resp.getState());
        assertNumberOfBrokersInState(resp, 2, HealthCheckResponse.State.UP);
        assertData(resp);
    }

    @Test
    public void testHealthEndpointOneBrokerDown() {
        setupDummyServers(config, 3, 1);
        HealthCheckResponse resp = readyCheck.call();
        Assertions.assertEquals(HealthCheckResponse.State.DOWN, resp.getState());
        assertNumberOfBrokersInState(resp, 2, HealthCheckResponse.State.UP);
        assertNumberOfBrokersInState(resp, 1, HealthCheckResponse.State.DOWN);
        assertData(resp);
    }

    @Test
    public void testHealthEndpointAllBrokersDown() {
        setupDummyServers(config, 5, 5);
        HealthCheckResponse resp = readyCheck.call();
        Assertions.assertEquals(HealthCheckResponse.State.DOWN, resp.getState());
        assertNumberOfBrokersInState(resp, 0, HealthCheckResponse.State.UP);
        assertNumberOfBrokersInState(resp, 5, HealthCheckResponse.State.DOWN);
        assertData(resp);
    }

    private void setupDummyServers(RabbitMQClientConfig config, int number, int down) {
        config.addresses.clear();
        for (int i = 0; i < number; i++) {
            DummyServer ds = DummyServer.newDummyServer();
            RabbitMQClientConfig.Address address = new RabbitMQClientConfig.Address();
            address.hostname = ds.getHostname();
            address.port = ds.getPort();
            config.addresses.put("dummy-" + i, address);
            dummyServers.add(ds);
            if (i < down) {
                ds.close();
            }
        }
    }

    private void assertNumberOfBrokersInState(HealthCheckResponse resp, int number, HealthCheckResponse.State state) {
        Assertions.assertTrue(resp.getData().isPresent());
        Assertions.assertEquals(number, resp.getData().get().values().stream().filter(s -> s.equals(state.name())).count());
    }

    private void assertData(HealthCheckResponse resp) {
        Assertions.assertTrue(resp.getData().isPresent());
        Assertions.assertEquals(dummyServers.size(), resp.getData().get().size());
        dummyServers.forEach(ds -> {
            Object obj = resp.getData().get().get(ds.toString());
            Assertions.assertNotNull(obj);
            Assertions.assertEquals(obj.toString(),
                    ds.isAvailable() ? HealthCheckResponse.State.UP.name() : HealthCheckResponse.State.DOWN.name());
        });
    }
}
