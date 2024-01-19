package io.quarkiverse.rabbitmqclient.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.rabbitmq.client.*;

import io.quarkiverse.rabbitmqclient.NamedRabbitMQClient;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;

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
    RabbitMQClient rabbitMQClient;

    @Inject
    @NamedRabbitMQClient("ssl")
    RabbitMQClient sslRabbitMQClient;

    @Inject
    @NamedRabbitMQClient("mtls")
    RabbitMQClient mtlsRabbitMQClient;

    private TestConnection conn;
    private TestConnection sslConn;
    private TestConnection mtlsConn;

    public TestConnection def() {
        if (conn == null) {
            conn = new TestConnection(rabbitMQClient.connect("test-connection"));
        }
        return conn;
    }

    public TestConnection ssl() {
        if (sslConn == null) {
            sslConn = new TestConnection(sslRabbitMQClient.connect("test-connection"));
        }
        return sslConn;
    }

    public TestConnection mtls() {
        if (mtlsConn == null) {
            mtlsConn = new TestConnection(mtlsRabbitMQClient.connect("test-connection"));
        }
        return mtlsConn;
    }

    public class TestConnection {

        private Connection conn;

        private TestConnection(Connection conn) {
            this.conn = conn;
        }

        public void declareExchange(String name) throws IOException {
            declareExchangeInternal(name, conn.createChannel());
        }

        public void declareQueue(String queue, String exchange) throws IOException {
            declareQueueInternal(queue, exchange, conn.createChannel());
        }

        public void basicConsume(String queue, boolean autoAck, TestConsumer consumer) throws IOException {
            consumeInternal(queue, autoAck, consumer, conn.createChannel());
        }

        public void deleteQueue(String name) throws IOException {
            deleteQueueInternal(name, conn.createChannel());
        }

        public void deleteExchange(String name) throws IOException {
            deleteExchangeInternal(name, conn.createChannel());

        }

        public void send(String exchange, String value) throws IOException {
            sendInternal(conn, exchange, value);
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
}
