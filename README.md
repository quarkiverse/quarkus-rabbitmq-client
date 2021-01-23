# Quarkiverse RabbitMQ Client
This is a Quarkus extension for the [RabbitMQ](https://www.rabbitmq.com/) [Java Client](https://rabbitmq.com/api-guide.html).

Main use of this extension is to be able to inject the client without worrying about the
complexity related to handling dependencies in case of **native builds**.

## Coordinates

```xml
<dependency>
    <groupId>io.quarkiverse.rabbitmqclient</groupId>
    <artifactId>quarkus-rabbitmq-client</artifactId>
    <version>LATEST</version>
</dependency>
```