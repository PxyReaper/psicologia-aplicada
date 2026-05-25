package com.tfg.proyectolibreria.psicologiaAplicada.observations.service;

import com.tfg.proyectolibreria.psicologiaAplicada.kernel.Patient;
import com.tfg.proyectolibreria.psicologiaAplicada.kernel.PatientAccess;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.ObservationsEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.dto.PatientObservationsDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.repository.ObservationsRepository;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.service.impl.ObservationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObservationServiceImplTest {

    @Mock
    private PatientAccess patientAccess;

    @Mock
    private ObservationsRepository observationsRepository;

    @InjectMocks
    private ObservationServiceImpl observationService;

    @Test
    void getActivePatientsWithObservations_shouldReturnPatientWithObservations() {
        Patient patient = new Patient() {
            @Override            public Long getId() {                return 1L;            }
            @Override            public String getName() {                return "John";            }
            @Override            public String getSurname() {                return "Doe";            }
        };

        ObservationsEntity obs = new ObservationsEntity(null, "Test observation", 1L);

        Pageable pageable = PageRequest.of(0, 20);
        Page<Patient> patientPage = new PageImpl<>(List.of(patient), pageable, 1);

        when(patientAccess.findActivePatientsInRange(any(), any(), any(Pageable.class))).thenReturn(patientPage);
        when(observationsRepository.findByPatientIdIn(any())).thenReturn(List.of(obs));

        Page<PatientObservationsDTO> result = observationService.getActivePatientsWithObservations(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).patient().getId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).observations()).hasSize(1);
    }

    @Test
    void getActivePatientsWithObservations_shouldReturnEmptyWhenNoPatients() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Patient> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(patientAccess.findActivePatientsInRange(any(), any(), any(Pageable.class))).thenReturn(emptyPage);

        Page<PatientObservationsDTO> result = observationService.getActivePatientsWithObservations(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), pageable);

        assertThat(result).isEmpty();
    }

    @Test
    void getActivePatientsWithObservations_shouldReturnPatientWithoutObservations() {
        Patient patient = new Patient() {
            @Override public Long getId() { return 1L; }
            @Override public String getName() { return "John"; }
            @Override public String getSurname() { return "Doe"; }
        };

        Pageable pageable = PageRequest.of(0, 20);
        Page<Patient> patientPage = new PageImpl<>(List.of(patient), pageable, 1);

        when(patientAccess.findActivePatientsInRange(any(), any(), any(Pageable.class))).thenReturn(patientPage);
        when(observationsRepository.findByPatientIdIn(any())).thenReturn(List.of());

        Page<PatientObservationsDTO> result = observationService.getActivePatientsWithObservations(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).observations()).isEmpty();
    }
}
