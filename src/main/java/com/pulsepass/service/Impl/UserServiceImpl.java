package com.pulsepass.service.Impl;

import com.pulsepass.domain.enums.Role;
import com.pulsepass.domain.model.User;
import com.pulsepass.domain.model.UserCredential;
import com.pulsepass.domain.model.UserProfile;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.UserMapper;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.security.AuthorizationService;
import com.pulsepass.service.UserService;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserCredentialRepository credentialRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthorizationService authorizationService;

    public UserServiceImpl(UserRepository userRepository, UserCredentialRepository credentialRepository,
                           UserMapper userMapper, PasswordEncoder passwordEncoder,
                           AuthorizationService authorizationService) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessRuleException("Password must not exceed 72 bytes");
        }
        // BR-USER-001: username único
        if (userRepository.existsByUsername(request.username())
                || userRepository.existsByEmailIgnoreCase(request.username())) {
            throw new DuplicateResourceException("Username already exists: " + request.username());
        }

        // BR-USER-002: email único (case-insensitive)
        if (userRepository.existsByEmailIgnoreCase(request.email())
                || userRepository.existsByUsername(request.email())) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }

        // BR-USER-005: birthDate no futura
        if (request.birthDate() != null && request.birthDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Birth date cannot be in the future");
        }

        // BR-USER-003 + BR-USER-004: crear User + UserProfile en la misma transacción
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setActive(true);

        UserProfile profile = new UserProfile();
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setPhone(request.phone());
        profile.setCity(request.city());
        profile.setBirthDate(request.birthDate());
        profile.setUser(user);

        user.setProfile(profile);

        User saved = userRepository.save(user);
        UserCredential credential = new UserCredential();
        credential.setUser(saved);
        credential.setPasswordHash(passwordEncoder.encode(request.password()));
        credential.setRoles(new HashSet<>(java.util.Set.of(Role.USER)));
        credentialRepository.save(credential);
        return userMapper.toResponse(saved);
    }

    @Override
    public UserResponse findByEmail(String email) {
        authorizationService.requireOwnEmailOrAdmin(email);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        authorizationService.requireSelfOrAdmin(user.getId());
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse findByUsername(String username) {
        authorizationService.requireOwnUsernameOrAdmin(username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        authorizationService.requireSelfOrAdmin(user.getId());
        return userMapper.toResponse(user);
    }
}