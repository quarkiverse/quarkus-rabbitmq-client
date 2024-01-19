package io.quarkiverse.rabbitmqclient.deployment;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

import io.quarkiverse.rabbitmqclient.NamedRabbitMQClient;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkiverse.rabbitmqclient.RabbitMQClients;
import io.quarkiverse.rabbitmqclient.runtime.RabbitMQRecorder;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.arc.deployment.ValidationPhaseBuildItem;
import io.quarkus.arc.processor.DotNames;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.deployment.metrics.MetricsCapabilityBuildItem;
import io.quarkus.runtime.metrics.MetricsFactory;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;

/**
 * RabbitMQ client processor.
 *
 * @author b.passon
 */
class QuarkusRabbitMQClientProcessor {

    private static final String FEATURE = "rabbitmq-client";
    private static final DotName NAMED_RABBITMQ_CLIENT_ANNOTATION = DotName
            .createSimple(NamedRabbitMQClient.class.getName());

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    HealthBuildItem addHealthCheck(RabbitMQClientsBuildConfig buildTimeConfig) {
        return new HealthBuildItem("io.quarkiverse.rabbitmqclient.RabbitMQReadyCheck",
                buildTimeConfig.healthEnabled());
    }

    @BuildStep
    public void registerAdditionalBeans(BuildProducer<AdditionalBeanBuildItem> additionalBeanBuildItemProducer) {
        additionalBeanBuildItemProducer.produce(AdditionalBeanBuildItem.builder().addBeanClasses(RabbitMQClients.class)
                .setUnremovable().setDefaultScope(DotNames.SINGLETON).build());
        additionalBeanBuildItemProducer
                .produce(AdditionalBeanBuildItem.builder().addBeanClass(NamedRabbitMQClient.class).build());
    }

    @BuildStep
    public void defineClients(BuildProducer<QuarkusRabbitMQClientBuildItem> clientName, RabbitMQClientsBuildConfig clients,
            BeanArchiveIndexBuildItem indexBuildItem) {

        if (clients.clients().get(RabbitMQClients.DEFAULT_CLIENT_NAME).enabled()) {
            clientName
                    .produce(new QuarkusRabbitMQClientBuildItem(RabbitMQClients.DEFAULT_CLIENT_NAME, clients.metricsEnabled()));
        }

        IndexView indexView = indexBuildItem.getIndex();
        Collection<AnnotationInstance> clientAnnotations = indexView.getAnnotations(NAMED_RABBITMQ_CLIENT_ANNOTATION);
        for (AnnotationInstance annotation : clientAnnotations) {
            RabbitMQClientBuildConfig cfg = clients.clients().get(annotation.value().asString());
            if (cfg != null && cfg.enabled()) {
                clientName.produce(new QuarkusRabbitMQClientBuildItem(annotation.value().asString(), clients.metricsEnabled()));
            } else if (cfg == null) {
                clientName.produce(new QuarkusRabbitMQClientBuildItem(annotation.value().asString(), clients.metricsEnabled()));
            }
        }
    }

    @BuildStep
    public void validateClients(ValidationPhaseBuildItem validationPhase,
            BuildProducer<ValidationPhaseBuildItem.ValidationErrorBuildItem> errors,
            List<QuarkusRabbitMQClientBuildItem> clients) {
        if (clients.stream().filter(c -> RabbitMQClients.DEFAULT_CLIENT_NAME.equalsIgnoreCase(c.getName())).count() > 1) {
            errors.produce(new ValidationPhaseBuildItem.ValidationErrorBuildItem(
                    new IllegalArgumentException("RabbitMQ client name '" + RabbitMQClients.DEFAULT_CLIENT_NAME
                            + "' is reserved for the default client.")));
        }
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void createClients(RabbitMQRecorder recorder, ShutdownContextBuildItem shutdown,
            List<QuarkusRabbitMQClientBuildItem> namedClients,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeans, Optional<MetricsCapabilityBuildItem> metricsCapability) {
        recorder.registerShutdownTask(shutdown);

        // create clients
        for (QuarkusRabbitMQClientBuildItem namedClient : namedClients) {
            addRabbitMQClient(recorder, namedClient, metricsCapability, syntheticBeans);
        }
    }

    void addRabbitMQClient(RabbitMQRecorder recorder, QuarkusRabbitMQClientBuildItem client,
            Optional<MetricsCapabilityBuildItem> metricsCapability,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {

        Supplier<RabbitMQClient> rabbitMQClientSupplier = null;
        if (client.isMetricsEnabled() && metricsCapability.isPresent()) {
            if (metricsCapability.get().metricsSupported(MetricsFactory.MICROMETER)) {
                rabbitMQClientSupplier = recorder.rabbitMQClientSupplierMicrometerMetrics(client.getName(),
                        Map.of("name", client.getName()));
            } else if (metricsCapability.get().metricsSupported(MetricsFactory.MP_METRICS)) {
                rabbitMQClientSupplier = recorder.rabbitMQClientSupplierMPMetrics(client.getName(),
                        Map.of("name", client.getName()));
            }
        }
        if (rabbitMQClientSupplier == null) {
            rabbitMQClientSupplier = recorder.rabbitMQClientSupplier(client.getName());
        }

        SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator = SyntheticBeanBuildItem
                .configure(RabbitMQClient.class)
                .scope(ApplicationScoped.class)
                .setRuntimeInit()
                .unremovable()
                .supplier(rabbitMQClientSupplier);

        if (RabbitMQClients.DEFAULT_CLIENT_NAME.equals(client.getName())) {
            configurator.addQualifier(Default.class);
        } else {
            configurator.addQualifier().annotation(DotNames.NAMED).addValue("value", client.getName()).done();
            configurator.addQualifier().annotation(NamedRabbitMQClient.class).addValue("value", client.getName()).done();
        }
        syntheticBeans.produce(configurator.done());
    }
}
