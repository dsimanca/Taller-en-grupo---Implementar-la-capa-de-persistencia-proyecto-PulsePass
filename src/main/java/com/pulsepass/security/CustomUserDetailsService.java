package com.pulsepass.security;

import com.pulsepass.domain.enums.Role;
import com.pulsepass.domain.model.User;
import com.pulsepass.domain.model.UserCredential;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.repository.UserRoleRepository;
import java.util.List;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final UserRoleRepository userRoleRepository;

    public CustomUserDetailsService(UserRepository userRepository,
                                    UserCredentialRepository userCredentialRepository,
                                    UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.userCredentialRepository = userCredentialRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) {
        User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmailIgnoreCase(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));
        return buildPrincipal(user);
    }

    public SecurityPrincipal loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
        return buildPrincipal(user);
    }

    private SecurityPrincipal buildPrincipal(User user) {
        UserCredential credential = userCredentialRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User has no credentials: " + user.getUsername()));
        List<Role> roles = userRoleRepository.findRolesByUserId(user.getId());
        return new SecurityPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                credential.getPasswordHash(),
                Boolean.TRUE.equals(user.getActive()),
                roles);
    }
}
