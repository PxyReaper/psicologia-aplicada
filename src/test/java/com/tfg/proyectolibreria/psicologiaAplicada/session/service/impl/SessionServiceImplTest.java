package com.tfg.proyectolibreria.psicologiaAplicada.session.service.impl;

import com.tfg.proyectolibreria.psicologiaAplicada.session.SessionEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.session.dto.SessionRequestDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.session.repository.SessionRepository;
import com.tfg.proyectolibreria.psicologiaAplicada.kernel.PatientAccess;
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

    @InjectMocks
    private SessionServiceImpl sessionService;

    @Test
    void save_shouldCallRepositorySave() {
        SessionRequestDTO request = new SessionRequestDTO(LocalDateTime.now(), "obs", "summary", 1L);
        when(patientAccess.findById(1L)).thenReturn(Optional.of(new com.tfg.proyectolibreria.psicologiaAplicada.kernel.Patient() {
            @Override
            public Long getId() { return 1L; }
            @Override
            public String getName() { return "John"; }
            @Override
            public String getSurname() { return "Doe"; }
        }));

        sessionService.save(request);

        ArgumentCaptor<SessionEntity> captor = ArgumentCaptor.forClass(SessionEntity.class);
        verify(sessionRepository).save(captor.capture());
        SessionEntity saved = captor.getValue();
        assertThat(saved.getPatientId()).isEqualTo(1L);
        assertThat(saved.getObservation()).isEqualTo("obs");
    }
}
