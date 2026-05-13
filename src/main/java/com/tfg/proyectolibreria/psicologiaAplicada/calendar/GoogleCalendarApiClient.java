package com.tfg.proyectolibreria.psicologiaAplicada.calendar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Low-level HTTP client for the Google Calendar v3 REST API.
 * Each method maps to a single API endpoint and returns raw JSON responses.
 */
@Slf4j
@Component
public class GoogleCalendarApiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GoogleCalendarApiClient(GoogleCalendarAuthService authService, GoogleCalendarProperties properties, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().setBearerAuth(authService.getAccessToken());
                    return execution.execute(request, body);
                })
                .build();
    }

    /**
     * GET /users/me/calendarList
     * Lists all calendars accessible by the authenticated account.
     */
    public JsonNode listCalendarList() {
        String responseBody = restClient.get()
                .uri("/users/me/calendarList")
                .retrieve()
                .body(String.class);
        return parseJson(responseBody);
    }

    /**
     * POST /calendars
     * Creates a secondary calendar with the given name and time zone.
     */
    public JsonNode insertCalendar(String summary, String timeZone) {
        ObjectNode calendarNode = objectMapper.createObjectNode();
        calendarNode.put("summary", summary);
        calendarNode.put("timeZone", timeZone);
        return postJson("/calendars", calendarNode);
    }

    /**
     * POST /calendars/{calendarId}/acl
     * Grants a scope (user/group/domain) a role (reader/writer/owner) on the calendar.
     */
    public JsonNode insertAclRule(String calendarId, String role, String scopeType, String scopeValue) {
        ObjectNode aclNode = objectMapper.createObjectNode();
        aclNode.put("role", role);
        ObjectNode scopeNode = objectMapper.createObjectNode();
        scopeNode.put("type", scopeType);
        scopeNode.put("value", scopeValue);
        aclNode.set("scope", scopeNode);
        return postJson("/calendars/{calendarId}/acl", aclNode, calendarId);
    }

    /**
     * POST /calendars/{calendarId}/events
     * Inserts an event into the specified calendar.
     */
    public JsonNode insertEvent(String calendarId, ObjectNode eventNode) {
        return postJson("/calendars/{calendarId}/events", eventNode, calendarId);
    }

    /**
     * Sends a POST request with a JSON body to the given URI and returns the parsed response.
     */
    private JsonNode postJson(String uri, ObjectNode body, Object... uriVariables) {
        String responseBody = restClient.post()
                .uri(uri, uriVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);
        return parseJson(responseBody);
    }

    /**
     * Parses a raw JSON string into a Jackson JsonNode tree.
     */
    private JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Google Calendar API response", e);
        }
    }
}
