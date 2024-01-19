package io.quarkiverse.rabbitmqclient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DefaultAndOtherClientService {

    @Inject
    RabbitMQClient rabbitMQClient;

    @Inject
    @NamedRabbitMQClient("ssl")
    RabbitMQClient sslRabbitMQClient;

    @Inject
    @NamedRabbitMQClient("mtls")
    RabbitMQClient mtlsRabbitMQClient;

}
