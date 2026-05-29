package com.tfg.proyectolibreria.psicologiaAplicada.users.service.impl;

import com.tfg.proyectolibreria.psicologiaAplicada.users.UsersEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.users.dto.RegisterRequest;
import com.tfg.proyectolibreria.psicologiaAplicada.users.dto.UpdateUserRequest;
import com.tfg.proyectolibreria.psicologiaAplicada.users.dto.UserResponse;
import com.tfg.proyectolibreria.psicologiaAplicada.users.email.EmailService;
import com.tfg.proyectolibreria.psicologiaAplicada.users.enums.UserRole;
import com.tfg.proyectolibreria.psicologiaAplicada.users.event.UserCreatedEvent;
import com.tfg.proyectolibreria.psicologiaAplicada.users.repository.UsersRepository;
import com.tfg.proyectolibreria.psicologiaAplicada.web.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UsersServiceImpl usersService;

    @Test
    void register_shouldEncodePasswordAndPublishEvent() {
        RegisterRequest request = new RegisterRequest("test@test.com", "testuser", null, "Test", "User", UserRole.PSYCHOLOGIST);

        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("encoded-password");

        UsersEntity savedEntity = new UsersEntity(1L, "test@test.com", "testuser", "encoded-password", "Test", "User", UserRole.PSYCHOLOGIST, true, null, 0);
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
        UsersEntity entity = new UsersEntity(1L, "test@test.com", "testuser", "encoded", "Test", "User", UserRole.ADMIN, true, null, 0);
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

    @Test
    void findAll_shouldReturnAllUsers() {
        UsersEntity entity1 = new UsersEntity(1L, "admin@test.com", "admin", "encoded", "Admin", "User", UserRole.ADMIN, true, LocalDateTime.now(), 0);
        UsersEntity entity2 = new UsersEntity(2L, "psych@test.com", "psych", "encoded", "Psych", "User", UserRole.PSYCHOLOGIST, true, LocalDateTime.now(), 0);

        when(usersRepository.findAll()).thenReturn(List.of(entity1, entity2));

        List<UserResponse> result = usersService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).email()).isEqualTo("admin@test.com");
        assertThat(result.get(1).email()).isEqualTo("psych@test.com");
    }

    @Test
    void findById_shouldReturnUser_whenFound() {
        LocalDateTime now = LocalDateTime.now();
        UsersEntity entity = new UsersEntity(1L, "test@test.com", "testuser", "encoded", "Test", "User", UserRole.ADMIN, true, now, 0);

        when(usersRepository.findById(1L)).thenReturn(Optional.of(entity));

        UserResponse result = usersService.findById(1L);

        assertThat(result.email()).isEqualTo("test@test.com");
        assertThat(result.role()).isEqualTo(UserRole.ADMIN);
        assertThat(result.createdAt()).isEqualTo(now);
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(usersRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usersService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_shouldUpdateFields() {
        LocalDateTime now = LocalDateTime.now();
        UsersEntity existing = new UsersEntity(1L, "old@test.com", "olduser", "encoded-password", "Old", "Name", UserRole.PSYCHOLOGIST, true, now, 0);
        UpdateUserRequest request = new UpdateUserRequest("new@test.com", "newuser", "New", "Name", UserRole.ADMIN, true);

        when(usersRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(usersRepository.save(any(UsersEntity.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse result = usersService.update(1L, request);

        assertThat(result.email()).isEqualTo("new@test.com");
        assertThat(result.username()).isEqualTo("newuser");
        assertThat(result.name()).isEqualTo("New");
        assertThat(result.role()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void update_shouldPreservePasswordAndKeepNullFields() {
        LocalDateTime now = LocalDateTime.now();
        UsersEntity existing = new UsersEntity(1L, "test@test.com", "testuser", "original-encoded", "Test", "User", UserRole.PSYCHOLOGIST, true, now, 0);
        UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, UserRole.ADMIN, null);

        when(usersRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(usersRepository.save(any(UsersEntity.class))).thenAnswer(i -> i.getArgument(0));

        usersService.update(1L, request);

        ArgumentCaptor<UsersEntity> captor = ArgumentCaptor.forClass(UsersEntity.class);
        verify(usersRepository).save(captor.capture());
        UsersEntity saved = captor.getValue();

        assertThat(saved.getPassword()).isEqualTo("original-encoded");
        assertThat(saved.getCreatedAt()).isEqualTo(now);
        assertThat(saved.getEmail()).isEqualTo("test@test.com");
        assertThat(saved.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void update_shouldThrow_whenNotFound() {
        when(usersRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usersService.update(99L, new UpdateUserRequest(null, null, null, null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_shouldDelete_whenFound() {
        when(usersRepository.existsById(1L)).thenReturn(true);

        usersService.delete(1L);

        verify(usersRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(usersRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> usersService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void resetPassword_shouldGenerateEncodeSaveAndSendEmail() {
        LocalDateTime now = LocalDateTime.now();
        UsersEntity existing = new UsersEntity(1L, "test@test.com", "testuser", "old-encoded", "Test", "User", UserRole.PSYCHOLOGIST, true, now, 0);

        when(usersRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("new-encoded");
        when(usersRepository.save(any(UsersEntity.class))).thenAnswer(i -> i.getArgument(0));

        usersService.resetPassword(1L);

        ArgumentCaptor<UsersEntity> captor = ArgumentCaptor.forClass(UsersEntity.class);
        verify(usersRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("new-encoded");

        verify(emailService).sendPasswordEmail(eq("test@test.com"), eq("Test"), any(String.class));
    }

    @Test
    void resetPassword_shouldThrow_whenNotFound() {
        when(usersRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usersService.resetPassword(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void incrementTokenVersion_shouldIncrementVersion() {
        LocalDateTime now = LocalDateTime.now();
        UsersEntity existing = new UsersEntity(1L, "test@test.com", "testuser", "encoded", "Test", "User", UserRole.PSYCHOLOGIST, true, now, 0);

        when(usersRepository.findByEmail("test@test.com")).thenReturn(Optional.of(existing));
        when(usersRepository.save(any(UsersEntity.class))).thenAnswer(i -> i.getArgument(0));

        usersService.incrementTokenVersion("test@test.com");

        ArgumentCaptor<UsersEntity> captor = ArgumentCaptor.forClass(UsersEntity.class);
        verify(usersRepository).save(captor.capture());
        assertThat(captor.getValue().getTokenVersion()).isEqualTo(1);
    }

    @Test
    void incrementTokenVersion_shouldDoNothing_whenUserNotFound() {
        when(usersRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        usersService.incrementTokenVersion("missing@test.com");

        verify(usersRepository, org.mockito.Mockito.never()).save(any());
    }
}
