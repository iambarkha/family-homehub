package com.aab.homehub.auth;

import com.aab.homehub.auth.entity.Role;
import com.aab.homehub.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> , JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Modifying query: update without loading entity
    @Modifying
    @Transactional
    @Query("update User u set u.enabled = false where u.id = :id")
    int deactivateById(@Param("id") UUID id);

    // Pageable example (derived + pagination)
    Page<User> findByRole(Role role, Pageable pageable);


  /*  @Transactional
    int deleteByIdAndFamilyGroup_Id(String userId, String familyId);*/
}
