package io.quarkiverse.rabbitmqclient.core.runtime;

import static io.quarkus.credentials.CredentialsProvider.EXPIRATION_TIMESTAMP_PROPERTY_NAME;
import static io.quarkus.credentials.CredentialsProvider.PASSWORD_PROPERTY_NAME;
import static io.quarkus.credentials.CredentialsProvider.USER_PROPERTY_NAME;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import io.quarkus.credentials.CredentialsProvider;

/**
 * Caches the username and password of a Quarkus {@link CredentialsProvider} and refreshes them on access once
 * they have expired.
 * <p>
 * The expiration is taken from the {@link CredentialsProvider#EXPIRATION_TIMESTAMP_PROPERTY_NAME} property. As long
 * as no timestamp has ever been returned, the credentials are refreshed on every access. A refresh that returns no
 * timestamp keeps the previously seen one, so once a timestamp has been seen, refreshes only happen after it has
 * passed (or on an explicit {@link #refresh()}).
 * </p>
 */
public class RefreshingCredentials {

    private final CredentialsProvider credentialsProvider;
    private final String credentialsProviderName;
    private String username;
    private String password;
    private Instant expiresAt;

    public RefreshingCredentials(CredentialsProvider credentialsProvider, String credentialsProviderName) {
        this.credentialsProvider = credentialsProvider;
        this.credentialsProviderName = credentialsProviderName;
        refresh();
    }

    private void refreshIfExpired() {
        if (expiresAt != null && expiresAt.isAfter(Instant.now())) {
            return;
        }
        refresh();
    }

    public String getUsername() {
        refreshIfExpired();
        return username;
    }

    public String getPassword() {
        refreshIfExpired();
        return password;
    }

    /**
     * @return the time left before the credentials expire, or {@code null} if they carry no expiration timestamp.
     */
    public Duration getTimeBeforeExpiration() {
        return expiresAt != null ? Duration.between(Instant.now(), expiresAt) : null;
    }

    public void refresh() {
        Map<String, String> credentials = credentialsProvider.getCredentials(credentialsProviderName);
        username = credentials.get(USER_PROPERTY_NAME);
        password = credentials.get(PASSWORD_PROPERTY_NAME);
        String expirationTimestamp;
        if ((expirationTimestamp = credentials.get(EXPIRATION_TIMESTAMP_PROPERTY_NAME)) != null) {
            expiresAt = Instant.parse(expirationTimestamp);
        }
    }

}
