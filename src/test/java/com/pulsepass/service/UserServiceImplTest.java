package com.pulsepass.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.domain.enums.Role;
import com.pulsepass.domain.model.User;
import com.pulsepass.domain.model.UserCredential;
import com.pulsepass.domain.model.UserRole;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.mapper.UserMapper;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.repository.UserRoleRepository;
import com.pulsepass.service.Impl.UserServiceImpl;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock private UserRepository userRepository;
    @Mock private UserCredentialRepository userCredentialRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthorizationService authorizationService;
    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(
                userRepository, userCredentialRepository, userRoleRepository,
                userMapper, passwordEncoder, authorizationService);
    }

    @Test
    void registersAnActiveUserWithProfileCredentialAndRole() {
        RegisterUserRequest request = request(LocalDate.now().minusYears(20));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("UnaFraseSeguraLarga2026!")).thenReturn("$2a$hash");

        service.register(request);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getActive()).isTrue();
        assertThat(saved.getValue().getProfile().getUser()).isSameAs(saved.getValue());

        ArgumentCaptor<UserCredential> credential = ArgumentCaptor.forClass(UserCredential.class);
        verify(userCredentialRepository).save(credential.capture());
        assertThat(credential.getValue().getPasswordHash()).isEqualTo("$2a$hash");

        ArgumentCaptor<UserRole> role = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository).save(role.capture());
        assertThat(role.getValue().getRole()).isEqualTo(Role.USER);
    }

    @Test
    void rejectsADuplicateUsername() {
        when(userRepository.existsByUsername("andrea")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request(LocalDate.now().minusYears(20))))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void rejectsADuplicateEmailIgnoringCase() {
        when(userRepository.existsByEmailIgnoreCase("andrea@email.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request(LocalDate.now().minusYears(20))))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void rejectsAFutureBirthDate() {
        assertThatThrownBy(() -> service.register(request(LocalDate.now().plusDays(1))))
                .isInstanceOf(BusinessRuleException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void rejectsReadingAnotherUsersData() {
        User user = new User();
        user.setId(99L);
        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        doThrow(new AccessDeniedException("No tiene permisos para acceder a este recurso"))
                .when(authorizationService).requireSelfOrAdmin(99L);

        assertThatThrownBy(() -> service.findByEmail("andrea@email.com"))
                .isInstanceOf(AccessDeniedException.class);
    }

    private RegisterUserRequest request(LocalDate birthDate) {
        return new RegisterUserRequest("andrea", "andrea@email.com", "UnaFraseSeguraLarga2026!",
                "Andrea", "Perez", "300", "Bogota", birthDate);
    }
}
