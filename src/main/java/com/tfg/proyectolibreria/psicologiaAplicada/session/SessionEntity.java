package com.tfg.proyectolibreria.psicologiaAplicada.session;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "session")
@NoArgsConstructor
public class SessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime sessionDate;
    private LocalDateTime sessionDateEnd;
    private String observation;
    private String observationSummary;
    private boolean pay;

    @Column(name = "id_patient")
    private Long patientId;

    public SessionEntity(Long id, LocalDateTime sessionDate, LocalDateTime sessionDateEnd, String observation, String observationSummary, boolean pay, Long patientId) {
        this.id = id;
        this.sessionDate = sessionDate;
        this.sessionDateEnd = sessionDateEnd;
        this.observation = observation;
        this.observationSummary = observationSummary;
        this.pay = pay;
        this.patientId = patientId;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getSessionDate() {
        return sessionDate;
    }

    public LocalDateTime getSessionDateEnd() {
        return sessionDateEnd;
    }

    public String getObservation() {
        return observation;
    }

    public String getObservationSummary() {
        return observationSummary;
    }

    public boolean isPay() {
        return pay;
    }

    public Long getPatientId() {
        return patientId;
    }
}
