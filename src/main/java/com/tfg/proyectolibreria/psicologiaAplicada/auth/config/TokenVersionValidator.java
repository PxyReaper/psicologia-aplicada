package com.tfg.proyectolibreria.psicologiaAplicada.auth.config;

import com.tfg.proyectolibreria.psicologiaAplicada.users.UserDetailsImpl;
import com.tfg.proyectolibreria.psicologiaAplicada.users.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

@Slf4j
public class TokenVersionValidator implements OAuth2TokenValidator<Jwt> {
    private final UsersService usersService;

    public TokenVersionValidator(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        try {
            Object tokenVersionClaim = token.getClaim("tokenVersion");
            if (!(tokenVersionClaim instanceof Number tokenVersionNum)) {
                log.warn("Token has no tokenVersion claim for subject: {}", token.getSubject());
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("token_version_missing",
                                "Token has no tokenVersion claim", ""));
            }
            int tokenVersion = tokenVersionNum.intValue();
            String email = token.getSubject();

            UserDetails details = usersService.loadUserByUsername(email);

            if (details instanceof UserDetailsImpl impl) {
                if (impl.getTokenVersion() == tokenVersion) {
                    return OAuth2TokenValidatorResult.success();
                }
                log.warn("Token version mismatch for {}: token={}, db={}", email, tokenVersion, impl.getTokenVersion());
            }
        } catch (UsernameNotFoundException e) {
            log.warn("Token validation failed: user not found: {}", token.getSubject());
        }
        return OAuth2TokenValidatorResult.failure(
                new OAuth2Error("token_version_mismatch",
                        "Token version does not match current user version", ""));
    }
}
