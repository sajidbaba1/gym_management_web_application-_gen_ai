package com.example.gym_management_system.service;

import com.example.gym_management_system.dto.AuthRequest;
import com.example.gym_management_system.dto.AuthResponse;
import com.example.gym_management_system.dto.RegisterRequest;
import com.example.gym_management_system.entity.User;
import com.example.gym_management_system.repository.UserRepository;
import com.example.gym_management_system.security.JwtUtil;
import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j; // Replaced with manual logger
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    // Authenticate user and generate JWT token
    public AuthResponse authenticate(AuthRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );

            // Get user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsernameOrEmail(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if user is active
            if (!user.isEnabled() || user.getStatus() != User.UserStatus.ACTIVE) {
                throw new BadCredentialsException("Account is disabled or inactive");
            }

            // Generate JWT tokens
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("userId", user.getId());
            extraClaims.put("role", user.getRole().name());
            extraClaims.put("fullName", user.getFullName());

            String accessToken = jwtUtil.generateToken(userDetails, extraClaims);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            // Update last login time
            userService.updateLastLogin(user.getUsername());

            log.info("User authenticated successfully: {}", user.getUsername());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getExpirationTime())
                    .user(mapToUserInfo(user))
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Authentication failed for user: {}", request.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    // Register new user
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : User.Role.MEMBER);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setEnabled(true);

        // Save user
        User savedUser = userRepository.save(user);

        // Generate JWT tokens
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", savedUser.getId());
        extraClaims.put("role", savedUser.getRole().name());
        extraClaims.put("fullName", savedUser.getFullName());

        String accessToken = jwtUtil.generateToken(savedUser, extraClaims);
        String refreshToken = jwtUtil.generateRefreshToken(savedUser);

        log.info("User registered successfully: {}", savedUser.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .user(mapToUserInfo(savedUser))
                .build();
    }

    // Refresh JWT token
    public AuthResponse refreshToken(String refreshToken) {
        try {
            // Validate refresh token
            if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
                throw new RuntimeException("Invalid refresh token");
            }

            // Extract username from refresh token
            String username = jwtUtil.extractUsername(refreshToken);
            
            // Load user details
            User user = userRepository.findByUsernameOrEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if user is still active
            if (!user.isEnabled() || user.getStatus() != User.UserStatus.ACTIVE) {
                throw new RuntimeException("Account is disabled or inactive");
            }

            // Generate new access token
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("userId", user.getId());
            extraClaims.put("role", user.getRole().name());
            extraClaims.put("fullName", user.getFullName());

            String newAccessToken = jwtUtil.generateToken(user, extraClaims);

            log.info("Token refreshed successfully for user: {}", user.getUsername());

            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken) // Keep the same refresh token
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getExpirationTime())
                    .user(mapToUserInfo(user))
                    .build();

        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new RuntimeException("Token refresh failed: " + e.getMessage());
        }
    }

    // Logout user (invalidate token - in a real app, you'd maintain a blacklist)
    public void logout(String token) {
        try {
            String username = jwtUtil.extractUsername(token);
            log.info("User logged out: {}", username);
            // In a production app, you would add the token to a blacklist
        } catch (Exception e) {
            log.warn("Logout attempt with invalid token: {}", e.getMessage());
        }
    }

    // Change password
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", username);
    }

    // Reset password (for forgot password functionality)
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password reset successfully for user: {}", user.getUsername());
    }

    // Helper method to map User to UserInfo
    private AuthResponse.UserInfo mapToUserInfo(User user) {
        return AuthResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .profileImageUrl(user.getProfileImageUrl())
                .emailVerified(user.getEmailVerified())
                .build();
    }
}
