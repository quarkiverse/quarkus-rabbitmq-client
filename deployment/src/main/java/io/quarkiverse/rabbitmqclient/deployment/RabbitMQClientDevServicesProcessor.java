package io.quarkiverse.rabbitmqclient.deployment;

import io.quarkus.deployment.IsDockerWorking;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CuratedApplicationShutdownBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem.RunningDevService;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.runtime.configuration.ConfigUtils;
import org.jboss.logging.Logger;
import org.testcontainers.containers.RabbitMQContainer;

import java.time.Duration;
import java.util.*;

class RabbitMQClientDevServicesProcessor {

    private static final Logger log = Logger.getLogger("io.quarkiverse.rabbitmqclient.deployment");

    private static final String RABBITMQ_URI = "quarkus.rabbitmqclient.hostname";
    private static final String RABBITMQ_USER_PROP = "quarkus.rabbitmqclient.username";
    private static final String RABBITMQ_PASSWORD_PROP = "quarkus.rabbitmqclient.password";
    private static final String FEATURE_RABBITMQ = "RABBITMQ";

    static volatile RunningDevService devService;
    static volatile RabbitMQClientDevServiceConfig runningConfiguration;
    static volatile boolean first = true;

    private final IsDockerWorking isDockerWorking = new IsDockerWorking(true);

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = GlobalDevServicesConfig.Enabled.class)
    public DevServicesResultBuildItem startRabbitMQDevService(
            LaunchModeBuildItem launchMode,
            RabbitMQClientsBuildConfig rabbitMQBuildTimeConfig,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            CuratedApplicationShutdownBuildItem closeBuildItem,
            LoggingSetupBuildItem loggingSetupBuildItem,
            GlobalDevServicesConfig globalDevServicesConfig) {

        var configuration = new RabbitMQClientDevServiceConfig(rabbitMQBuildTimeConfig.devservices);

        if (devService != null) {
            if (configuration.equals(runningConfiguration)) {
                return devService.toBuildItem();
            }
            shutdownRabbitMQ();
            runningConfiguration = null;
        }

        var compressor = new StartupLogCompressor(
                (launchMode.isTest() ? "(test) " : "") + "RabbitMQ Dev Services Starting:", consoleInstalledBuildItem,
                loggingSetupBuildItem);
        try {
            RabbitMQContainer rabbitMQContainer = startRabbitMQ(configuration, globalDevServicesConfig.timeout);

            if (rabbitMQContainer != null) {
                var config = Map.of(
                        RABBITMQ_URI, rabbitMQContainer.getHttpUrl(),
                        RABBITMQ_PASSWORD_PROP, rabbitMQContainer.getAdminPassword());
                log.infof("Dev Services started a RabbitMQ container reachable at %s", rabbitMQContainer.getHttpUrl());
                log.infof("The username for both endpoints is `%s`, authenticated by `%s`", "admin",
                        rabbitMQContainer.getAdminPassword());
                log.infof("Connect via amqp using `%s`, authenticated by `%s`, on url `%s`", "admin",
                        rabbitMQContainer.getAdminPassword(), rabbitMQContainer.getAmqpUrl()
                );
                devService = new RunningDevService(FEATURE_RABBITMQ, rabbitMQContainer.getContainerId(),
                        rabbitMQContainer::close, config);
            }
        } catch (Throwable t) {
            compressor.closeAndDumpCaptured();
            throw new RuntimeException(t);
        }

        // Configure the watch dog
        if (first) {
            first = false;
            Runnable closeTask = () -> {
                if (devService != null) {
                    shutdownRabbitMQ();
                    log.info("Dev Services for RabbitMQ shut down.");
                }
                first = true;
                runningConfiguration = null;
            };
            closeBuildItem.addCloseTask(closeTask, true);
        }
        runningConfiguration = configuration;

        return devService == null ? null : devService.toBuildItem();
    }

    private RabbitMQContainer startRabbitMQ(RabbitMQClientDevServiceConfig configuration, Optional<Duration> timeout) {

        if (!isDockerWorking.getAsBoolean()) {
            log.debug("Not starting Dev Services for RabbitMQ, as Docker is not working.");
            return null;
        }

        if (!configuration.devServicesEnabled) {
            log.debug("Not starting Dev Services for RabbitMQ, as it has been disabled in the config.");
            return null;
        }

        // Check if RabbitMQ URL or password is set to explicitly
        if (ConfigUtils.isPropertyPresent(RABBITMQ_URI) || ConfigUtils.isPropertyPresent(RABBITMQ_USER_PROP)
                || ConfigUtils.isPropertyPresent(RABBITMQ_PASSWORD_PROP)) {
            log.debug("Not starting Dev Services for RabbitMQ, as there is explicit configuration present.");
            return null;
        }

        RabbitMQContainer container = new RabbitMQContainer(configuration.imageName);
        configuration.additionalEnv.forEach(container::addEnv);
        timeout.ifPresent(container::withStartupTimeout);
        container.start();
        return container;
    }

    private void shutdownRabbitMQ() {
        if (devService != null) {
            try {
                devService.close();
            } catch (Throwable e) {
                log.error("Failed to stop RabbitMQ container", e);
            } finally {
                devService = null;
            }
        }
    }

    private static final class RabbitMQClientDevServiceConfig {
        final boolean devServicesEnabled;
        final String imageName;
        final Map<String, String> additionalEnv;
        final OptionalInt fixedManagementPort;
        final OptionalInt fixedHttpPort;

        RabbitMQClientDevServiceConfig(DevServicesBuildTimeConfig devServicesConfig) {
            this.devServicesEnabled = devServicesConfig.enabled.orElse(true);
            this.imageName = devServicesConfig.imageName;
            this.additionalEnv = new HashMap<>(devServicesConfig.additionalEnv);
            this.fixedManagementPort = devServicesConfig.managementPort;
            this.fixedHttpPort = devServicesConfig.httpPort;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RabbitMQClientDevServiceConfig that = (RabbitMQClientDevServiceConfig) o;
            return devServicesEnabled == that.devServicesEnabled && imageName.equals(that.imageName)
                    && additionalEnv.equals(
                            that.additionalEnv)
                    && fixedManagementPort.equals(that.fixedManagementPort) && fixedHttpPort.equals(
                            that.fixedHttpPort);
        }

        @Override
        public int hashCode() {
            return Objects.hash(devServicesEnabled, imageName, additionalEnv, fixedManagementPort, fixedHttpPort);
        }
    }
}
