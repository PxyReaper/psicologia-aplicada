package com.tfg.proyectolibreria.psicologiaAplicada.patients.controller;

import com.tfg.proyectolibreria.psicologiaAplicada.patients.dto.PatientsRequestDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.service.PatientsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody PatientsRequestDTO patientsRequestDTO) {
        patientsService.update(id, patientsRequestDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/discharge")
    public ResponseEntity<Void> discharge(@PathVariable Long id) {
        patientsService.discharge(id);
        return ResponseEntity.ok().build();
    }
}
