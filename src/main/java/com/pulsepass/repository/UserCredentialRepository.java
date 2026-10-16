package com.pulsepass.repository;

import com.pulsepass.domain.model.UserCredential;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
    @Query("SELECT c FROM UserCredential c JOIN FETCH c.user u WHERE LOWER(u.email) = LOWER(:identifier) OR u.username = :identifier")
    Optional<UserCredential> findByIdentifier(@Param("identifier") String identifier);

    @Query("SELECT c FROM UserCredential c JOIN FETCH c.user WHERE c.user.id = :userId")
    Optional<UserCredential> findWithUserByUserId(@Param("userId") Long userId);
}
