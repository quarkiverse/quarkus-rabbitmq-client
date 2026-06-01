package io.quarkiverse.rabbitmqclient;

import com.rabbitmq.client.DefaultSaslConfig;
import com.rabbitmq.client.SaslConfig;

public enum SaslType {
    PLAIN(DefaultSaslConfig.PLAIN),
    EXTERNAL(DefaultSaslConfig.EXTERNAL);

    private final SaslConfig saslConfig;

    SaslType(SaslConfig saslConfig) {
        this.saslConfig = saslConfig;
    }

    public SaslConfig getSaslConfig() {
        return saslConfig;
    }
}
