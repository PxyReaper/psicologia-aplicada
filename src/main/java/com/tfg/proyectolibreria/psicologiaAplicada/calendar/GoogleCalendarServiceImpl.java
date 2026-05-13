package com.tfg.proyectolibreria.psicologiaAplicada.calendar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GoogleCalendarServiceImpl implements GoogleCalendarService {

    private final GoogleCalendarApiClient apiClient;
    private final GoogleCalendarProperties properties;
    private final ObjectMapper objectMapper;

    public GoogleCalendarServiceImpl(GoogleCalendarApiClient apiClient, GoogleCalendarProperties properties, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a Google Calendar event for a therapy session.
     * Resolves or creates the "Psicologia Aplicada" calendar, then inserts the event.
     */
    @Override
    public void createSessionEvent(String patientFullName, LocalDateTime sessionDateTime, LocalDateTime sessionDateTimeEnd) {
        try {
            String calendarId = resolveOrCreateCalendar();
            ObjectNode eventNode = buildEventNode(patientFullName, sessionDateTime, sessionDateTimeEnd);

            apiClient.insertEvent(calendarId, eventNode);

            log.info("Calendar event created for session: {} from {} to {}", patientFullName, sessionDateTime, sessionDateTimeEnd);
        } catch (Exception e) {
            log.error("Failed to create calendar event for session: {}", patientFullName, e);
        }
    }

    /**
     * Lists all Google Calendars accessible by the service account.
     */
    @Override
    public List<String> listAccessibleCalendars() {
        List<String> result = new ArrayList<>();
        try {
            JsonNode response = apiClient.listCalendarList();
            JsonNode items = response.get("items");
            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    String id = item.get("id").asText();
                    String summary = item.has("summary") ? item.get("summary").asText() : "";
                    String accessRole = item.has("accessRole") ? item.get("accessRole").asText() : "";
                    result.add(id + " | " + summary + " | " + accessRole);
                }
            }
            log.info("Accessible calendars: {}", result);
        } catch (Exception e) {
            log.error("Failed to list calendars", e);
        }
        return result;
    }

    /**
     * Returns the ID of the "Psicologia Aplicada" calendar.
     * Searches existing calendars first; creates one if none is found,
     * and shares it with the configured email.
     */
    private String resolveOrCreateCalendar() throws IOException {
        String targetEmail = properties.getCalendarId();
        String calendarName = properties.getCalendarName();

        JsonNode calendarList = apiClient.listCalendarList();
        JsonNode items = calendarList.get("items");
        if (items != null && items.isArray()) {
            for (JsonNode entry : items) {
                String entryId = entry.get("id").asText();
                String entrySummary = entry.has("summary") ? entry.get("summary").asText() : "";
                if (targetEmail.equals(entryId) || targetEmail.equals(entrySummary) || calendarName.equals(entrySummary)) {
                    log.info("Found calendar: {} -> id={}", entrySummary, entryId);
                    return entryId;
                }
            }
        }

        log.warn("Calendar '{}' not found. Creating '{}'...", targetEmail, calendarName);

        JsonNode created = apiClient.insertCalendar(calendarName, properties.getTimezone());
        String createdId = created.get("id").asText();
        log.info("Created new calendar: {} -> id={}", calendarName, createdId);

        apiClient.insertAclRule(createdId, "writer", "user", targetEmail);
        log.info("Shared calendar '{}' with {}", calendarName, targetEmail);

        return createdId;
    }

    /**
     * Constructs a JSON event node with summary, start, and end fields
     * formatted for the Google Calendar API.
     */
    private ObjectNode buildEventNode(String patientFullName, LocalDateTime sessionDateTime, LocalDateTime sessionDateTimeEnd) {
        ZoneId zoneId = ZoneId.of(properties.getTimezone());

        ObjectNode eventNode = objectMapper.createObjectNode();
        eventNode.put("summary", "Sesi\u00f3n: " + patientFullName);

        ObjectNode startNode = objectMapper.createObjectNode();
        OffsetDateTime startOffset = sessionDateTime.atZone(zoneId).toOffsetDateTime();
        startNode.put("dateTime", startOffset.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        startNode.put("timeZone", properties.getTimezone());
        eventNode.set("start", startNode);

        ObjectNode endNode = objectMapper.createObjectNode();
        OffsetDateTime endOffset = sessionDateTimeEnd.atZone(zoneId).toOffsetDateTime();
        endNode.put("dateTime", endOffset.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        endNode.put("timeZone", properties.getTimezone());
        eventNode.set("end", endNode);

        return eventNode;
    }
}
