package com.contactpro.contactpro.service;

import com.contactpro.contactpro.dto.GoogleContactDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class GooglePeopleService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<GoogleContactDto> getMyContacts(String accessToken) throws Exception {
        List<GoogleContactDto> contacts = new ArrayList<>();
        String pageToken = null;

        do {
            String url = "https://people.googleapis.com/v1/people/me/connections"
                    + "?personFields=names,emailAddresses,phoneNumbers"
                    + "&pageSize=500"
                    + (pageToken != null ? "&pageToken=" + URLEncoder.encode(pageToken, StandardCharsets.UTF_8) : "");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new RuntimeException("Google People API failed: " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode connections = root.path("connections");

            if (connections.isArray()) {
                for (JsonNode person : connections) {
                    String name = getFirstField(person, "names", "displayName");
                    String email = getFirstField(person, "emailAddresses", "value");
                    String phone = getFirstField(person, "phoneNumbers", "value");

                    if (isBlank(name) && isBlank(email) && isBlank(phone)) {
                        continue;
                    }

                    contacts.add(new GoogleContactDto(name, email, phone));
                }
            }

            pageToken = root.has("nextPageToken") ? root.get("nextPageToken").asText() : null;
        } while (pageToken != null && !pageToken.isBlank());

        return contacts;
    }

    private String getFirstField(JsonNode node, String arrayField, String valueField) {
        JsonNode array = node.path(arrayField);
        if (array.isArray() && !array.isEmpty()) {
            JsonNode first = array.get(0);
            if (first.has(valueField)) {
                return first.get(valueField).asText(null);
            }
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
