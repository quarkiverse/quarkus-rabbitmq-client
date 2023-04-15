package io.quarkiverse.rabbitmqclient.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.rabbitmq.client.*;

import io.quarkiverse.rabbitmqclient.NamedRabbitMQClient;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkiverse.rabbitmqclient.RabbitMQClientsConfig;

@ApplicationScoped
public class RabbitMQTestHelper {

    public interface TestConsumer {
        void handleDelivery(String consumerTag,
                Envelope envelope,
                AMQP.BasicProperties properties,
                byte[] body)
                throws IOException;
    }

    @Inject
    RabbitMQClientsConfig configs;

    @Inject
    TestConfig testConfig;

    @Inject
    RabbitMQClient rabbitMQClient;

    @Inject
    @NamedRabbitMQClient("other")
    RabbitMQClient otherRabbitMQClient;

    private Connection defaultConn;
    private Connection otherConn;

    public void connectClientServerSsl() {
        testConfig.setupClientCertSsl(configs);
        testConfig.setupClientCertSsl("other", configs);
        defaultConn = rabbitMQClient.connect("test-connection");
        otherConn = otherRabbitMQClient.connect("other-test-connection");
    }

    public void declareExchange(String name) throws IOException {
        declareExchangeInternal(name, defaultConn.createChannel());
    }

    public void declareExchangeOther(String name) throws IOException {
        declareExchangeInternal(name, otherConn.createChannel());
    }

    public void declareQueue(String queue, String exchange) throws IOException {
        declareQueueInternal(queue, exchange, defaultConn.createChannel());
    }

    public void declareQueueOther(String queue, String exchange) throws IOException {
        declareQueueInternal(queue, exchange, otherConn.createChannel());
    }

    public void basicConsume(String queue, boolean autoAck, TestConsumer consumer) throws IOException {
        consumeInternal(queue, autoAck, consumer, defaultConn.createChannel());
    }

    public void basicConsumeOther(String queue, boolean autoAck, TestConsumer consumer) throws IOException {
        consumeInternal(queue, autoAck, consumer, otherConn.createChannel());
    }

    public void deleteQueue(String name) throws IOException {
        deleteQueueInternal(name, defaultConn.createChannel());
    }

    public void deleteQueueOther(String name) throws IOException {
        deleteQueueInternal(name, otherConn.createChannel());
    }

    public void deleteExchange(String name) throws IOException {
        deleteExchangeInternal(name, defaultConn.createChannel());

    }

    public void deleteExchangeOther(String name) throws IOException {
        deleteExchangeInternal(name, otherConn.createChannel());
    }

    public void send(String exchange, String value) throws IOException {
        sendInternal(defaultConn, exchange, value);
    }

    public void sendOther(String exchange, String value) throws IOException {
        sendInternal(otherConn, exchange, value);
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

    private void consumeInternal(String queue, boolean autoAck, TestConsumer consumer, Channel channel) throws IOException {
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
}
