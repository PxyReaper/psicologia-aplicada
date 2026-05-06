package com.tfg.proyectolibreria.psicologiaAplicada.patients.repository;

import com.tfg.proyectolibreria.psicologiaAplicada.patients.PatientsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PatientsRepository extends JpaRepository<PatientsEntity,Long> {
    @Query("SELECT p FROM PatientsEntity p WHERE p.startDate <= :rangeEnd AND COALESCE(p.endDate, :rangeEnd) >= :rangeStart")
    List<PatientsEntity> findActiveInRange(LocalDate rangeStart, LocalDate rangeEnd);
}

