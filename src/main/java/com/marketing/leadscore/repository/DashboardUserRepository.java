package com.marketing.leadscore.repository;

import com.marketing.leadscore.entity.DashboardUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DashboardUserRepository extends JpaRepository<DashboardUser, Long> {

    Optional<DashboardUser> findByUsername(String username);

    boolean existsByUsername(String username);

    List<DashboardUser> findAllByRoleOrderByCreatedAtAsc(String role);
}
