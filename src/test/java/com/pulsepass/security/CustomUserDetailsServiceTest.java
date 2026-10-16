package com.pulsepass.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.pulsepass.domain.enums.Role;
import com.pulsepass.domain.model.User;
import com.pulsepass.domain.model.UserCredential;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.repository.UserRoleRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserCredentialRepository userCredentialRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @InjectMocks private CustomUserDetailsService service;

    private User user() {
        User user = new User();
        user.setId(7L);
        user.setUsername("andrea");
        user.setEmail("andrea@email.com");
        user.setActive(true);
        return user;
    }

    @Test
    void loadsByUsername() {
        when(userRepository.findByUsername("andrea")).thenReturn(Optional.of(user()));
        when(userCredentialRepository.findByUserId(7L))
                .thenReturn(Optional.of(new UserCredential(7L, "$2a$hash")));
        when(userRoleRepository.findRolesByUserId(7L)).thenReturn(List.of(Role.USER, Role.STAFF));

        SecurityPrincipal principal = (SecurityPrincipal) service.loadUserByUsername("andrea");

        assertThat(principal.getId()).isEqualTo(7L);
        assertThat(principal.getUsername()).isEqualTo("andrea");
        assertThat(principal.getPassword()).isEqualTo("$2a$hash");
        assertThat(principal.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_STAFF");
    }

    @Test
    void fallsBackToEmailWhenUsernameDoesNotMatch() {
        when(userRepository.findByUsername("andrea@email.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user()));
        when(userCredentialRepository.findByUserId(7L))
                .thenReturn(Optional.of(new UserCredential(7L, "$2a$hash")));
        when(userRoleRepository.findRolesByUserId(7L)).thenReturn(List.of(Role.USER));

        SecurityPrincipal principal = (SecurityPrincipal) service.loadUserByUsername("andrea@email.com");

        assertThat(principal.getEmail()).isEqualTo("andrea@email.com");
    }

    @Test
    void loadsById() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user()));
        when(userCredentialRepository.findByUserId(7L))
                .thenReturn(Optional.of(new UserCredential(7L, "$2a$hash")));
        when(userRoleRepository.findRolesByUserId(7L)).thenReturn(List.of(Role.ADMIN));

        SecurityPrincipal principal = service.loadUserById(7L);

        assertThat(principal.getId()).isEqualTo(7L);
        assertThat(principal.isEnabled()).isTrue();
    }

    @Test
    void rejectsAnUnknownIdentifier() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void rejectsAUserWithoutCredentials() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user()));
        when(userCredentialRepository.findByUserId(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserById(7L))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
