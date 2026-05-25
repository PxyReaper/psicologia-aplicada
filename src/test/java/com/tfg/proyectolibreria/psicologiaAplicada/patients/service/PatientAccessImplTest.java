package com.tfg.proyectolibreria.psicologiaAplicada.patients.service;

import com.tfg.proyectolibreria.psicologiaAplicada.kernel.Patient;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.PatientsEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.enums.Genre;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.repository.PatientsRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientAccessImplTest {

    @Mock
    private PatientsRepository patientsRepository;

    @InjectMocks
    private PatientAccessImpl patientAccess;

    @Test
    void findById_shouldReturnPatientWhenExists() {
        PatientsEntity entity = new PatientsEntity(1L, "John", "Doe",
                LocalDate.of(2024, 1, 1), null, LocalDate.of(1990, 1, 1),
                "123456789", Genre.MASCULINO);
        when(patientsRepository.findById(1L)).thenReturn(Optional.of(entity));

        Optional<Patient> result = patientAccess.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("John");
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        when(patientsRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Patient> result = patientAccess.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void findActivePatientsInRange_shouldReturnActivePatients() {
        PatientsEntity entity = new PatientsEntity(1L, "Jane", "Doe",
                LocalDate.of(2024, 3, 1), null, LocalDate.of(1992, 5, 10),
                "987654321", Genre.FEMENINO);
        Pageable pageable = PageRequest.of(0, 20);
        Page<PatientsEntity> page = new PageImpl<>(List.of(entity), pageable, 1);
        when(patientsRepository.findActiveInRange(any(), any(), any(Pageable.class))).thenReturn(page);

        Page<Patient> result = patientAccess.findActivePatientsInRange(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Jane");
    }

    @Test
    void findByIdIn_shouldReturnPatients() {
        PatientsEntity entity = new PatientsEntity(1L, "John", "Doe",
                LocalDate.of(2024, 1, 1), null, LocalDate.of(1990, 1, 1),
                "123456789", Genre.MASCULINO);
        when(patientsRepository.findByIdIn(List.of(1L))).thenReturn(List.of(entity));

        List<Patient> result = patientAccess.findByIdIn(List.of(1L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John");
    }
}
