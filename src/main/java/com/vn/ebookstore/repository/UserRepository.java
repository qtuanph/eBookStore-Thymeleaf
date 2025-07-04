package com.vn.ebookstore.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vn.ebookstore.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    long countByDeletedAtIsNull();

    Page<User> findByFirstNameContainingOrLastNameContainingOrEmailContainingOrUsernameContaining(
        String firstName, String lastName, String email, String username, Pageable pageable);
    
    Page<User> findByRolesName(String role, Pageable pageable);
}