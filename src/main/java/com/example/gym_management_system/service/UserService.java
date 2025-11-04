package com.example.gym_management_system.service;

import com.example.gym_management_system.entity.User;
import com.example.gym_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // Create a new user
    public User createUser(User user) {
        // Creating new user
        
        // Validate username uniqueness
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        
        // Validate email uniqueness
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set default values
        if (user.getStatus() == null) {
            user.setStatus(User.UserStatus.ACTIVE);
        }
        
        User savedUser = userRepository.save(user);
        return savedUser;
    }

    // Get all users with pagination
    public Page<User> getAllUsers(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return userRepository.findAll(pageable);
    }

    // Get user by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Get user by username
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Get user by email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Update user
    public User updateUser(Long id, User userDetails) {
        // Updating user
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        
        // Validate username uniqueness (excluding current user)
        if (!existingUser.getUsername().equals(userDetails.getUsername()) &&
            userRepository.existsByUsernameAndIdNot(userDetails.getUsername(), id)) {
            throw new RuntimeException("Username already exists: " + userDetails.getUsername());
        }
        
        // Validate email uniqueness (excluding current user)
        if (!existingUser.getEmail().equals(userDetails.getEmail()) &&
            userRepository.existsByEmailAndIdNot(userDetails.getEmail(), id)) {
            throw new RuntimeException("Email already exists: " + userDetails.getEmail());
        }
        
        // Update fields
        existingUser.setUsername(userDetails.getUsername());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());
        existingUser.setPhone(userDetails.getPhone());
        existingUser.setRole(userDetails.getRole());
        existingUser.setStatus(userDetails.getStatus());
        existingUser.setProfileImageUrl(userDetails.getProfileImageUrl());
        existingUser.setBio(userDetails.getBio());
        existingUser.setEnabled(userDetails.getEnabled());
        
        // Only update password if provided
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        
        User updatedUser = userRepository.save(existingUser);
        // User updated successfully
        return updatedUser;
    }

    // Delete user
    public void deleteUser(Long id) {
        // Deleting user
        
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with ID: " + id);
        }
        
        userRepository.deleteById(id);
        // User deleted successfully
    }

    // Search users
    public Page<User> searchUsers(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.searchUsers(searchTerm, pageable);
    }

    // Get users by role
    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    // Get users by role with pagination
    public Page<User> getUsersByRole(User.Role role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findByRole(role, pageable);
    }

    // Get users by status
    public List<User> getUsersByStatus(User.UserStatus status) {
        return userRepository.findByStatus(status);
    }

    // Get active users
    public List<User> getActiveUsers() {
        return userRepository.findByStatusAndEnabled(User.UserStatus.ACTIVE, true);
    }

    // Get active trainers
    public List<User> getActiveTrainers() {
        return userRepository.findActiveTrainers();
    }

    // Get active staff
    public List<User> getActiveStaff() {
        return userRepository.findActiveStaff();
    }

    // Update user status
    public User updateUserStatus(Long userId, User.UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.setStatus(status);
        return userRepository.save(user);
    }

    // Update user role
    public User updateUserRole(Long userId, User.Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.setRole(role);
        return userRepository.save(user);
    }

    // Update last login time
    public void updateLastLogin(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    // Change password
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Password changed successfully
    }

    // Reset password
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Password reset successfully
    }

    // Verify email
    public void verifyEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.setEmailVerified(true);
        if (user.getStatus() == User.UserStatus.PENDING_VERIFICATION) {
            user.setStatus(User.UserStatus.ACTIVE);
        }
        
        userRepository.save(user);
        // Email verified
    }

    // Get user statistics
    public UserStats getUserStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(User.UserStatus.ACTIVE);
        long adminUsers = userRepository.countByRole(User.Role.ADMIN);
        long managerUsers = userRepository.countByRole(User.Role.MANAGER);
        long trainerUsers = userRepository.countByRole(User.Role.TRAINER);
        long memberUsers = userRepository.countByRole(User.Role.MEMBER);
        long receptionistUsers = userRepository.countByRole(User.Role.RECEPTIONIST);
        
        return new UserStats(totalUsers, activeUsers, adminUsers, managerUsers, 
                           trainerUsers, memberUsers, receptionistUsers);
    }

    // Get total users count
    public long getTotalUsers() {
        return userRepository.count();
    }

    // Get user count by role
    public long getUserCountByRole(User.Role role) {
        return userRepository.countByRole(role);
    }

    // Inner class for user statistics
    public static class UserStats {
        private final long totalUsers;
        private final long activeUsers;
        private final long adminUsers;
        private final long managerUsers;
        private final long trainerUsers;
        private final long memberUsers;
        private final long receptionistUsers;

        public UserStats(long totalUsers, long activeUsers, long adminUsers, long managerUsers,
                        long trainerUsers, long memberUsers, long receptionistUsers) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.adminUsers = adminUsers;
            this.managerUsers = managerUsers;
            this.trainerUsers = trainerUsers;
            this.memberUsers = memberUsers;
            this.receptionistUsers = receptionistUsers;
        }

        // Getters
        public long getTotalUsers() { return totalUsers; }
        public long getActiveUsers() { return activeUsers; }
        public long getAdminUsers() { return adminUsers; }
        public long getManagerUsers() { return managerUsers; }
        public long getTrainerUsers() { return trainerUsers; }
        public long getMemberUsers() { return memberUsers; }
        public long getReceptionistUsers() { return receptionistUsers; }
    }
}
