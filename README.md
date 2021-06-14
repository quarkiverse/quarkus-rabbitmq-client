# Quarkiverse RabbitMQ Client
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-) [![version](https://img.shields.io/maven-central/v/io.quarkiverse.rabbitmqclient/quarkus-rabbitmq-client-parent)](https://repo1.maven.org/maven2/io/quarkiverse/rabbitmqclient/)

This is a Quarkus 2 extension for the [RabbitMQ](https://www.rabbitmq.com/) [Java Client](https://rabbitmq.com/api-guide.html).

RabbitMQ is a popular message broker. This Quarkus extension provides a client for RabbitMQ which is configurable using the `application.properties`.

_Note: Looking for the Quarkus 1.x extension, see the [1.x branch](https://github.com/quarkiverse/quarkus-rabbitmq-client/tree/1.x) for the details._

## Coordinates

```xml
<dependency>
    <groupId>io.quarkiverse.rabbitmqclient</groupId>
    <artifactId>quarkus-rabbitmq-client</artifactId>
    <version>0.3.0.CR3</version>
</dependency>
```
## Usage
Assuming you have RabbitMQ running on localhost:5672 you should add the following properties to your `application.properties` and fill in the values for `<username>` and `<password>`.

```properties
quarkus.rabbitmqclient.virtual-host=/
quarkus.rabbitmqclient.username=<username>
quarkus.rabbitmqclient.password=<password>
quarkus.rabbitmqclient.hostname=localhost
quarkus.rabbitmqclient.port=5672
```
Once you have configured the properties, you can start using the RabbitMQ client.

```java
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
            channel.exchangeDeclare("sample", BuiltinExchangeType.TOPIC, true);
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
```

You do not need to worry about closing connections as the `RabbitMQClient` will close them for you on application shutdown.

### Multiple RabbitMQ Clients
The extension supports having multiple RabbitMQ clients. You can add named RabbitMQ clients as follows.

```properties
quarkus.rabbitmqclient.<name>.virtual-host=/
quarkus.rabbitmqclient.<name>.username=<username>
quarkus.rabbitmqclient.<name>.password=<password>
quarkus.rabbitmqclient.<name>.hostname=localhost
quarkus.rabbitmqclient.<name>.port=5672
```
All configuration options that are available on the default non named RabbitMQ client are available. Injecting a named RabbitMQ client, e.g. foo, can be achieved as follows.

```java
@ApplicationScoped
public class MessageService {
    
    @Inject
    @NamedRabbitMQClient("foo")
    RabbitMQClient fooClient;
}
```

It is possible to use multiple RabbitMQ clients in the same class as long as they are all named, or in combination with the unnamed default client. 

## License
This extension is licensed under the Apache License 2.0.

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="https://github.com/bpasson"><img src="https://avatars.githubusercontent.com/u/6814512?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Bas Passon</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-rabbitmq-client/commits?author=bpasson" title="Code">💻</a> <a href="#maintenance-bpasson" title="Maintenance">🚧</a></td>
  </tr>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!
