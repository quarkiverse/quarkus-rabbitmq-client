package io.quarkiverse.rabbitmqclient.runtime;

import java.time.Duration;
import java.time.Instant;

import io.quarkiverse.rabbitmqclient.core.runtime.RefreshingCredentials;
import io.quarkus.credentials.CredentialsProvider;

/**
 * Adapts a Quarkus {@link CredentialsProvider} to the AMQP 0-9-1 client's credentials provider.
 */
public class CredentialsProviderLink implements com.rabbitmq.client.impl.CredentialsProvider {

    private final RefreshingCredentials credentials;

    public CredentialsProviderLink(CredentialsProvider credentialsProvider, String credentialsProviderName) {
        this.credentials = new RefreshingCredentials(credentialsProvider, credentialsProviderName);
    }

    @Override
    public String getUsername() {
        return credentials.getUsername();
    }

    @Override
    public String getPassword() {
        return credentials.getPassword();
    }

    @Override
    public Duration getTimeBeforeExpiration() {
        return credentials.getTimeBeforeExpiration();
    }

    @Override
    public void refresh() {
        credentials.refresh();
    }

    public String getDefaultExpiresAt() {
        return Instant.now().plusSeconds(10).toString();
    }
}
