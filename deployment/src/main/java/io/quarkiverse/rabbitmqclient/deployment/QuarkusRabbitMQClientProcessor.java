package io.quarkiverse.rabbitmqclient.deployment;

import javax.enterprise.inject.Default;
import javax.inject.Singleton;

import io.quarkiverse.rabbitmqclient.*;
import io.quarkiverse.rabbitmqclient.RabbitMQClientsConfig;
import io.quarkiverse.rabbitmqclient.runtime.RabbitMQRecorder;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
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

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    HealthBuildItem addHealthCheck(RabbitMQClientBuildConfig buildTimeConfig) {
        return new HealthBuildItem("io.quarkiverse.rabbitmqclient.RabbitMQReadyCheck",
                buildTimeConfig.healthEnabled);
    }

    @BuildStep
    public void registerBeans(BuildProducer<AdditionalBeanBuildItem> additionalBeanBuildItemProducer) {
        additionalBeanBuildItemProducer.produce(AdditionalBeanBuildItem.builder().addBeanClasses(RabbitMQClients.class)
                .setUnremovable().setDefaultScope(DotNames.SINGLETON).build());
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void registerClients(RabbitMQRecorder recorder, ShutdownContextBuildItem shutdown,
            RabbitMQClientsConfig rabbitMQClientsConfig,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {
        recorder.registerShutdownTask(shutdown);

        // create default client
        addRabbitMQClient(recorder, null, rabbitMQClientsConfig, syntheticBeans);

        // create named clients
        for (String name : rabbitMQClientsConfig.namedClients.keySet()) {
            addRabbitMQClient(recorder, name, rabbitMQClientsConfig, syntheticBeans);
        }
    }

    void addRabbitMQClient(RabbitMQRecorder recorder, String name, RabbitMQClientsConfig rabbitMQClientsConfig,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {
        SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator = SyntheticBeanBuildItem
                .configure(RabbitMQClient.class)
                .scope(Singleton.class)
                .setRuntimeInit()
                .unremovable()
                // pass the runtime config into the recorder to ensure that the DataSource related beans
                // are created after runtime configuration has been setup
                .supplier(recorder.rabbitMQClientSupplier(name, rabbitMQClientsConfig));

        if (name == null) {
            configurator.addQualifier(Default.class);
        } else {
            configurator.addQualifier().annotation(DotNames.NAMED).addValue("value", name).done();
        }

        syntheticBeans.produce(configurator.done());
    }
}
