package com.pulsepass.repository;

import com.pulsepass.domain.enums.Role;
import com.pulsepass.domain.model.UserRole;
import com.pulsepass.domain.model.UserRoleId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    @Query("SELECT ur.role FROM UserRole ur WHERE ur.userId = :userId")
    List<Role> findRolesByUserId(@Param("userId") Long userId);

    void deleteByUserId(Long userId);
}
