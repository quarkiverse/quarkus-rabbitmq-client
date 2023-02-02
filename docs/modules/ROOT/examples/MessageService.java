// tag::skeleton[]
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import com.rabbitmq.client.*;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkus.runtime.StartupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MessageService {
    private static final Logger log = LoggerFactory.getLogger(MessageService.class);
    @Inject
    RabbitMQClient rabbitMQClient;
    private Channel channel;
    public void onApplicationStart(@Observes StartupEvent event) {
        // on application start prepare the queus and message listener
        setupQueues();
        setupReceiving();
    }

    private void setupQueues() {
        try {
            // create a connection
            Connection connection = rabbitMQClient.connect();
            // create a channel
            channel = connection.createChannel();
            // declare exchanges and queues
            channel.exchangeDeclare("test", BuiltinExchangeType.TOPIC, true);
            channel.queueDeclare("sample.queue", true, false, false, null);
            channel.queueBind("sample.queue", "test", "#");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void setupReceiving() {
        try {
            // register a consumer for messages
            channel.basicConsume("sample.queue", true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    // just print the received message.
                    log.info("Received: " + new String(body, StandardCharsets.UTF_8));
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void send(String message) {
        try {
            // send a message to the exchange
            channel.basicPublish("test", "#", null, message.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
// end::skeleton[]