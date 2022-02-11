package io.quarkiverse.rabbitmqclient.runtime;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.microprofile.metrics.*;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.impl.AbstractMetricsCollector;

import io.smallrye.metrics.MetricRegistries;

public class QuarkusMPMetricsCollector extends AbstractMetricsCollector {

    private static final String PREFIX = "rabbitmq";

    private final RabbitMQGauge connections;
    private final RabbitMQGauge channels;
    private final RabbitMQCounter publishedMessages;
    private final RabbitMQCounter failedToPublishMessages;
    private final RabbitMQCounter ackedPublishedMessages;
    private final RabbitMQCounter nackedPublishedMessages;
    private final RabbitMQCounter unroutedPublishedMessages;
    private final RabbitMQCounter consumedMessages;
    private final RabbitMQCounter acknowledgedMessages;
    private final RabbitMQCounter rejectedMessages;

    public QuarkusMPMetricsCollector(Map<String, String> tags) {
        MetricRegistry registry = MetricRegistries.get(MetricRegistry.Type.VENDOR);
        Tag[] tagArray = toTagArray(tags);
        connections = (RabbitMQGauge) Metrics.CONNECTIONS.create(registry, tagArray);
        channels = (RabbitMQGauge) Metrics.CHANNELS.create(registry, tagArray);
        publishedMessages = (RabbitMQCounter) Metrics.PUBLISHED_MESSAGES.create(registry, tagArray);
        failedToPublishMessages = (RabbitMQCounter) Metrics.FAILED_TO_PUBLISH_MESSAGES.create(registry, tagArray);
        ackedPublishedMessages = (RabbitMQCounter) Metrics.ACKED_PUBLISHED_MESSAGES.create(registry, tagArray);
        nackedPublishedMessages = (RabbitMQCounter) Metrics.NACKED_PUBLISHED_MESSAGES.create(registry, tagArray);
        unroutedPublishedMessages = (RabbitMQCounter) Metrics.UNROUTED_PUBLISHED_MESSAGES.create(registry, tagArray);
        consumedMessages = (RabbitMQCounter) Metrics.CONSUMED_MESSAGES.create(registry, tagArray);
        acknowledgedMessages = (RabbitMQCounter) Metrics.ACKNOWLEDGED_MESSAGES.create(registry, tagArray);
        rejectedMessages = (RabbitMQCounter) Metrics.REJECTED_MESSAGES.create(registry, tagArray);
    }

    @Override
    protected void incrementConnectionCount(Connection connection) {
        this.connections.increment();
    }

    @Override
    protected void decrementConnectionCount(Connection connection) {
        this.connections.decrement();
    }

    @Override
    protected void incrementChannelCount(Channel channel) {
        this.channels.increment();
    }

    @Override
    protected void decrementChannelCount(Channel channel) {
        this.channels.decrement();
    }

    @Override
    protected void markPublishedMessage() {
        this.publishedMessages.inc();
    }

    @Override
    protected void markMessagePublishFailed() {
        this.failedToPublishMessages.inc();
    }

    @Override
    protected void markConsumedMessage() {
        this.consumedMessages.inc();
    }

    @Override
    protected void markAcknowledgedMessage() {
        this.acknowledgedMessages.inc();
    }

    @Override
    protected void markRejectedMessage() {
        this.rejectedMessages.inc();
    }

    @Override
    protected void markMessagePublishAcknowledged() {
        this.ackedPublishedMessages.inc();
    }

    @Override
    protected void markMessagePublishNotAcknowledged() {
        this.nackedPublishedMessages.inc();
    }

    @Override
    protected void markPublishedMessageUnrouted() {
        this.unroutedPublishedMessages.inc();
    }

    private static Tag[] toTagArray(Map<String, String> tags) {
        return tags.entrySet().stream().map(e -> new Tag(e.getKey(), e.getValue())).toArray(Tag[]::new);
    }

