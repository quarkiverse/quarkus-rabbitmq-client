package io.quarkiverse.rabbitmqclient.deployment;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Singleton;

import org.jboss.jandex.*;
import org.jboss.logging.Logger;

import io.opentelemetry.api.OpenTelemetry;
import io.quarkiverse.rabbitmqclient.*;
import io.quarkiverse.rabbitmqclient.runtime.MetricsType;
import io.quarkiverse.rabbitmqclient.runtime.RabbitMQHealthCheck;
import io.quarkiverse.rabbitmqclient.runtime.RabbitMQRecorder;
import io.quarkus.arc.BeanDestroyer;
import io.quarkus.arc.deployment.*;
import io.quarkus.arc.processor.DotNames;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ExecutorBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.metrics.MetricsCapabilityBuildItem;
import io.quarkus.runtime.metrics.MetricsFactory;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;

/**
 * RabbitMQ client processor.
 *
 * @author b.passon
 */
class QuarkusRabbitMQClientProcessor {

    private static final Logger LOG = Logger.getLogger(QuarkusRabbitMQClientProcessor.class);

    private static final String FEATURE = "rabbitmq-client";
    private static final DotName NAMED_RABBITMQ_CLIENT_ANNOTATION = DotName
            .createSimple(NamedRabbitMQClient.class.getName());

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void registerHealthCheck(RabbitMQClientsBuildConfig config, BuildProducer<HealthBuildItem> healthBuildItems,
            Capabilities capabilities) {
        if (config.healthEnabled() && capabilities.isPresent(Capability.SMALLRYE_HEALTH)) {
            healthBuildItems.produce(new HealthBuildItem(RabbitMQHealthCheck.class.getName(), true));
        }
    }

    @BuildStep
    public void registerAdditionalBeans(BuildProducer<AdditionalBeanBuildItem> additionalBeanBuildItemProducer) {
        additionalBeanBuildItemProducer
                .produce(AdditionalBeanBuildItem.builder().addBeanClass(NamedRabbitMQClient.class).build());
    }

    @Record(ExecutionTime.RUNTIME_INIT)
    @BuildStep
    void produceOidcClientBeans(RabbitMQRecorder recorder, List<QuarkusRabbitMQClientBuildItem> resolvedClients,
            BuildProducer<SyntheticBeanBuildItem> syntheticBean,
            LaunchModeBuildItem launchModeBuildItem,
            ExecutorBuildItem executorBuildItem) {

        String defaultClientId = null;
        for (QuarkusRabbitMQClientBuildItem clientBuildItem : resolvedClients) {
            SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator = SyntheticBeanBuildItem
                    .configure(RabbitMQClient.class)
                    .types(RabbitMQClient.class)
                    .scope(ApplicationScoped.class)
                    .setRuntimeInit()
                    .destroyer(BeanDestroyer.CloseableDestroyer.class)
                    .unremovable()
                    .createWith(recorder.createClient(clientBuildItem.getConfigId(), clientBuildItem.getId(),
                            launchModeBuildItem.getLaunchMode(), executorBuildItem.getExecutorProxy(),
                            clientBuildItem.getMetricsType(),
                            clientBuildItem.isDefaultClient()));
            if (clientBuildItem.getMetricsType() == MetricsType.OPEN_TELEMETRY) {
                configurator.addInjectionPoint(ClassType.builder(OpenTelemetry.class).build());
            }

            if (clientBuildItem.isDefaultClient()) {
                defaultClientId = clientBuildItem.getId();
                configurator.addQualifier(Default.class);
            } else {
                configurator.addQualifier().annotation(DotNames.NAMED).addValue("value", clientBuildItem.getId()).done();
                configurator.addQualifier().annotation(NamedRabbitMQClient.class).addValue("value", clientBuildItem.getId())
                        .done();
            }
            syntheticBean.produce(configurator.done());
        }

        syntheticBean.produce(SyntheticBeanBuildItem.configure(RabbitMQClients.class).unremovable()
                .types(RabbitMQClients.class)
                .scope(Singleton.class)
                .addQualifier(Default.class)
                .addInjectionPoint(ParameterizedType.builder(Instance.class)
                        .addArgument(RabbitMQClient.class)
                        .build())
                .setRuntimeInit()
                .createWith(recorder.initClients(defaultClientId))
                .done());

    }

