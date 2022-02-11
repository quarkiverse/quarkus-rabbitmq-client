package io.quarkiverse.rabbitmqclient.runtime;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.rabbitmq.client.impl.MicrometerMetricsCollector;

import io.micrometer.core.instrument.Tag;

class QuarkusMicrometerMetricsCollector extends MicrometerMetricsCollector {

    private static final String PREFIX = "rabbitmq";

    public QuarkusMicrometerMetricsCollector(Map<String, String> tags) {
        super(io.micrometer.core.instrument.Metrics.globalRegistry, PREFIX, toTagList(tags));
    }

    private static List<Tag> toTagList(Map<String, String> tags) {
        return tags.entrySet().stream().map(e -> Tag.of(e.getKey(), e.getValue())).collect(Collectors.toList());
    }
}
