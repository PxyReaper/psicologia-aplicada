package com.tfg.proyectolibreria.psicologiaAplicada.session.service.impl;

import com.tfg.proyectolibreria.psicologiaAplicada.calendar.CalendarAsyncService;
import com.tfg.proyectolibreria.psicologiaAplicada.kernel.Patient;
import com.tfg.proyectolibreria.psicologiaAplicada.kernel.PatientAccess;
import com.tfg.proyectolibreria.psicologiaAplicada.session.SessionEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.session.dto.SessionRequestDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.session.dto.SessionWithPatientDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.session.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private PatientAccess patientAccess;

    @Mock
    private CalendarAsyncService calendarAsyncService;

    @InjectMocks
    private SessionServiceImpl sessionService;

    @Test
    void save_shouldCallRepositorySaveAndAsyncCalendarService() {
        LocalDateTime now = LocalDateTime.now();
        SessionRequestDTO request = new SessionRequestDTO(now, now.plusHours(1), "obs", "summary", 1L);
        when(patientAccess.findById(1L)).thenReturn(Optional.of(new com.tfg.proyectolibreria.psicologiaAplicada.kernel.Patient() {
            @Override
            public Long getId() { return 1L; }
            @Override
            public String getName() { return "John"; }
            @Override
            public String getSurname() { return "Doe"; }
        }));

        SessionEntity savedEntity = new SessionEntity(1L, now, now.plusHours(1), "obs", "summary", false, 1L);
        when(sessionRepository.save(any(SessionEntity.class))).thenReturn(savedEntity);

        sessionService.save(request);

        ArgumentCaptor<SessionEntity> entityCaptor = ArgumentCaptor.forClass(SessionEntity.class);
        verify(sessionRepository).save(entityCaptor.capture());
        SessionEntity saved = entityCaptor.getValue();
        assertThat(saved.getPatientId()).isEqualTo(1L);
        assertThat(saved.getObservation()).isEqualTo("obs");

        verify(calendarAsyncService).createAndStoreEvent(
                savedEntity.getId(), "John Doe", now, now.plusHours(1)
        );
    }

    @Test
    void findByDate_shouldReturnSessionsWithPatientNames() {
        LocalDate date = LocalDate.of(2026, 5, 25);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        SessionEntity session1 = new SessionEntity(1L, startOfDay.plusHours(10), startOfDay.plusHours(11), "obs1", "summary1", false, 1L);
        SessionEntity session2 = new SessionEntity(2L, startOfDay.plusHours(12), startOfDay.plusHours(13), "obs2", "summary2", true, 2L);

        when(sessionRepository.findBySessionDateBetween(startOfDay, endOfDay))
                .thenReturn(List.of(session1, session2));

        when(patientAccess.findByIdIn(List.of(1L, 2L))).thenReturn(List.of(
                new Patient() {
                    @Override public Long getId() { return 1L; }
                    @Override public String getName() { return "John"; }
                    @Override public String getSurname() { return "Doe"; }
                },
                new Patient() {
                    @Override public Long getId() { return 2L; }
                    @Override public String getName() { return "Jane"; }
                    @Override public String getSurname() { return "Smith"; }
                }
        ));

        List<SessionWithPatientDTO> result = sessionService.findByDate(date);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).patientName()).isEqualTo("John");
        assertThat(result.get(0).patientSurname()).isEqualTo("Doe");
        assertThat(result.get(1).patientName()).isEqualTo("Jane");
        assertThat(result.get(1).patientSurname()).isEqualTo("Smith");
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(1).id()).isEqualTo(2L);
    }

    @Test
    void findByDate_shouldReturnEmptyListWhenNoSessions() {
        LocalDate date = LocalDate.of(2026, 5, 25);
        when(sessionRepository.findBySessionDateBetween(any(), any())).thenReturn(List.of());

        List<SessionWithPatientDTO> result = sessionService.findByDate(date);

        assertThat(result).isEmpty();
    }

    @Test
    void save_shouldThrowWhenPatientNotFound() {
        LocalDateTime now = LocalDateTime.now();
        SessionRequestDTO request = new SessionRequestDTO(now, now.plusHours(1), "obs", "summary", 99L);
        when(patientAccess.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Patient not found");
    }

    @Test
    void update_shouldCallRepositorySaveAndAsyncCalendarService() {
        LocalDateTime now = LocalDateTime.now();
        SessionRequestDTO request = new SessionRequestDTO(now, now.plusHours(1), "updated-obs", "updated-summary", 1L);

        SessionEntity existing = new SessionEntity(1L, now.minusDays(1), now.minusDays(1).plusHours(1), "old-obs", "old-summary", false, 1L);
        existing.setGoogleEventId("event-123");
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(existing));

        when(patientAccess.findById(1L)).thenReturn(Optional.of(new Patient() {
            @Override public Long getId() { return 1L; }
            @Override public String getName() { return "John"; }
            @Override public String getSurname() { return "Doe"; }
        }));
        when(patientAccess.findById(existing.getPatientId())).thenReturn(Optional.of(new Patient() {
            @Override public Long getId() { return 1L; }
            @Override public String getName() { return "John"; }
            @Override public String getSurname() { return "Doe"; }
        }));

        when(sessionRepository.save(any(SessionEntity.class))).thenAnswer(i -> i.getArgument(0));

        sessionService.update(1L, request);

        verify(calendarAsyncService).updateAndStoreEvent(
                eq(1L), eq("John Doe"), eq(now), eq(now.plusHours(1)),
                eq("event-123"), eq("John Doe"),
                eq(now.minusDays(1)), eq(now.minusDays(1).plusHours(1))
        );
    }

    @Test
    void update_shouldThrowWhenSessionNotFound() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.update(99L, new SessionRequestDTO(null, null, null, null, 1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Session not found");
    }

    @Test
    void delete_shouldRemoveSessionAndDeleteEvent() {
        SessionEntity session = new SessionEntity(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), "obs", "summary", false, 1L);
        session.setGoogleEventId("event-123");
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        sessionService.delete(1L);

        verify(sessionRepository).deleteById(1L);
        verify(calendarAsyncService).deleteEvent("event-123");
    }

    @Test
    void delete_shouldThrowWhenSessionNotFound() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.delete(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Session not found");
    }
}
