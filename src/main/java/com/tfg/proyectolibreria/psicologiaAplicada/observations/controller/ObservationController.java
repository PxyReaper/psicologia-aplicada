package com.tfg.proyectolibreria.psicologiaAplicada.observations.controller;

import com.tfg.proyectolibreria.psicologiaAplicada.observations.dto.PatientObservationsDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.service.ObservationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/observations")
public class ObservationController {

    private final ObservationService observationService;

    public ObservationController(ObservationService observationService) {
        this.observationService = observationService;
    }

    @GetMapping("/patients")
    public List<PatientObservationsDTO> getActivePatientsWithObservations(
            @RequestParam LocalDate rangeStart,
            @RequestParam LocalDate rangeEnd) {
        return observationService.getActivePatientsWithObservations(rangeStart, rangeEnd);
    }
}
