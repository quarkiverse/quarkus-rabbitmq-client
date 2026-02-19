package io.quarkiverse.rabbitmqclient.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;

@ApplicationScoped
public class OpenTelemetryInMemoryProducer {

    @Produces
    @Singleton
    InMemorySpanExporter inMemorySpanExporter() {
        return InMemorySpanExporter.create();
    }

    @Produces
    @Singleton
    InMemoryMetricExporter inMemoryMetricExporter() {
        return InMemoryMetricExporter.create();
    }
}
