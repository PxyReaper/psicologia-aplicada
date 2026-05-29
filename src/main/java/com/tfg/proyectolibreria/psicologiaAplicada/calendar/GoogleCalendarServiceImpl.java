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
     * Returns the event ID from Google Calendar.
     */
    @Override
    public String createSessionEvent(String patientFullName, LocalDateTime sessionDateTime, LocalDateTime sessionDateTimeEnd) {
        try {
            String calendarId = resolveOrCreateCalendar();
            ObjectNode eventNode = buildEventNode(patientFullName, sessionDateTime, sessionDateTimeEnd);

            JsonNode created = apiClient.insertEvent(calendarId, eventNode);
            String eventId = created.get("id").asText();

            log.info("Calendar event created for session: {} (eventId: {})", patientFullName, eventId);
            return eventId;
        } catch (Exception e) {
            log.error("Failed to create calendar event for session: {}", patientFullName, e);
            return null;
        }
    }

    /**
     * Updates an existing Google Calendar event with new date/time and patient name.
     */
    @Override
    public void updateSessionEvent(String eventId, String patientFullName, LocalDateTime sessionDateTime, LocalDateTime sessionDateTimeEnd) {
        try {
            String calendarId = resolveOrCreateCalendar();
            ObjectNode eventNode = buildEventNode(patientFullName, sessionDateTime, sessionDateTimeEnd);
            apiClient.updateEvent(calendarId, eventId, eventNode);
            log.info("Calendar event updated: {} (eventId: {})", patientFullName, eventId);
        } catch (Exception e) {
            log.error("Failed to update calendar event: {} (eventId: {})", patientFullName, eventId, e);
        }
    }

    /**
     * Deletes a Google Calendar event by its event ID.
     */
    @Override
    public void deleteSessionEvent(String eventId) {
        try {
            String calendarId = resolveOrCreateCalendar();
            apiClient.deleteEvent(calendarId, eventId);
            log.info("Deleted calendar event: {}", eventId);
        } catch (Exception e) {
            log.error("Failed to delete calendar event: {}", eventId, e);
        }
    }

    /**
     * Searches for a Google Calendar event by patient name and session date.
     * Returns the event ID if found, or null if not found.
     */
    @Override
    public String findSessionEventId(String patientFullName, LocalDateTime sessionDateTime) {
        try {
            String calendarId = resolveOrCreateCalendar();
            ZoneId zoneId = ZoneId.of(properties.getTimezone());

            OffsetDateTime dayStart = sessionDateTime.toLocalDate().atStartOfDay().atZone(zoneId).toOffsetDateTime();
            OffsetDateTime dayEnd = sessionDateTime.toLocalDate().plusDays(1).atStartOfDay().atZone(zoneId).toOffsetDateTime();

            String timeMin = dayStart.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            String timeMax = dayEnd.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            JsonNode events = apiClient.listEvents(calendarId, timeMin, timeMax);
            JsonNode items = events.get("items");

            if (items != null && items.isArray()) {
                String expectedSummary = "Sesi\u00f3n: " + patientFullName;
                for (JsonNode item : items) {
                    String summary = item.has("summary") ? item.get("summary").asText() : "";
                    if (expectedSummary.equals(summary)) {
                        String eventId = item.get("id").asText();
                        log.info("Found calendar event for {}: (eventId: {})", patientFullName, eventId);
                        return eventId;
                    }
                }
            }
            log.warn("No calendar event found for {} on {}", patientFullName, sessionDateTime.toLocalDate());
            return null;
        } catch (Exception e) {
            log.error("Failed to search calendar event for session: {}", patientFullName, e);
            return null;
        }
    }

    /**
     * Deletes a Google Calendar event by searching for it on the session date
     * matching the patient's full name. Used as fallback when no eventId is stored.
     */
    @Override
    public void deleteSessionEvent(String patientFullName, LocalDateTime sessionDateTime, LocalDateTime sessionDateTimeEnd) {
        try {
            String eventId = findSessionEventId(patientFullName, sessionDateTime);
            if (eventId != null) {
                String calendarId = resolveOrCreateCalendar();
                apiClient.deleteEvent(calendarId, eventId);
                log.info("Deleted calendar event (fallback): {} (eventId: {})", patientFullName, eventId);
            }
        } catch (Exception e) {
            log.error("Failed to delete calendar event (fallback) for session: {}", patientFullName, e);
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
