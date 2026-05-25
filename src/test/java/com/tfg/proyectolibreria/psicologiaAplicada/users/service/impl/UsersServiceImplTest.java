package com.tfg.proyectolibreria.psicologiaAplicada.users.service.impl;

import com.tfg.proyectolibreria.psicologiaAplicada.users.UsersEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.users.dto.RegisterRequest;
import com.tfg.proyectolibreria.psicologiaAplicada.users.enums.UserRole;
import com.tfg.proyectolibreria.psicologiaAplicada.users.event.UserCreatedEvent;
import com.tfg.proyectolibreria.psicologiaAplicada.users.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsersServiceImplTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UsersServiceImpl usersService;

    @Test
    void register_shouldEncodePasswordAndPublishEvent() {
        RegisterRequest request = new RegisterRequest("test@test.com", "testuser", null, "Test", "User", UserRole.PSYCHOLOGIST);

        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("encoded-password");

        UsersEntity savedEntity = new UsersEntity(1L, "test@test.com", "testuser", "encoded-password", "Test", "User", UserRole.PSYCHOLOGIST, true, null);
        when(usersRepository.save(any(UsersEntity.class))).thenReturn(savedEntity);

        usersService.register(request);

        ArgumentCaptor<UsersEntity> entityCaptor = ArgumentCaptor.forClass(UsersEntity.class);
        verify(usersRepository).save(entityCaptor.capture());
        UsersEntity captured = entityCaptor.getValue();
        assertThat(captured.getEmail()).isEqualTo("test@test.com");
        assertThat(captured.getPassword()).isEqualTo("encoded-password");

        verify(eventPublisher).publishEvent(any(UserCreatedEvent.class));
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetailsWhenFound() {
        UsersEntity entity = new UsersEntity(1L, "test@test.com", "testuser", "encoded", "Test", "User", UserRole.ADMIN, true, null);
        when(usersRepository.findByEmail("test@test.com")).thenReturn(Optional.of(entity));

        var details = usersService.loadUserByUsername("test@test.com");

        assertThat(details.getUsername()).isEqualTo("test@test.com");
        assertThat(details.getAuthorities()).isNotEmpty();
    }

    @Test
    void loadUserByUsername_shouldThrowWhenNotFound() {
        when(usersRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usersService.loadUserByUsername("missing@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("missing@test.com");
    }
}
