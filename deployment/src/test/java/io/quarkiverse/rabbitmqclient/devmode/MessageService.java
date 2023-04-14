package io.quarkiverse.rabbitmqclient.devmode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import io.quarkiverse.rabbitmqclient.util.RabbitMQTestHelper;
import io.quarkus.runtime.ShutdownEvent;

@ApplicationScoped
public class MessageService {

    @Inject
    RabbitMQTestHelper rabbitMQTestHelper;

    private List<String> messages;

    @PostConstruct
    public void setup() throws IOException {
        messages = new ArrayList<>();
        rabbitMQTestHelper.connectClientServerSsl();
        rabbitMQTestHelper.declareExchange("receive-test");
        rabbitMQTestHelper.declareQueue("receive-test-queue", "receive-test");
        rabbitMQTestHelper.basicConsume("receive-test-queue", false, (tag, envelope, properties, body) -> {
            messages.add(new String(body, StandardCharsets.UTF_8));
        });
    }

    //@PreDestroy
    public void cleanup(@Observes ShutdownEvent shutdownEvent) throws IOException {
        rabbitMQTestHelper.deleteQueue("receive-test-queue");
        rabbitMQTestHelper.deleteExchange("receive-test");
    }

    public void send(String msg) throws IOException {
        for (int i = 0; i < 10000; i++) {
            rabbitMQTestHelper.send("receive-test", "[" + i + "] " + msg);
        }
    }

    public List<String> getMessages() {
        return messages;
    }
}
