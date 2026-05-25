package com.tfg.proyectolibreria.psicologiaAplicada.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        jwtUtils.getClass().getDeclaredMethods(); // ensure class loaded

        setField(jwtUtils, "secret", "my-secret-key-for-testing-purposes-1234567890");
        setField(jwtUtils, "issuer", "test-issuer");
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user@test.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_PSYCHOLOGIST"))
        );

        String token = jwtUtils.generateToken(auth);

        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void getSubject_shouldReturnEmailFromToken() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user@test.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_PSYCHOLOGIST"))
        );

        String token = jwtUtils.generateToken(auth);
        String subject = jwtUtils.getSubject(token);

        assertThat(subject).isEqualTo("user@test.com");
    }

    @Test
    void getSubject_shouldThrowForInvalidToken() {
        assertThatThrownBy(() -> jwtUtils.getSubject("invalid-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid token");
    }
}
