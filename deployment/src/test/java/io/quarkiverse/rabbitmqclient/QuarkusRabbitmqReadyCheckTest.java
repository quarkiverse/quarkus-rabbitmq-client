package io.quarkiverse.rabbitmqclient;

import java.util.ArrayList;
import java.util.HashMap;
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
            .setFlatClassPath(true)
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(DummyServer.class)
                    .addAsResource(QuarkusRabbitmqReadyCheckTest.class.getResource("/empty-properties.properties"),
                            "application.properties"));

    @Inject
    RabbitMQClientsConfig config;

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
    public void testHealthEndpointDefaultClientAllBrokersUp() {
        setupDummyServers(config, 2, 0);
        HealthCheckResponse resp = readyCheck.call();
        Assertions.assertEquals(HealthCheckResponse.Status.UP, resp.getStatus());
        assertNumberOfBrokersInState(resp, 2, HealthCheckResponse.Status.UP);
        assertNumberOfBrokersInState(resp, 0, HealthCheckResponse.Status.DOWN);
        assertData(resp);
    }

    @Test
    public void testHealthEndpointMultipleClientsAllBrokersUp() {
        setupDummyServers(config, 2, 0);
        setupDummyServers(config, "other", 2, 0);
        HealthCheckResponse resp = readyCheck.call();
        Assertions.assertEquals(HealthCheckResponse.Status.UP, resp.getStatus());
        assertNumberOfBrokersInState(resp, 4, HealthCheckResponse.Status.UP);
        assertNumberOfBrokersInState(resp, 0, HealthCheckResponse.Status.DOWN);
        assertData(resp);
    }

    @Test
    public void testHealthEndpointDefaultClientOneBrokerDown() {
        setupDummyServers(config, 3, 1);
        HealthCheckResponse resp = readyCheck.call();
        Assertions.assertEquals(HealthCheckResponse.Status.UP, resp.getStatus());
        assertNumberOfBrokersInState(resp, 2, HealthCheckResponse.Status.UP);
        assertNumberOfBrokersInState(resp, 1, HealthCheckResponse.Status.DOWN);
        assertData(resp);
    }

    @Test
    public void testHealthEndpointDefaultClientAllBrokersDown() {
        setupDummyServers(config, 5, 5);
        HealthCheckResponse resp = readyCheck.call();
        Assertions.assertEquals(HealthCheckResponse.Status.DOWN, resp.getStatus());
        assertNumberOfBrokersInState(resp, 0, HealthCheckResponse.Status.UP);
        assertNumberOfBrokersInState(resp, 5, HealthCheckResponse.Status.DOWN);
        assertData(resp);
    }

    @Test
    public void testHealthEndpointMultipleClientsAllBrokersDown() {
        setupDummyServers(config, 5, 5);
        setupDummyServers(config, "other", 3, 3);
        HealthCheckResponse resp = readyCheck.call();
        Assertions.assertEquals(HealthCheckResponse.Status.DOWN, resp.getStatus());
        assertNumberOfBrokersInState(resp, 0, HealthCheckResponse.Status.UP);
        assertNumberOfBrokersInState(resp, 8, HealthCheckResponse.Status.DOWN);
        assertData(resp);
    }

    @Test
    public void testHealthEndpointWithoutDefaultClientUp() {
        setupDummyServers(config, "other", 1, 0);
        HealthCheckResponse resp = readyCheck.call();
        Assertions.assertEquals(HealthCheckResponse.Status.UP, resp.getStatus());
        assertNumberOfBrokersInState(resp, 1, HealthCheckResponse.Status.UP);
    }

    @Test
    public void testHealthEndpointWithoutDefaultClientDown() {
        setupDummyServers(config, "other", 1, 1);
        HealthCheckResponse resp = readyCheck.call();
        Assertions.assertEquals(HealthCheckResponse.Status.DOWN, resp.getStatus());
        assertNumberOfBrokersInState(resp, 1, HealthCheckResponse.Status.DOWN);
    }

    @Test
    public void testHealthEndpointWithoutDefaultClientMultipleAddressGivesDown() {
        setupDummyServers(config, "other", 2, 1);
        HealthCheckResponse resp = readyCheck.call();
        Assertions.assertEquals(HealthCheckResponse.Status.UP, resp.getStatus());
        assertNumberOfBrokersInState(resp, 1, HealthCheckResponse.Status.UP);
        assertNumberOfBrokersInState(resp, 1, HealthCheckResponse.Status.DOWN);
    }

    private void setupDummyServers(RabbitMQClientsConfig config, int number, int down) {
        setupDummyServers(config, null, number, down);
    }

    private void setupDummyServers(RabbitMQClientsConfig config, String name, int number, int down) {
        RabbitMQClientConfig cfg = new RabbitMQClientConfig();
        Runnable closeCallback;
        if (name == null) {
            config.defaultClient = cfg;
            closeCallback = () -> config.defaultClient = null;
        } else {
            config.namedClients.put(name, cfg);
            closeCallback = () -> config.namedClients.remove(name);
        }
        cfg.addresses = new HashMap<>();
        String hostName = "client-" + (name == null ? "" : name + "-") + "dummy-";
        for (int i = 0; i < number; i++) {
            DummyServer ds = DummyServer.newDummyServer(name, closeCallback, i < down);
            RabbitMQClientConfig.Address address = new RabbitMQClientConfig.Address();
            address.hostname = ds.getHostname();
            address.port = ds.getPort();
            cfg.addresses.put(hostName + i, address);
            dummyServers.add(ds);
        }
    }

    private void assertNumberOfBrokersInState(HealthCheckResponse resp, int number, HealthCheckResponse.Status state) {
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
                    ds.isAvailable() ? HealthCheckResponse.Status.UP.name() : HealthCheckResponse.Status.DOWN.name());
        });
    }
}
