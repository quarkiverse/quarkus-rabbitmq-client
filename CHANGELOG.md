# Changelog

## 4.0.0

### Breaking Changes
* Requires Quarkus 4. Applications on Quarkus 3 should stay on 3.4.x (Quarkus 3.40 LTS) or 3.3.x (Quarkus 3.33 LTS), see the compatibility table in the README or documentation.
* The extension is compiled for Java 21; Java 17 is no longer supported.
* Removed APIs deprecated since 3.3.0:
  * `RabbitMQClients.getRabbitMQClient(String name)`, use `RabbitMQClients.getClient(String id)` (or `getClient()` for the default client).
  * `RabbitMQClient.disconnect()`, use `RabbitMQClient.close()`.
  * `RabbitMQClient.getName()`, use `RabbitMQClient.getId()`.
* Removed the deprecated `quarkus.rabbitmqclient.<name>.enabled` property, use `quarkus.rabbitmqclient.<name>.client-enabled`. The old property is no longer recognized, so a client disabled with it will be enabled again.

### Non-Breaking Changes
* Upgraded the RabbitMQ Java client (`com.rabbitmq:amqp-client`) to 5.37.0.
* Extension status changed from `experimental` to `stable`.

## 3.4.0

### Non-Breaking Changes
* Fixed [#364](https://github.com/quarkiverse/quarkus-rabbitmq-client/issues/364): health check failed with a `ClassCastException` because the injected client proxy was not unwrapped.
* Fixed [#358](https://github.com/quarkiverse/quarkus-rabbitmq-client/issues/358): native build failure introduced in 3.3.0. Native integration tests were added to prevent regressions.
* `classpath:` resources referenced in the client configuration (e.g. TLS key and trust stores) are now loaded through the thread context class loader, fixing resource lookups with newer Quarkus class loading.
* Built against Quarkus 3.37.4 and RabbitMQ Java client 5.34.0.

## 3.3.0

### Breaking Changes
* `RabbitMQClients.getRabbitMQClient(String name, MetricsCollector mc)` has been removed due to stricter client creation. It is no longer possible to add a custom metrics collector. For metrics you should use `quarkus-micrometer`.
* `quarkus-smallrye-metrics` is no longer supported, please migrate to `quarkus-micrometer`.
* When using a custom `quarkus.rabbitmqclient.<name>.id` you need to update any `@NamedRabbitMQClient` to match the id value.
* Metric `name` tag now uses the client id instead of the client name.

### Non-Breaking Changes

* `quarkus.rabbitmqclient.<name>.enabled` → renamed to `quarkus.rabbitmqclient.<name>.client-enabled` due to Quarkus 3.31.x stricter built-time-runtime-fixed property fixation. The old property will continue to work but is marked as deprecated and will be removed in a future release.
* `quarkus.rabbitmqclient.<name>.id` was added as an optional property to uniquely identify RabbitMQ clients. It will be populated with the name if not set. The value `default` is reserved for the default client.
* `RabbitMQClients.getRabbitMQClient(String name)` has been marked deprecated and will be removed in a future release. Use `RabbitMQClients.getClient(String id)` instead.
* Added support for `quarkus-opentelemetry` metrics.