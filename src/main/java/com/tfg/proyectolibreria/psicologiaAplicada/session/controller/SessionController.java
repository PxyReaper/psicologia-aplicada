package com.tfg.proyectolibreria.psicologiaAplicada.session.controller;

import com.tfg.proyectolibreria.psicologiaAplicada.session.dto.SessionRequestDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.session.dto.SessionWithPatientDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/session")
public class SessionController {
    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody SessionRequestDTO requestDTO) throws URISyntaxException {
        sessionService.save(requestDTO);
        return ResponseEntity.created(new URI("/api/session")).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody SessionRequestDTO requestDTO) {
        sessionService.update(id, requestDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sessionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<SessionWithPatientDTO> findByDate(@RequestParam LocalDate date) {
        return sessionService.findByDate(date);
    }
}
