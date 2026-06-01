package io.quarkiverse.rabbitmqclient.runtime;

import java.util.concurrent.ExecutorService;

import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkiverse.rabbitmqclient.RabbitMQClientConfig;
import io.quarkus.runtime.LaunchMode;

/**
 * RabbitMQ client parameters for creation of {@link RabbitMQClient}
 *
 * @author b.passon
 */
class RabbitMQClientParams {

    private String id;
    private RabbitMQClientConfig config;
    private ExecutorService executorService;
    private LaunchMode launchMode;
    private boolean isDefault = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
