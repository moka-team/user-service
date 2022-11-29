package com.mokaform.userservice.repository;


import com.mokaform.userservice.domain.Role;
import com.mokaform.userservice.domain.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
