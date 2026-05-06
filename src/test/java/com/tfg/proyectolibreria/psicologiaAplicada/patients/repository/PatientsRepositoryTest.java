package com.tfg.proyectolibreria.psicologiaAplicada.patients.repository;

import com.tfg.proyectolibreria.psicologiaAplicada.patients.PatientsEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.enums.Genre;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(com.tfg.proyectolibreria.psicologiaAplicada.config.TestJpaConfig.class)
class PatientsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PatientsRepository patientsRepository;

    @Test
    void findActiveInRange_shouldReturnPatientsWithNullEndDate() {
        PatientsEntity activePatient = new PatientsEntity(null, "John", "Doe",
                LocalDate.of(2024, 1, 1), null, LocalDate.of(1990, 1, 1),
                "123456789", Genre.MASCULINO);
        entityManager.persist(activePatient);
        entityManager.flush();

        LocalDate rangeStart = LocalDate.of(2024, 1, 1);
        LocalDate rangeEnd = LocalDate.of(2024, 12, 31);

        List<PatientsEntity> result = patientsRepository.findActiveInRange(rangeStart, rangeEnd);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John");
    }

    @Test
    void findActiveInRange_shouldReturnPatientsWithinRange() {
        PatientsEntity patient = new PatientsEntity(null, "Jane", "Doe",
                LocalDate.of(2024, 3, 1), LocalDate.of(2024, 6, 1),
                LocalDate.of(1992, 5, 10), "987654321", Genre.FEMENINO);
        entityManager.persist(patient);
        entityManager.flush();

        LocalDate rangeStart = LocalDate.of(2024, 2, 1);
        LocalDate rangeEnd = LocalDate.of(2024, 7, 1);

        List<PatientsEntity> result = patientsRepository.findActiveInRange(rangeStart, rangeEnd);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Jane");
    }

    @Test
    void findActiveInRange_shouldNotReturnPatientsOutsideRange() {
        PatientsEntity patient = new PatientsEntity(null, "Bob", "Smith",
                LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31),
                LocalDate.of(1985, 3, 15), "555555555", Genre.MASCULINO);
        entityManager.persist(patient);
        entityManager.flush();

        LocalDate rangeStart = LocalDate.of(2024, 1, 1);
        LocalDate rangeEnd = LocalDate.of(2024, 12, 31);

        List<PatientsEntity> result = patientsRepository.findActiveInRange(rangeStart, rangeEnd);

        assertThat(result).isEmpty();
    }
}
