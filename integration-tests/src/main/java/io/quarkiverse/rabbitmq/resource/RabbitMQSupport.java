package io.quarkiverse.rabbitmq.resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import com.rabbitmq.client.*;

import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class RabbitMQSupport {

    @Inject
    RabbitMQClient rabbitMQClient;

    private List<String> exchanges = new ArrayList<>();
    private List<String> queues = new ArrayList<>();
    private List<RabbitMQConsumer> consumers = new ArrayList<>();

    private Connection conn;

    void init(@Observes StartupEvent evt) {
        this.conn = rabbitMQClient.connect();
    }

    public void declareExchange(String name) {
        try {
            declareExchangeInternal(name, conn.createChannel());
            exchanges.add(name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void declareQueue(String queue, String exchange) {
        try {
            consumers.add(new RabbitMQConsumer(exchange, queue));
            declareQueueInternal(queue, exchange, conn.createChannel());
            registerConsumer(queue);
            queues.add(queue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void registerConsumer(String queue) {
        consumers.stream().filter(c -> c.queue.equals(queue))
                .findAny()
                .map(consumer -> {
                    try {
                        consumeInternal(queue, true, consumer, conn.createChannel());
                        return consumer;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseThrow(() -> new RuntimeException("Failed to register consumer."));

    }

    public List<String> getConsumedMessages(String queue) {
        return consumers.stream().filter(c -> c.queue.equals(queue))
                .flatMap(c -> c.getMessages().stream())
                .toList();
    }

    public void deleteQueue(String name) {
        try {
            deleteQueueInternal(name, conn.createChannel());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteExchange(String name) {
        try {
            deleteExchangeInternal(name, conn.createChannel());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(String exchange, String value) {
        @SuppressWarnings("unchecked")
        CompletableFuture<Void>[] watchers = consumers.stream()
                .filter(c -> c.exchange.equals(exchange))
                .map(RabbitMQConsumer::watch)
                .toArray(CompletableFuture[]::new);
        try {
            sendInternal(conn, exchange, value);
            CompletableFuture.allOf(watchers).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void reset() {
        queues.forEach(this::deleteQueue);
        queues.clear();
        exchanges.forEach(this::deleteExchange);
        exchanges.clear();
        consumers.forEach(RabbitMQConsumer::reset);
        consumers.clear();
    }

    private void declareExchangeInternal(String name, Channel channel) throws IOException {
        channel.exchangeDeclare(name, BuiltinExchangeType.TOPIC, true);
    }

    private void deleteQueueInternal(String name, Channel channel) throws IOException {
        channel.queueDelete(name);
    }

    private void deleteExchangeInternal(String name, Channel channel) throws IOException {
        channel.exchangeDelete(name);
    }

    private void sendInternal(Connection conn, String exchange, String value) throws IOException {
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .contentType("application/json")
                .contentEncoding("UTF-8")
                .headers(new HashMap<>())
                .build();
        conn.createChannel().basicPublish(exchange, "#", properties, value.getBytes(StandardCharsets.UTF_8));
    }

    private void declareQueueInternal(String queue, String exchange, Channel channel) throws IOException {
        channel.queueDeclare(queue, true, false, false, null);
        channel.queueBind(queue, exchange, "#");
    }

    private void consumeInternal(String queue, boolean autoAck, RabbitMQConsumer consumer, Channel channel) throws IOException {
        channel.basicConsume(queue, autoAck, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                long deliveryTag = envelope.getDeliveryTag();
                try {
                    consumer.handleDelivery(consumerTag, envelope, properties, body);
                    if (!autoAck) {
                        channel.basicAck(deliveryTag, false);
                    }
                } catch (Throwable t) {
                    if (!autoAck) {
                        channel.basicNack(deliveryTag, false, false);
                    } else {
                        throw new IOException("Delivery failed.", t);
                    }
                }
            }
        });
    }

    private static class RabbitMQConsumer {
        private final List<String> messages = new ArrayList<>();
        private String exchange;
        private String queue;
        private CompletableFuture<Void> watcher;

        private RabbitMQConsumer(String exchange, String queue) {
            this.exchange = exchange;
            this.queue = queue;
        }

        void handleDelivery(String consumerTag,
                Envelope envelope,
                AMQP.BasicProperties properties,
                byte[] body)
                throws IOException {
            messages.add(new String(body, StandardCharsets.UTF_8));
            this.watcher.complete(null);
        }

        public void reset() {
            if (this.watcher != null && !this.watcher.isDone()) {
                this.watcher.cancel(true);
            }
        }

        public CompletableFuture<Void> watch() {
            if (this.watcher != null && !this.watcher.isDone()) {
                this.watcher.cancel(true);
            }
            this.watcher = new CompletableFuture<>();
            return this.watcher;
        }

        public List<String> getMessages() {
            return messages;
        }
    }
}
