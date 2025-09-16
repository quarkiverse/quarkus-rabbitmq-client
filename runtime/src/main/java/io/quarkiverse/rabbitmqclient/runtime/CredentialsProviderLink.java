package io.quarkiverse.rabbitmqclient.runtime;

import static io.quarkus.credentials.CredentialsProvider.EXPIRATION_TIMESTAMP_PROPERTY_NAME;
import static io.quarkus.credentials.CredentialsProvider.PASSWORD_PROPERTY_NAME;
import static io.quarkus.credentials.CredentialsProvider.USER_PROPERTY_NAME;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import io.quarkus.credentials.CredentialsProvider;

public class CredentialsProviderLink implements com.rabbitmq.client.impl.CredentialsProvider {

    private final CredentialsProvider credentialsProvider;
    private final String credentialsProviderName;
    private String username;
    private String password;
    private Instant expiresAt;

    public CredentialsProviderLink(CredentialsProvider credentialsProvider, String credentialsProviderName) {
        this.credentialsProvider = credentialsProvider;
        this.credentialsProviderName = credentialsProviderName;
        this.expiresAt = Instant.MIN;
        refreshIfExpired();
    }

    private void refreshIfExpired() {
        if (expiresAt.isAfter(Instant.now())) {
            return;
        }
        refresh();
    }

    @Override
    public String getUsername() {
        refreshIfExpired();
        return username;
    }

    @Override
    public String getPassword() {
        refreshIfExpired();
        return password;
    }

    @Override
    public Duration getTimeBeforeExpiration() {
        return Duration.between(Instant.now(), expiresAt);
    }

    @Override
    public void refresh() {
        Map<String, String> credentials = credentialsProvider.getCredentials(credentialsProviderName);
        username = credentials.get(USER_PROPERTY_NAME);
        password = credentials.get(PASSWORD_PROPERTY_NAME);
        expiresAt = Instant.parse(credentials.getOrDefault(EXPIRATION_TIMESTAMP_PROPERTY_NAME, getDefaultExpiresAt()));
    }

    public String getDefaultExpiresAt() {
        return Instant.now().plusSeconds(10).toString();
    }
}