    private MetricsType getMetricsType(RabbitMQClientsBuildConfig clients,
            Optional<MetricsCapabilityBuildItem> metricsCapability,
            Optional<OpenTelemetrySdkBuildItem> openTelemetrySdkBuildItem) {
        if (clients.metricsEnabled()) {
            if (metricsCapability.isPresent() && metricsCapability.get().metricsSupported(MetricsFactory.MICROMETER)) {
                return MetricsType.MICROMETER;
            } else if (openTelemetrySdkBuildItem.isPresent() && openTelemetrySdkBuildItem.get().isMetricsBuildTimeEnabled()) {
                return MetricsType.OPEN_TELEMETRY;
            }
        }
        return MetricsType.NOOP;
    }

    @BuildStep
    public void defineClients(BuildProducer<QuarkusRabbitMQClientBuildItem> clientName,
            Optional<MetricsCapabilityBuildItem> metricsCapability,
            Optional<OpenTelemetrySdkBuildItem> openTelemetrySdkBuildItem,
            RabbitMQClientsBuildConfig clients,
            BeanArchiveIndexBuildItem indexBuildItem) {

        MetricsType metricsType = getMetricsType(clients, metricsCapability, openTelemetrySdkBuildItem);

        boolean clientEnabled = QuarkusRabbitMQClientDeprecatedProperties.clientEnabled(null);
        if (clientEnabled) {
            String id = Optional.ofNullable(RabbitMQClientsBuildConfig.getDefaultClient(clients))
                    .flatMap(RabbitMQClientBuildConfig::id)
                    .orElse(RabbitMQClientsConfig.DEFAULT_CLIENT_NAME);
            clientName
                    .produce(new QuarkusRabbitMQClientBuildItem(RabbitMQClientsConfig.DEFAULT_CLIENT_NAME, id,
                            metricsType, true));
        }

        IndexView indexView = indexBuildItem.getIndex();
        Collection<AnnotationInstance> clientAnnotations = indexView.getAnnotations(NAMED_RABBITMQ_CLIENT_ANNOTATION);
        for (AnnotationInstance annotation : clientAnnotations) {
            String id = annotation.value().asString();
            String name = resolveConfigName(id, clients);
            clientEnabled = QuarkusRabbitMQClientDeprecatedProperties.clientEnabled(name);
            if (clientEnabled) {
                clientName.produce(new QuarkusRabbitMQClientBuildItem(name, id, metricsType, false));
            }
        }
    }

    private String resolveConfigName(String id, RabbitMQClientsBuildConfig clients) {
        return clients.clients().entrySet().stream()
                .filter(e -> e.getValue().id().isPresent() &&
                        e.getValue().id().get().equals(id))
                .findAny().map(Map.Entry::getKey).orElse(id);
    }

    @BuildStep
    public void validateClients(ValidationPhaseBuildItem validationPhase,
            BuildProducer<ValidationPhaseBuildItem.ValidationErrorBuildItem> errors,
            List<QuarkusRabbitMQClientBuildItem> clients) {
        if (clients.stream().filter(
                c -> !c.isDefaultClient() && RabbitMQClientsConfig.DEFAULT_CLIENT_NAME.equalsIgnoreCase(c.getConfigId()))
                .count() > 1) {
            errors.produce(new ValidationPhaseBuildItem.ValidationErrorBuildItem(
                    new IllegalArgumentException("RabbitMQ client name '" + RabbitMQClientsConfig.DEFAULT_CLIENT_NAME
                            + "' is reserved for the default client.")));
        }

        if (clients.stream()
                .filter(c -> !c.isDefaultClient() && RabbitMQClientsConfig.DEFAULT_CLIENT_NAME.equalsIgnoreCase(c.getId()))
                .count() > 1) {
            errors.produce(new ValidationPhaseBuildItem.ValidationErrorBuildItem(
                    new IllegalArgumentException("RabbitMQ client id '" + RabbitMQClientsConfig.DEFAULT_CLIENT_NAME
                            + "' is reserved for the default client.")));
        }
    }
}
