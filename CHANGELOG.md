# Changelog

## 3.3.0

### Breaking Changes
* `RabbitMQClients.getRabbitMQClient(String name, MetricsCollector mc)` has been removed due to stricter client creation. It is no longer possible to add a custom metrics collector. For metrics you should use `quarkus-micrometer`.
* `quarkus-smallrye-metrics` is no longer supported, please migrate to `quarkus-micrometer`.
* When using a custom `quarkus.rabbitmqclient.<name>.id` you need to update any `@NamedRabbitMQClient` to match the id value.
* Metric `name` tag now uses the client id instead of the client name.

### Non-Breaking Changes

* `quarkus.rabbitmqclient.<name>.enabled` → renamed to `io.sample.<name>.client-enabled` due to Quarkus 3.31.x stricter built-time-runtime-fixed property fixation. The old property will continue to work but is marked as deprecated and will be removed in a future release.
* `quarkus.rabbitmqclient.<name>.id` was added as an optional property to uniquely identify RabbitMQ clients. It will be populated with the name if not set. The value `default` is reserved for the default client.
* `RabbitMQClients.getRabbitMQClient(String name)` has been marked deprecated and will be removed in a future release. Use `RabbitMQClients.getClient(String id)` instead.
* Added support for `quarkus-opentelemetry` metrics.