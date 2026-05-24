package com.tfg.proyectolibreria.psicologiaAplicada.auth.controller;

import com.tfg.proyectolibreria.psicologiaAplicada.auth.dto.AuthRequest;
import com.tfg.proyectolibreria.psicologiaAplicada.auth.dto.AuthResponse;
import com.tfg.proyectolibreria.psicologiaAplicada.users.dto.RegisterRequest;
import com.tfg.proyectolibreria.psicologiaAplicada.users.UsersEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.users.service.UsersService;
import com.tfg.proyectolibreria.psicologiaAplicada.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UsersService usersService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        String token = jwtUtils.generateToken(auth);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<UsersEntity> register(@RequestBody RegisterRequest request) {
        UsersEntity user = usersService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
