package com.tfg.proyectolibreria.psicologiaAplicada.session.controller;

import com.tfg.proyectolibreria.psicologiaAplicada.session.dto.SessionRequestDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;

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
}
