package com.contactpro.contactpro.service;

import com.contactpro.contactpro.model.GoogleOAuthToken;
import com.contactpro.contactpro.repository.GoogleOAuthTokenRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
public class GoogleOAuthService {

    private final GoogleOAuthTokenRepository tokenRepository;
    private final OAuthStateService stateService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    @Value("${google.scope}")
    private String scope;

    public GoogleOAuthService(GoogleOAuthTokenRepository tokenRepository, OAuthStateService stateService) {
        this.tokenRepository = tokenRepository;
        this.stateService = stateService;
    }

    public String buildAuthUrl(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        String state = stateService.createState(userId);

        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + encode(clientId)
                + "&redirect_uri=" + encode(redirectUri)
                + "&response_type=code"
                + "&scope=" + encode(scope)
                + "&access_type=offline"
                + "&prompt=consent"
                + "&state=" + encode(state);
    }

    public void handleCallback(String code, String state) throws Exception {
        Long userId = stateService.consumeState(state);

        String body = "code=" + encode(code)
                + "&client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret)
                + "&redirect_uri=" + encode(redirectUri)
                + "&grant_type=authorization_code";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth2.googleapis.com/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode tokenJson = objectMapper.readTree(response.body());

        if (response.statusCode() >= 400 || tokenJson.get("access_token") == null) {
            throw new RuntimeException("Failed to exchange Google auth code");
        }

        GoogleOAuthToken token = tokenRepository.findByUserId(userId)
                .orElseGet(GoogleOAuthToken::new);

        token.setUserId(userId);
        token.setAccessToken(tokenJson.get("access_token").asText());

        if (tokenJson.has("refresh_token")) {
            token.setRefreshToken(tokenJson.get("refresh_token").asText());
        }

        token.setTokenType(tokenJson.path("token_type").asText("Bearer"));
        token.setExpiresAt(Instant.now().plusSeconds(tokenJson.path("expires_in").asLong(3600) - 60));

        tokenRepository.save(token);
    }

    public String getValidAccessToken(Long userId) throws Exception {
        GoogleOAuthToken token = tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Google account is not connected"));

        if (token.getExpiresAt() != null && Instant.now().isBefore(token.getExpiresAt())) {
            return token.getAccessToken();
        }

        if (token.getRefreshToken() == null || token.getRefreshToken().isBlank()) {
            throw new RuntimeException("Refresh token missing. Reconnect your Google account.");
        }

        String body = "client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret)
                + "&refresh_token=" + encode(token.getRefreshToken())
                + "&grant_type=refresh_token";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth2.googleapis.com/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode tokenJson = objectMapper.readTree(response.body());

        if (response.statusCode() >= 400 || tokenJson.get("access_token") == null) {
            throw new RuntimeException("Failed to refresh Google access token");
        }

        token.setAccessToken(tokenJson.get("access_token").asText());
        token.setExpiresAt(Instant.now().plusSeconds(tokenJson.path("expires_in").asLong(3600) - 60));
        tokenRepository.save(token);

        return token.getAccessToken();
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
