package com.tfg.proyectolibreria.psicologiaAplicada.calendar;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class GoogleCalendarServiceImpl implements GoogleCalendarService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarServiceImpl.class);

    private final GoogleCalendarProperties properties;

    public GoogleCalendarServiceImpl(GoogleCalendarProperties properties) {
        this.properties = properties;
    }

    private Calendar buildCalendarService() throws IOException, GeneralSecurityException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                        new FileInputStream(properties.getCredentialsPath()))
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/calendar"));

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(properties.getApplicationName())
                .build();
    }

    @Override
    public List<String> listAccessibleCalendars() {
        List<String> result = new ArrayList<>();
        try {
            Calendar calendarService = buildCalendarService();
            CalendarList calendarList = calendarService.calendarList().list().execute();
            for (CalendarListEntry entry : calendarList.getItems()) {
                result.add(entry.getId() + " | " + entry.getSummary() + " | " + entry.getAccessRole());
            }
            logger.info("Accessible calendars: {}", result);
        } catch (Exception e) {
            logger.error("Failed to list calendars", e);
        }
        return result;
    }

    @Override
    public void createSessionEvent(String patientFullName, LocalDateTime sessionDateTime, LocalDateTime sessionDateTimeEnd) {
        try {
            Calendar calendarService = buildCalendarService();

            String calendarId = resolveOrCreateCalendar(calendarService);

            Event event = new Event();
            event.setSummary("Sesi\u00f3n: " + patientFullName);

            Date startDate = Date.from(sessionDateTime.atZone(ZoneId.systemDefault()).toInstant());
            DateTime startDateTime = new DateTime(startDate);
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone(ZoneId.systemDefault().toString());
            event.setStart(start);

            Date endDate = Date.from(sessionDateTimeEnd.atZone(ZoneId.systemDefault()).toInstant());
            DateTime endDateTime = new DateTime(endDate);
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone(ZoneId.systemDefault().toString());
            event.setEnd(end);

            calendarService.events().insert(calendarId, event).execute();

            logger.info("Calendar event created for session: {} from {} to {}", patientFullName, sessionDateTime, sessionDateTimeEnd);
        } catch (Exception e) {
            logger.error("Failed to create calendar event for session: {}", patientFullName, e);
        }
    }

    private static final String CALENDAR_NAME = "Psicologia Aplicada";

    private String resolveOrCreateCalendar(Calendar calendarService) throws IOException {
        String targetEmail = properties.getCalendarId();
        CalendarList calendarList = calendarService.calendarList().list().execute();

        for (CalendarListEntry entry : calendarList.getItems()) {
            if (targetEmail.equals(entry.getId())
                    || targetEmail.equals(entry.getSummary())
                    || CALENDAR_NAME.equals(entry.getSummary())) {
                logger.info("Found calendar: {} -> id={}", entry.getSummary(), entry.getId());
                return entry.getId();
            }
        }

        logger.warn("Calendar '{}' not found. Creating '{}'...", targetEmail, CALENDAR_NAME);

        com.google.api.services.calendar.model.Calendar newCalendar = new com.google.api.services.calendar.model.Calendar();
        newCalendar.setSummary(CALENDAR_NAME);
        newCalendar.setTimeZone(ZoneId.systemDefault().toString());
        com.google.api.services.calendar.model.Calendar created = calendarService.calendars().insert(newCalendar).execute();
        logger.info("Created new calendar: {} -> id={}", created.getSummary(), created.getId());

        AclRule rule = new AclRule();
        rule.setScope(new AclRule.Scope().setType("user").setValue(targetEmail));
        rule.setRole("writer");
        calendarService.acl().insert(created.getId(), rule).execute();
        logger.info("Shared calendar '{}' with {}", created.getSummary(), targetEmail);

        return created.getId();
    }
}
