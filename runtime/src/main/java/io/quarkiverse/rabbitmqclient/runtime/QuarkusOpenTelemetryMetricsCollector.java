package io.quarkiverse.rabbitmqclient.runtime;

import java.util.Map;

import com.rabbitmq.client.impl.OpenTelemetryMetricsCollector;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;

public class QuarkusOpenTelemetryMetricsCollector extends OpenTelemetryMetricsCollector {

    private static final String PREFIX = "rabbitmq";

    public QuarkusOpenTelemetryMetricsCollector(OpenTelemetry openTelemetry, Map<String, String> tags) {
        super(openTelemetry, PREFIX, toAttributes(tags));
    }

    private static Attributes toAttributes(Map<String, String> tags) {
        AttributesBuilder b = Attributes.builder();
        for (Map.Entry<String, String> tag : tags.entrySet()) {
            b.put(tag.getKey(), tag.getValue());
        }
        return b.build();
    }
}
