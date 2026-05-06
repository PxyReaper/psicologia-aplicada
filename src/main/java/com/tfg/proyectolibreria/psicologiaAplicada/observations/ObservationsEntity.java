package com.tfg.proyectolibreria.psicologiaAplicada.observations;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Table(name = "observations")
@Entity
@NoArgsConstructor
public class ObservationsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String observation;

    @Column(name = "id_patient")
    private Long patientId;

    public ObservationsEntity(Long id, String observation, Long patientId) {
        this.id = id;
        this.observation = observation;
        this.patientId = patientId;
    }

    public Long getId() {
        return id;
    }

    public String getObservation() {
        return observation;
    }

    public Long getPatientId() {
        return patientId;
    }
}
