package com.pulsepass.repository;

import com.pulsepass.domain.model.UserCredential;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {

    Optional<UserCredential> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
