package io.quarkiverse.rabbitmqclient.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkiverse.rabbitmqclient.NamedRabbitMQClient;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;

@ApplicationScoped
public class OtherClientService {

    @Inject
    @NamedRabbitMQClient("other")
    RabbitMQClient otherRabbitMQClient;

}
