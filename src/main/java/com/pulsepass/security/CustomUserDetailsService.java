package com.pulsepass.security;

import com.pulsepass.domain.model.UserCredential;
import com.pulsepass.repository.UserCredentialRepository;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserCredentialRepository credentialRepository;

    public CustomUserDetailsService(UserCredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) {
        UserCredential credential = credentialRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        return toPrincipal(credential);
    }

    @Transactional(readOnly = true)
    public SecurityPrincipal loadByUserId(Long userId) {
        UserCredential credential = credentialRepository.findWithUserByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        SecurityPrincipal principal = toPrincipal(credential);
        if (!principal.isEnabled()) {
            throw new UsernameNotFoundException("User not found");
        }
        return principal;
    }

    private SecurityPrincipal toPrincipal(UserCredential credential) {
        var user = credential.getUser();
        var authorities = credential.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toUnmodifiableSet());
        return new SecurityPrincipal(user.getId(), user.getUsername(), user.getEmail(), credential.getPasswordHash(),
                Boolean.TRUE.equals(user.getActive()), authorities);
    }
}
