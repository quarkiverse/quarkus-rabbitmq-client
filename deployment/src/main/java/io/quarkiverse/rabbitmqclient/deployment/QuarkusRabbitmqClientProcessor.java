package io.quarkiverse.rabbitmqclient.deployment;

import io.quarkiverse.rabbitmqclient.RabbitMQClientBuildConfig;
import io.quarkiverse.rabbitmqclient.RabbitMQClientProducer;
import io.quarkiverse.rabbitmqclient.runtime.RabbitMQRecorder;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
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
class QuarkusRabbitmqClientProcessor {

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
        additionalBeanBuildItemProducer.produce(AdditionalBeanBuildItem.unremovableOf(RabbitMQClientProducer.class));
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void registerBaseAndVendorMetrics(RabbitMQRecorder recorder, ShutdownContextBuildItem shutdown,
            BeanContainerBuildItem beanContainer) {
        recorder.handleShutdown(shutdown, beanContainer.getValue());
    }
}
