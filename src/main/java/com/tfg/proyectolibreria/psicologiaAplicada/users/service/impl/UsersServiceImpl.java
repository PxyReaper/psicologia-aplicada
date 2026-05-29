package com.tfg.proyectolibreria.psicologiaAplicada.users.service.impl;

import com.tfg.proyectolibreria.psicologiaAplicada.users.dto.RegisterRequest;
import com.tfg.proyectolibreria.psicologiaAplicada.users.dto.UpdateUserRequest;
import com.tfg.proyectolibreria.psicologiaAplicada.users.dto.UserResponse;
import com.tfg.proyectolibreria.psicologiaAplicada.users.UserDetailsImpl;
import com.tfg.proyectolibreria.psicologiaAplicada.users.UsersEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.users.email.EmailService;
import com.tfg.proyectolibreria.psicologiaAplicada.users.event.UserCreatedEvent;
import com.tfg.proyectolibreria.psicologiaAplicada.users.repository.UsersRepository;
import com.tfg.proyectolibreria.psicologiaAplicada.users.service.UsersService;
import com.tfg.proyectolibreria.psicologiaAplicada.web.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailService emailService;

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String DIGITS = "0123456789";
    private static final String PASSWORD_CHARS = CHAR_LOWER + CHAR_UPPER + DIGITS;
    private static final int PASSWORD_LENGTH = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UsersEntity entity = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return new UserDetailsImpl(entity);
    }

    @Override
    public void register(RegisterRequest request) {
        String rawPassword = generatePassword();
        UsersEntity entity = new UsersEntity(
                null,
                request.email(),
                request.username(),
                passwordEncoder.encode(rawPassword),
                request.name(),
                request.surname(),
                request.role(),
                true,
                null,
                0
        );
        usersRepository.save(entity);
        eventPublisher.publishEvent(new UserCreatedEvent(
                entity.getEmail(), entity.getName(), entity.getSurname(), rawPassword
        ));
    }

    @Override
    public List<UserResponse> findAll() {
        return usersRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserResponse findById(Long id) {
        UsersEntity entity = usersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return toResponse(entity);
    }

    @Override
    public UserResponse update(Long id, UpdateUserRequest request) {
        UsersEntity entity = usersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        UsersEntity updated = new UsersEntity(
                id,
                request.email() != null ? request.email() : entity.getEmail(),
                request.username() != null ? request.username() : entity.getUsername(),
                entity.getPassword(),
                request.name() != null ? request.name() : entity.getName(),
                request.surname() != null ? request.surname() : entity.getSurname(),
                request.role() != null ? request.role() : entity.getRole(),
                request.enabled() != null ? request.enabled() : entity.isEnabled(),
                entity.getCreatedAt(),
                entity.getTokenVersion()
        );

        return toResponse(usersRepository.save(updated));
    }

    @Override
    public void resetPassword(Long id) {
        UsersEntity entity = usersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String rawPassword = generatePassword();

        UsersEntity updated = new UsersEntity(
                id,
                entity.getEmail(),
                entity.getUsername(),
                passwordEncoder.encode(rawPassword),
                entity.getName(),
                entity.getSurname(),
                entity.getRole(),
                entity.isEnabled(),
                entity.getCreatedAt(),
                entity.getTokenVersion()
        );

        usersRepository.save(updated);

        emailService.sendPasswordEmail(
                entity.getEmail(),
                entity.getName() != null ? entity.getName() : entity.getUsername(),
                rawPassword
        );
    }

    @Override
    public void delete(Long id) {
        if (!usersRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        usersRepository.deleteById(id);
    }

    @Override
    public void incrementTokenVersion(String email) {
        UsersEntity entity = usersRepository.findByEmail(email)
                .orElse(null);
        if (entity != null) {
            UsersEntity updated = new UsersEntity(
                    entity.getId(),
                    entity.getEmail(),
                    entity.getUsername(),
                    entity.getPassword(),
                    entity.getName(),
                    entity.getSurname(),
                    entity.getRole(),
                    entity.isEnabled(),
                    entity.getCreatedAt(),
                    entity.getTokenVersion() + 1
            );
            usersRepository.save(updated);
        }
    }

    private UserResponse toResponse(UsersEntity entity) {
        return new UserResponse(
                entity.getId(),
                entity.getEmail(),
                entity.getUsername(),
                entity.getName(),
                entity.getSurname(),
                entity.getRole(),
                entity.isEnabled(),
                entity.getCreatedAt()
        );
    }

    private String generatePassword() {
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
