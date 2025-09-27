package io.quarkiverse.rabbitmqclient;

import java.util.*;

import jakarta.inject.Inject;

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

    @Inject
    RabbitMQClientsBuildConfig buildConfig;

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
        setupDummyServers(config, buildConfig, 2, 0);
        HealthCheckResponse resp = readyCheck.call();
        Assertions.assertEquals(HealthCheckResponse.Status.UP, resp.getStatus());
        assertNumberOfBrokersInState(resp, 2, HealthCheckResponse.Status.UP);
        assertNumberOfBrokersInState(resp, 0, HealthCheckResponse.Status.DOWN);
        assertData(resp);
    }

    @Test
    public void testHealthEndpointMultipleClientsAllBrokersUp() {
        setupDummyServers(config, buildConfig, 2, 0);
        setupDummyServers(config, buildConfig, "other", 2, 0);
        HealthCheckResponse resp = readyCheck.call();
        Assertions.assertEquals(HealthCheckResponse.Status.UP, resp.getStatus());
        assertNumberOfBrokersInState(resp, 4, HealthCheckResponse.Status.UP);
        assertNumberOfBrokersInState(resp, 0, HealthCheckResponse.Status.DOWN);
        assertData(resp);
    }

    @Test
    public void testHealthEndpointDefaultClientOneBrokerDown() {
        setupDummyServers(config, buildConfig, 3, 1);
        HealthCheckResponse resp = readyCheck.call();
        Assertions.assertEquals(HealthCheckResponse.Status.UP, resp.getStatus());
        assertNumberOfBrokersInState(resp, 2, HealthCheckResponse.Status.UP);
        assertNumberOfBrokersInState(resp, 1, HealthCheckResponse.Status.DOWN);
        assertData(resp);
    }

    @Test
    public void testHealthEndpointDefaultClientAllBrokersDown() {
        setupDummyServers(config, buildConfig, 5, 5);
        HealthCheckResponse resp = readyCheck.call();
        Assertions.assertEquals(HealthCheckResponse.Status.DOWN, resp.getStatus());
        assertNumberOfBrokersInState(resp, 0, HealthCheckResponse.Status.UP);
        assertNumberOfBrokersInState(resp, 5, HealthCheckResponse.Status.DOWN);
        assertData(resp);
    }

    @Test
    public void testHealthEndpointMultipleClientsAllBrokersDown() {
        setupDummyServers(config, buildConfig, 5, 5);
        setupDummyServers(config, buildConfig, "other", 3, 3);
        HealthCheckResponse resp = readyCheck.call();
        Assertions.assertEquals(HealthCheckResponse.Status.DOWN, resp.getStatus());
        assertNumberOfBrokersInState(resp, 0, HealthCheckResponse.Status.UP);
        assertNumberOfBrokersInState(resp, 8, HealthCheckResponse.Status.DOWN);
        assertData(resp);
    }

    @Test
    public void testHealthEndpointWithoutDefaultClientUp() {
        setupDisabledDummyServers(config, buildConfig);
        setupDummyServers(config, buildConfig, "other", 1, 0);
        HealthCheckResponse resp = readyCheck.call();
        Assertions.assertEquals(HealthCheckResponse.Status.UP, resp.getStatus());
        assertNumberOfBrokersInState(resp, 1, HealthCheckResponse.Status.UP);
    }

    @Test
    public void testHealthEndpointWithoutDefaultClientDown() {
        setupDisabledDummyServers(config, buildConfig);
        setupDummyServers(config, buildConfig, "other", 1, 1);
        HealthCheckResponse resp = readyCheck.call();
        Assertions.assertEquals(HealthCheckResponse.Status.DOWN, resp.getStatus());
        assertNumberOfBrokersInState(resp, 1, HealthCheckResponse.Status.DOWN);
    }

    @Test
    public void testHealthEndpointWithoutDefaultClientMultipleAddressGivesDown() {
        setupDisabledDummyServers(config, buildConfig);
        setupDummyServers(config, buildConfig, "other", 2, 1);
        HealthCheckResponse resp = readyCheck.call();
        Assertions.assertEquals(HealthCheckResponse.Status.UP, resp.getStatus());
        assertNumberOfBrokersInState(resp, 1, HealthCheckResponse.Status.UP);
        assertNumberOfBrokersInState(resp, 1, HealthCheckResponse.Status.DOWN);
    }

    private void setupDisabledDummyServers(RabbitMQClientsConfig config, RabbitMQClientsBuildConfig buildConfig) {
        setupDummyServers(config, buildConfig, false, null, 0, 0);
    }

    private void setupDisabledDummyServers(RabbitMQClientsConfig config, RabbitMQClientsBuildConfig buildConfig, String name) {
        setupDummyServers(config, buildConfig, false, name, 0, 0);
    }

    private void setupDummyServers(RabbitMQClientsConfig config, RabbitMQClientsBuildConfig buildConfig, int number, int down) {
        setupDummyServers(config, buildConfig, true, null, number, down);
    }

    private void setupDummyServers(RabbitMQClientsConfig config, RabbitMQClientsBuildConfig buildConfig, String name,
            int number, int down) {
        setupDummyServers(config, buildConfig, true, name, number, down);
    }

    private void setupDummyServers(RabbitMQClientsConfig config, RabbitMQClientsBuildConfig buildConfig, boolean enabled,
            String name, int number, int down) {
        RabbitMQClientConfig cfg;
        RabbitMQClientBuildConfig buildCfg;
        Runnable closeCallback;
        if (name == null) {
            cfg = newClientConfig();
            buildCfg = newClientBuildConfig(enabled);
            config.clients().put(RabbitMQClients.DEFAULT_CLIENT_NAME, cfg);
            buildConfig.clients().put(RabbitMQClients.DEFAULT_CLIENT_NAME, buildCfg);
            closeCallback = () -> {
                config.clients().remove(RabbitMQClients.DEFAULT_CLIENT_NAME);
                buildConfig.clients().remove(RabbitMQClients.DEFAULT_CLIENT_NAME);
            };
        } else {
            cfg = newClientConfig();
            buildCfg = newClientBuildConfig(enabled);
            config.clients().put(name, cfg);
            buildConfig.clients().put(name, buildCfg);
            closeCallback = () -> {
                config.clients().remove(name);
                buildConfig.clients().remove(name);
            };
        }

        String hostName = "client-" + (name == null ? "" : name + "-") + "dummy-";
        for (int i = 0; i < number; i++) {
            DummyServer ds = DummyServer.newDummyServer(name, closeCallback, i < down);
            cfg.addresses().put(hostName + i, addressFor(ds));
            dummyServers.add(ds);
        }
    }

    private RabbitMQClientConfig newClientConfig() {
        return new RabbitMQClientConfig() {
            private final Map<String, Address> addresses = new HashMap<>();

            @Override
            public Optional<String> uri() {
                return Optional.empty();
            }

            @Override
            public Map<String, Address> addresses() {
                return addresses;
            }

            @Override
            public String username() {
                return null;
            }

            @Override
            public String password() {
                return null;
            }

            @Override
            public Optional<String> credentialsProvider() {
                return Optional.empty();
            }

            @Override
            public Optional<String> credentialsProviderName() {
                return Optional.empty();
            }

            @Override
            public String hostname() {
                return null;
            }

            @Override
            public String virtualHost() {
                return null;
            }

            @Override
            public int port() {
                return 0;
            }

            @Override
            public int connectionTimeout() {
                return 0;
            }

            @Override
            public int connectionCloseTimeout() {
                return 0;
            }

            @Override
            public int requestedHeartbeat() {
                return 0;
            }

            @Override
            public int handshakeTimeout() {
                return 0;
            }

            @Override
            public int shutdownTimeout() {
                return 0;
            }

            @Override
            public int requestedChannelMax() {
                return 0;
            }

            @Override
            public int requestedFrameMax() {
                return 0;
            }

            @Override
            public int maxInboundMessageBodySize() {
                return 0;
            }

            @Override
            public int networkRecoveryInterval() {
                return 0;
            }

            @Override
            public int channelRpcTimeout() {
                return 0;
            }

            @Override
            public boolean channelRpcResponseTypeCheck() {
                return false;
            }

            @Override
            public boolean connectionRecovery() {
                return false;
            }

            @Override
            public boolean topologyRecovery() {
                return false;
            }

            @Override
            public SaslType sasl() {
                return null;
            }

            @Override
            public TlsConfig tls() {
                return null;
            }

            @Override
            public NioConfig nio() {
                return null;
            }

            @Override
            public Map<String, String> properties() {
                return null;
            }
        };
    }

    private RabbitMQClientBuildConfig newClientBuildConfig(boolean enabled) {
        return () -> enabled;
    }

    private RabbitMQClientConfig.Address addressFor(DummyServer ds) {
        return new RabbitMQClientConfig.Address() {
            @Override
            public String hostname() {
                return ds.getHostname();
            }

            @Override
            public int port() {
                return ds.getPort();
            }
        };
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
