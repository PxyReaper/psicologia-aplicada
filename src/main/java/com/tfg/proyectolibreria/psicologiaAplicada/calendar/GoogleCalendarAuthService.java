package com.tfg.proyectolibreria.psicologiaAplicada.calendar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Component
public class GoogleCalendarAuthService {

    private static final long TOKEN_EXPIRY_BUFFER_SECONDS = 60;

    private final GoogleCalendarProperties properties;
    private final ObjectMapper objectMapper;

    private String cachedAccessToken;
    private long tokenExpiresAtEpochSecond;

    public GoogleCalendarAuthService(GoogleCalendarProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * Returns a valid OAuth2 access token for the Google Calendar API.
     * Reuses the cached token if it has not expired, otherwise refreshes it.
     */
    public synchronized String getAccessToken() {
        long now = Instant.now().getEpochSecond();
        if (cachedAccessToken != null && now < tokenExpiresAtEpochSecond - TOKEN_EXPIRY_BUFFER_SECONDS) {
            return cachedAccessToken;
        }
        return refreshAccessToken();
    }

    /**
     * Reads the service account credentials file, builds a signed JWT assertion,
     * and exchanges it for an OAuth2 access token via the Google token endpoint.
     */
    private String refreshAccessToken() {
        try {
            JsonNode creds = objectMapper.readTree(new File(properties.getCredentialsPath()));
            String clientEmail = creds.get("client_email").asText();
            String tokenUri = creds.get("token_uri").asText();

            PrivateKey privateKey = parsePrivateKey(creds.get("private_key").asText());

            long issuedAt = Instant.now().getEpochSecond();
            long expiration = issuedAt + 3600;

            String jwt = buildJwtAssertion(clientEmail, tokenUri, issuedAt, expiration, privateKey);

            String tokenResponseBody = exchangeJwtForToken(tokenUri, jwt);

            JsonNode tokenResponse = objectMapper.readTree(tokenResponseBody);
            cachedAccessToken = tokenResponse.get("access_token").asText();
            tokenExpiresAtEpochSecond = Instant.now().getEpochSecond() + tokenResponse.get("expires_in").asLong();

            log.info("Obtained new OAuth2 access token, expires in {}s", tokenResponse.get("expires_in").asLong());
            return cachedAccessToken;
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to obtain Google Calendar access token", e);
        }
    }

    /**
     * Decodes a PEM-encoded PKCS#8 private key string into a {@link PrivateKey} instance.
     */
    private PrivateKey parsePrivateKey(String privateKeyPem) throws GeneralSecurityException {
        String cleanedKey = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(cleanedKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * Creates a signed JWT assertion for the service account OAuth2 flow (RFC 7523).
     */
    private String buildJwtAssertion(String clientEmail, String tokenUri, long issuedAt, long expiration, PrivateKey privateKey) throws GeneralSecurityException, IOException {
        ObjectNode headerNode = objectMapper.createObjectNode();
        headerNode.put("alg", "RS256");
        headerNode.put("typ", "JWT");
        String headerB64 = base64UrlEncode(objectMapper.writeValueAsBytes(headerNode));

        ObjectNode claimsNode = objectMapper.createObjectNode();
        claimsNode.put("iss", clientEmail);
        claimsNode.put("scope", properties.getScope());
        claimsNode.put("aud", tokenUri);
        claimsNode.put("exp", expiration);
        claimsNode.put("iat", issuedAt);
        String claimsB64 = base64UrlEncode(objectMapper.writeValueAsBytes(claimsNode));

        String toSign = headerB64 + "." + claimsB64;

        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(toSign.getBytes(StandardCharsets.UTF_8));
        String signatureB64 = base64UrlEncode(signer.sign());

        return toSign + "." + signatureB64;
    }

    /**
     * Exchanges a signed JWT assertion for an OAuth2 access token.
     */
    private String exchangeJwtForToken(String tokenUri, String jwt) {
        RestClient tokenClient = RestClient.create();

        String tokenRequestBody = "grant_type=" + urlEncode("urn:ietf:params:oauth:grant-type:jwt-bearer")
                + "&assertion=" + urlEncode(jwt);

        return tokenClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(tokenRequestBody)
                .retrieve()
                .body(String.class);
    }

    private static String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
