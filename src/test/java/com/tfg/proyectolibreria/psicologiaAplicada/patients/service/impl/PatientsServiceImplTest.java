package com.tfg.proyectolibreria.psicologiaAplicada.patients.service.impl;

import com.tfg.proyectolibreria.psicologiaAplicada.patients.PatientsEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.dto.PatientsRequestDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.enums.Genre;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.event.PatientCreatedEvent;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.event.PatientUpdatedEvent;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.repository.PatientsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientsServiceImplTest {

    @Mock
    private PatientsRepository patientsRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PatientsServiceImpl patientsService;

    @Test
    void save_shouldPersistPatientAndPublishEvent() {
        PatientsRequestDTO dto = new PatientsRequestDTO("John", "Doe", LocalDate.of(1990, 1, 1), "612345678", Genre.MASCULINO, "Initial observation");

        PatientsEntity savedEntity = new PatientsEntity(1L, "John", "Doe", LocalDate.now(), null, LocalDate.of(1990, 1, 1), "612345678", Genre.MASCULINO);
        when(patientsRepository.save(any(PatientsEntity.class))).thenReturn(savedEntity);

        patientsService.save(dto);

        ArgumentCaptor<PatientsEntity> entityCaptor = ArgumentCaptor.forClass(PatientsEntity.class);
        verify(patientsRepository).save(entityCaptor.capture());
        PatientsEntity captured = entityCaptor.getValue();
        assertThat(captured.getName()).isEqualTo("John");
        assertThat(captured.getStartDate()).isEqualTo(LocalDate.now());
        assertThat(captured.getEndDate()).isNull();

        ArgumentCaptor<PatientCreatedEvent> eventCaptor = ArgumentCaptor.forClass(PatientCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().patientId()).isEqualTo(1L);
        assertThat(eventCaptor.getValue().observation()).isEqualTo("Initial observation");
    }

    @Test
    void save_shouldPublishEventWithNullObservation() {
        PatientsRequestDTO dto = new PatientsRequestDTO("John", "Doe", LocalDate.of(1990, 1, 1), "612345678", Genre.MASCULINO, null);

        PatientsEntity savedEntity = new PatientsEntity(1L, "John", "Doe", LocalDate.now(), null, LocalDate.of(1990, 1, 1), "612345678", Genre.MASCULINO);
        when(patientsRepository.save(any(PatientsEntity.class))).thenReturn(savedEntity);

        patientsService.save(dto);

        verify(eventPublisher).publishEvent(any(PatientCreatedEvent.class));
    }

    @Test
    void update_shouldUpdatePatientAndPublishEvent() {
        PatientsRequestDTO dto = new PatientsRequestDTO("Jane", "Smith", LocalDate.of(1992, 5, 10), "987654321", Genre.FEMENINO, "Updated observation");

        PatientsEntity existing = new PatientsEntity(1L, "Old", "Name", LocalDate.of(2024, 1, 1), null, LocalDate.of(1990, 1, 1), "612345678", Genre.MASCULINO);
        when(patientsRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(patientsRepository.save(any(PatientsEntity.class))).thenAnswer(i -> i.getArgument(0));

        patientsService.update(1L, dto);

        ArgumentCaptor<PatientsEntity> entityCaptor = ArgumentCaptor.forClass(PatientsEntity.class);
        verify(patientsRepository).save(entityCaptor.capture());
        PatientsEntity captured = entityCaptor.getValue();
        assertThat(captured.getId()).isEqualTo(1L);
        assertThat(captured.getName()).isEqualTo("Jane");
        assertThat(captured.getSurname()).isEqualTo("Smith");
        assertThat(captured.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));

        verify(eventPublisher).publishEvent(any(PatientUpdatedEvent.class));
    }

    @Test
    void update_shouldThrowWhenPatientNotFound() {
        PatientsRequestDTO dto = new PatientsRequestDTO("Jane", "Smith", LocalDate.of(1992, 5, 10), "987654321", Genre.FEMENINO, null);
        when(patientsRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientsService.update(99L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Patient not found");
    }

    @Test
    void discharge_shouldSetEndDateToNow() {
        PatientsEntity existing = new PatientsEntity(1L, "John", "Doe", LocalDate.of(2024, 1, 1), null, LocalDate.of(1990, 1, 1), "612345678", Genre.MASCULINO);
        when(patientsRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(patientsRepository.save(any(PatientsEntity.class))).thenAnswer(i -> i.getArgument(0));

        patientsService.discharge(1L);

        ArgumentCaptor<PatientsEntity> entityCaptor = ArgumentCaptor.forClass(PatientsEntity.class);
        verify(patientsRepository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getEndDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void discharge_shouldThrowWhenPatientNotFound() {
        when(patientsRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientsService.discharge(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Patient not found");
    }
}
