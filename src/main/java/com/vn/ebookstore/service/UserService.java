package com.vn.ebookstore.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vn.ebookstore.model.User;

public interface UserService {
    User createUser(User user);
    User updateUser(int id, User user);
    void deleteUser(int id);
    User getUserById(int id);
    List<User> getAllUsers();
    
    // Thêm các phương thức mới
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    User registerNewUser(User user);
    boolean checkEmailExists(String email);
    boolean checkUsernameExists(String username);
    boolean checkPassword(String rawPassword, String encodedPassword);
    User save(User user);
    User getUserByEmail(String email);
    boolean existsByUsername(String username);
    long getTotalUsers();
    Page<User> getAllUsersPage(Pageable pageable);
    Page<User> searchUsers(String keyword, Pageable pageable);
    Page<User> findByRole(String role, Pageable pageable);
    
    // Thêm phương thức mới cho roles
    User updateUserRoles(int userId, List<Integer> roleIds);
    User addRolesToUser(int userId, List<Integer> roleIds);
    User removeRolesFromUser(int userId, List<Integer> roleIds);
}