    private static Metadata meta(String name, MetricType type) {
        return Metadata.builder()
                .withName(name)
                .withDisplayName(name)
                .withType(type)
                .withUnit("none")
                .withDescription(name)
                .build();
    }

    private static class RabbitMQCounter implements Counter {
        private final AtomicLong value = new AtomicLong(0L);

        @Override
        public void inc() {
            value.incrementAndGet();
        }

        @Override
        public void inc(long l) {
            value.addAndGet(l);
        }

        @Override
        public long getCount() {
            return value.get();
        }
    }

    private static class RabbitMQGauge implements Gauge<Long> {
        private final AtomicLong value = new AtomicLong(0L);

        public void increment() {
            value.incrementAndGet();
        }

        public void decrement() {
            value.decrementAndGet();
        }

        @Override
        public Long getValue() {
            return value.get();
        }
    }

    public enum Metrics {
        CONNECTIONS {
            Object create(MetricRegistry registry, Tag[] tags) {
                return registry.register(meta(QuarkusMPMetricsCollector.PREFIX + ".connections", MetricType.GAUGE),
                        new RabbitMQGauge(), tags);
            }
        },
        CHANNELS {
            Object create(MetricRegistry registry, Tag[] tags) {
                return registry.register(meta(QuarkusMPMetricsCollector.PREFIX + ".channels", MetricType.GAUGE),
                        new RabbitMQGauge(), tags);
            }
        },
        PUBLISHED_MESSAGES {
            Object create(MetricRegistry registry, Tag[] tags) {
                return registry.register(meta(QuarkusMPMetricsCollector.PREFIX + ".published", MetricType.COUNTER),
                        new RabbitMQCounter(), tags);
            }
        },
        CONSUMED_MESSAGES {
            Object create(MetricRegistry registry, Tag[] tags) {
                return registry.register(meta(QuarkusMPMetricsCollector.PREFIX + ".consumed", MetricType.COUNTER),
                        new RabbitMQCounter(), tags);
            }
        },
        ACKNOWLEDGED_MESSAGES {
            Object create(MetricRegistry registry, Tag[] tags) {
                return registry.register(meta(QuarkusMPMetricsCollector.PREFIX + ".acknowledged", MetricType.COUNTER),
                        new RabbitMQCounter(), tags);
            }
        },
        REJECTED_MESSAGES {
            Object create(MetricRegistry registry, Tag[] tags) {
                return registry.register(meta(QuarkusMPMetricsCollector.PREFIX + ".rejected", MetricType.COUNTER),
                        new RabbitMQCounter(), tags);
            }
        },
        FAILED_TO_PUBLISH_MESSAGES {
            Object create(MetricRegistry registry, Tag[] tags) {
                return registry.register(meta(QuarkusMPMetricsCollector.PREFIX + ".failed_to_publish", MetricType.COUNTER),
                        new RabbitMQCounter(), tags);
            }
        },
        ACKED_PUBLISHED_MESSAGES {
            Object create(MetricRegistry registry, Tag[] tags) {
                return registry.register(meta(QuarkusMPMetricsCollector.PREFIX + ".acknowledged_published", MetricType.COUNTER),
                        new RabbitMQCounter(), tags);
            }
        },
        NACKED_PUBLISHED_MESSAGES {
            Object create(MetricRegistry registry, Tag[] tags) {
                return registry.register(
                        meta(QuarkusMPMetricsCollector.PREFIX + ".not_acknowledged_published", MetricType.COUNTER),
                        new RabbitMQCounter(), tags);
            }
        },
        UNROUTED_PUBLISHED_MESSAGES {
            Object create(MetricRegistry registry, Tag[] tags) {
                return registry.register(meta(QuarkusMPMetricsCollector.PREFIX + ".unrouted_published", MetricType.COUNTER),
                        new RabbitMQCounter(), tags);
            }
        };

        abstract Object create(MetricRegistry registry, Tag[] tags);
    }
}
