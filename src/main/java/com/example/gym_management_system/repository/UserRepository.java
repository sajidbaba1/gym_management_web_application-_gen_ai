package com.example.gym_management_system.repository;

import com.example.gym_management_system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find by username
    Optional<User> findByUsername(String username);

    // Find by email
    Optional<User> findByEmail(String email);

    // Find by username or email
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

    // Check if username exists
    boolean existsByUsername(String username);

    // Check if email exists
    boolean existsByEmail(String email);

    // Find by role
    List<User> findByRole(User.Role role);

    // Find by status
    List<User> findByStatus(User.UserStatus status);

    // Find active users
    List<User> findByStatusAndEnabled(User.UserStatus status, Boolean enabled);

    // Search users
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Find users by role with pagination
    Page<User> findByRole(User.Role role, Pageable pageable);

    // Count users by role
    long countByRole(User.Role role);

    // Count users by status
    long countByStatus(User.UserStatus status);

    // Find users created in date range
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);

    // Find users who haven't logged in recently
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :cutoffDate OR u.lastLoginAt IS NULL")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Find unverified users
    List<User> findByEmailVerifiedFalse();

    // Find trainers (for class assignment)
    @Query("SELECT u FROM User u WHERE u.role = 'TRAINER' AND u.status = 'ACTIVE' AND u.enabled = true")
    List<User> findActiveTrainers();

    // Find staff members (non-members)
    @Query("SELECT u FROM User u WHERE u.role != 'MEMBER' AND u.status = 'ACTIVE' AND u.enabled = true")
    List<User> findActiveStaff();

    // Check if username exists (excluding specific user)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.id != :userId")
    boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("userId") Long userId);

    // Check if email exists (excluding specific user)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :userId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("userId") Long userId);
}
