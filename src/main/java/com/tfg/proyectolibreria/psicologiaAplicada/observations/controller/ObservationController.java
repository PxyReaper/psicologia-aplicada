package com.tfg.proyectolibreria.psicologiaAplicada.observations.controller;

import com.tfg.proyectolibreria.psicologiaAplicada.observations.dto.PatientObservationsDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.service.ObservationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/observations")
public class ObservationController {

    private final ObservationService observationService;

    public ObservationController(ObservationService observationService) {
        this.observationService = observationService;
    }

    @GetMapping("/patients")
    public Page<PatientObservationsDTO> getActivePatientsWithObservations(
            @RequestParam LocalDate rangeStart,
            @RequestParam LocalDate rangeEnd,
            @PageableDefault(size = 20) Pageable pageable) {
        return observationService.getActivePatientsWithObservations(rangeStart, rangeEnd, pageable);
    }
}
