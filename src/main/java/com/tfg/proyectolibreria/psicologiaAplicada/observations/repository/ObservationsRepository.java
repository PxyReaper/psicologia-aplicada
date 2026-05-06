package com.tfg.proyectolibreria.psicologiaAplicada.observations.repository;

import com.tfg.proyectolibreria.psicologiaAplicada.observations.ObservationsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObservationsRepository extends JpaRepository<ObservationsEntity,Long> {
    List<ObservationsEntity> findByPatientIdIn(List<Long> patientIds);
}
