package io.quarkiverse.rabbitmqclient.core.runtime;

/**
 * Protocol-neutral host and port of a RabbitMQ broker.
 *
 * @param host the host name.
 * @param port the port, or {@code -1} if unspecified.
 */
public record BrokerAddress(String host, int port) {

    /**
     * @return {@code host:port}, or {@code host} if the port is {@code -1}.
     */
    @Override
    public String toString() {
        return port == -1 ? host : host + ":" + port;
    }
}
