package io.quarkiverse.rabbitmqclient;

import java.util.concurrent.ExecutorService;

import io.quarkus.runtime.LaunchMode;

/**
 * RabbitMQ client parameters for creation of {@link RabbitMQClient}
 *
 * @author b.passon
 */
class RabbitMQClientParams {

    private String name;
    private RabbitMQClientConfig config;
    private ExecutorService executorService;
    private LaunchMode launchMode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RabbitMQClientConfig getConfig() {
        return config;
    }

    public void setConfig(RabbitMQClientConfig config) {
        this.config = config;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public LaunchMode getLaunchMode() {
        return launchMode;
    }

    public void setLaunchMode(LaunchMode launchMode) {
        this.launchMode = launchMode;
    }
}
