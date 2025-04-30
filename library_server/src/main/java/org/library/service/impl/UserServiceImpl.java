package org.library.service.impl;

import org.library.entity.User;
import org.library.entity.enums.Role;
import org.library.repository.UserRepository;
import org.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public void createUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void updateUser(Long id, User updatedUser) {
        Optional<User> existingUserOptional = userRepository.findById(id);

        if (existingUserOptional.isPresent()) {
            User existingUser = existingUserOptional.get();

            // Only update allowed fields
            existingUser.setFullName(updatedUser.getFullName());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setSex(updatedUser.getSex());
            existingUser.setMobileNumber(updatedUser.getMobileNumber());
            existingUser.setCountryCode(updatedUser.getCountryCode());

            // Avoid changing sensitive fields like username or password here

            // Save the updated user in the repository (DB)
            userRepository.save(existingUser); // ✅ Persist changes to DB
        } else {
            throw new RuntimeException("User not found with ID: " + id); // Optionally handle this exception
        }
    }

    @Override
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<String> getUsernames(String query) {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(User::getUsername)
                .filter(username -> username.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Long getUserIdByUsername(String username) {
        Optional<User> user = findByUsername(username);
        return user.map(User::getId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public boolean isAdmin(String username) {
        return userRepository.findByUsername(username)
                .map(user -> "ADMIN".equalsIgnoreCase(String.valueOf(user.getRole())))
                .orElse(false);
    }

    @Override
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public Role getUserRoleByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getRole();
    }
}
