package io.quarkiverse.rabbitmqclient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DefaultAndOtherClientService {

    @Inject
    RabbitMQClient rabbitMQClient;

    @Inject
    @NamedRabbitMQClient("other")
    RabbitMQClient otherRabbitMQClient;

}
