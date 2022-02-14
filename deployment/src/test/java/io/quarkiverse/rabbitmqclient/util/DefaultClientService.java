package io.quarkiverse.rabbitmqclient.util;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkiverse.rabbitmqclient.RabbitMQClient;

@ApplicationScoped
public class DefaultClientService {

    @Inject
    RabbitMQClient rabbitMQClient;

}
