package io.quarkiverse.rabbitmqclient.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import io.quarkiverse.rabbitmqclient.RabbitMQClient;

/**
 * Captures the thread a consumer delivery is dispatched on.
 */
public final class ConsumerThreadProbe {

    private ConsumerThreadProbe() {
    }

    public static Thread consumeOnThread(RabbitMQClient client, String queue) throws Exception {
        try (Connection connection = client.connect("vt-test-connection")) {
            Channel channel = connection.createChannel();
            channel.queueDeclare(queue, false, false, true, null);
            CompletableFuture<Thread> consumerThread = new CompletableFuture<>();
            channel.basicConsume(queue, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                        byte[] body) throws IOException {
                    consumerThread.complete(Thread.currentThread());
                }
            });
            channel.basicPublish("", queue, null, "{'foo':'bar'}".getBytes(StandardCharsets.UTF_8));
            return consumerThread.get(5, TimeUnit.SECONDS);
        }
    }
}
