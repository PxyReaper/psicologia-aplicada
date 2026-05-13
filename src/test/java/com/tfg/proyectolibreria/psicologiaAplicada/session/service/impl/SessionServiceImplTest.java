package com.tfg.proyectolibreria.psicologiaAplicada.session.service.impl;

import com.tfg.proyectolibreria.psicologiaAplicada.kernel.PatientAccess;
import com.tfg.proyectolibreria.psicologiaAplicada.session.SessionEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.session.dto.SessionRequestDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.session.event.SessionCreatedEvent;
import com.tfg.proyectolibreria.psicologiaAplicada.session.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SessionServiceImpl sessionService;

    @Test
    void save_shouldCallRepositorySaveAndPublishEvent() {
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

        sessionService.save(request);

        ArgumentCaptor<SessionEntity> entityCaptor = ArgumentCaptor.forClass(SessionEntity.class);
        verify(sessionRepository).save(entityCaptor.capture());
        SessionEntity saved = entityCaptor.getValue();
        assertThat(saved.getPatientId()).isEqualTo(1L);
        assertThat(saved.getObservation()).isEqualTo("obs");

        ArgumentCaptor<SessionCreatedEvent> eventCaptor = ArgumentCaptor.forClass(SessionCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        SessionCreatedEvent event = eventCaptor.getValue();
        assertThat(event.patientId()).isEqualTo(1L);
        assertThat(event.patientFullName()).isEqualTo("John Doe");
        assertThat(event.sessionDateTime()).isEqualTo(now);
        assertThat(event.sessionDateTimeEnd()).isEqualTo(now.plusHours(1));
    }
}
