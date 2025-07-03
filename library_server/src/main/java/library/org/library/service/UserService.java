package org.library.service;

import java.util.List;
import java.util.Optional;

import org.library.entity.User;
import org.library.entity.enums.Role;

public interface UserService {
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
    void createUser(User user);
    void updateUser(Long id, User updatedUser);  // Update User Method
    boolean deleteUser(Long id);
    List<String> getUsernames(String query);
    Optional<User> findByUsername(String username);  // Fetch User by username
    Long getUserIdByUsername(String username);
    boolean isAdmin(String username);
    boolean existsById(Long userId);
    Role getUserRoleByUsername(String username);
}