package com.tfg.proyectolibreria.psicologiaAplicada.auth.controller;

import com.tfg.proyectolibreria.psicologiaAplicada.auth.dto.AuthRequest;
import com.tfg.proyectolibreria.psicologiaAplicada.auth.dto.AuthResponse;
import com.tfg.proyectolibreria.psicologiaAplicada.users.dto.RegisterRequest;
import com.tfg.proyectolibreria.psicologiaAplicada.users.enums.UserRole;
import com.tfg.proyectolibreria.psicologiaAplicada.users.service.UsersService;
import com.tfg.proyectolibreria.psicologiaAplicada.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private UsersService usersService;

    @Test
    void login_shouldReturnToken() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken("test@test.com", "password");
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtils.generateToken(auth)).thenReturn("jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "test@test.com", "password": "password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void register_shouldReturnCreated() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@test.com",
                                    "username": "testuser",
                                    "name": "Test",
                                    "surname": "User",
                                    "role": "PSYCHOLOGIST"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(usersService).register(any(RegisterRequest.class));
    }
}
