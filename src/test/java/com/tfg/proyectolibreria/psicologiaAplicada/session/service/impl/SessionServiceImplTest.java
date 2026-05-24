package com.tfg.proyectolibreria.psicologiaAplicada.session.service.impl;

import com.tfg.proyectolibreria.psicologiaAplicada.calendar.CalendarAsyncService;
import com.tfg.proyectolibreria.psicologiaAplicada.kernel.PatientAccess;
import com.tfg.proyectolibreria.psicologiaAplicada.session.SessionEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.session.dto.SessionRequestDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.session.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
}
