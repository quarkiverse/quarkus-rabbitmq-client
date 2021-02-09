package io.quarkiverse.rabbitmqclient.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.rabbitmq.client.*;

import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkiverse.rabbitmqclient.RabbitMQClientConfig;

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
    RabbitMQClientConfig config;

    @Inject
    TestConfig testConfig;

    @Inject
    RabbitMQClient rabbitMQClient;

    private Connection conn;

    public void connectClientServerSsl() {
        testConfig.setupClientCertSsl(config);
        conn = rabbitMQClient.connect("test-connection");
    }

    public void declareExchange(String name) throws IOException {
        conn.createChannel().exchangeDeclare(name, BuiltinExchangeType.TOPIC, true);
    }

    public void declareQueue(String queue, String exchange) throws IOException {
        Channel channel = conn.createChannel();
        channel.queueDeclare(queue, true, false, false, null);
        channel.queueBind(queue, exchange, "#");
    }

    public void basicConsume(String queue, boolean autoAck, TestConsumer consumer) throws IOException {
        Channel channel = conn.createChannel();
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

    public void deleteQueue(String name) throws IOException {
        conn.createChannel().queueDelete(name);
    }

    public void deleteExchange(String name) throws IOException {
        conn.createChannel().exchangeDelete(name);
    }

    public void send(String exchange, String value) throws IOException {
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .contentType("application/json")
                .contentEncoding("UTF-8")
                .headers(new HashMap<>())
                .build();
        conn.createChannel().basicPublish(exchange, "#", properties, value.getBytes(StandardCharsets.UTF_8));
    }

}
