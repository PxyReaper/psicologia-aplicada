package com.tfg.proyectolibreria.psicologiaAplicada.patients.controller;

import com.tfg.proyectolibreria.psicologiaAplicada.patients.dto.PatientsRequestDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.service.PatientsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientsController {
    private final PatientsService patientsService;

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody PatientsRequestDTO patientsRequestDTO) throws URISyntaxException {
         patientsService.save(patientsRequestDTO);
         return ResponseEntity.created(new URI("/api/patients")).build();

    }

}
