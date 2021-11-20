package io.quarkiverse.rabbitmqclient.deployment;

import java.util.Collection;
import java.util.List;

import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

import io.quarkiverse.rabbitmqclient.NamedRabbitMQClient;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkiverse.rabbitmqclient.RabbitMQClients;
import io.quarkiverse.rabbitmqclient.RabbitMQClientsBuildConfig;
import io.quarkiverse.rabbitmqclient.runtime.RabbitMQRecorder;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.arc.processor.DotNames;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
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
    private static final DotName INJECT_ANNOTATION = DotName
            .createSimple(Inject.class.getName());

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    HealthBuildItem addHealthCheck(RabbitMQClientsBuildConfig buildTimeConfig) {
        return new HealthBuildItem("io.quarkiverse.rabbitmqclient.RabbitMQReadyCheck",
                buildTimeConfig.healthEnabled);
    }

    @BuildStep
    public void registerBeans(BuildProducer<AdditionalBeanBuildItem> additionalBeanBuildItemProducer) {
        additionalBeanBuildItemProducer.produce(AdditionalBeanBuildItem.builder().addBeanClasses(RabbitMQClients.class)
                .setUnremovable().setDefaultScope(DotNames.SINGLETON).build());
        additionalBeanBuildItemProducer
                .produce(AdditionalBeanBuildItem.builder().addBeanClass(NamedRabbitMQClient.class).build());
    }

    @BuildStep
    public void namedClients(BeanArchiveIndexBuildItem indexBuildItem,
            BuildProducer<QuarkusRabbitMQClientBuildItem> clientName) {
        IndexView indexView = indexBuildItem.getIndex();

        Collection<AnnotationInstance> clientAnnotations = indexView.getAnnotations(NAMED_RABBITMQ_CLIENT_ANNOTATION);
        for (AnnotationInstance annotation : clientAnnotations) {
            clientName.produce(new QuarkusRabbitMQClientBuildItem(annotation.value().asString()));
        }
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void registerClients(RabbitMQRecorder recorder, ShutdownContextBuildItem shutdown,
            List<QuarkusRabbitMQClientBuildItem> namedClients,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {
        recorder.registerShutdownTask(shutdown);

        // create default client
        addRabbitMQClient(recorder, null, syntheticBeans);

        // create named clients
        for (QuarkusRabbitMQClientBuildItem namedClient : namedClients) {
            addRabbitMQClient(recorder, namedClient.getName(), syntheticBeans);
        }
    }

    void addRabbitMQClient(RabbitMQRecorder recorder, String name,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {
        SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator = SyntheticBeanBuildItem
                .configure(RabbitMQClient.class)
                .scope(Singleton.class)
                .setRuntimeInit()
                .unremovable()
                .supplier(recorder.rabbitMQClientSupplier(name));

        if (name == null) {
            configurator.addQualifier(Default.class);
        } else {
            configurator.addQualifier().annotation(DotNames.NAMED).addValue("value", name).done();
            configurator.addQualifier().annotation(NamedRabbitMQClient.class).addValue("value", name).done();
        }
        syntheticBeans.produce(configurator.done());
    }
}
