package io.quarkiverse.rabbitmqclient.runtime;

import io.quarkiverse.rabbitmqclient.RabbitMQClientProducer;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class RabbitMQRecorder {

    public void handleShutdown(ShutdownContext shutdownContext, BeanContainer beanContainer) {
        RabbitMQClientProducer producer = beanContainer.instance(RabbitMQClientProducer.class);
        shutdownContext.addShutdownTask(producer::destroy);
    }
}
