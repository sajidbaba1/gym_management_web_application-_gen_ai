package com.example.gym_management_system.controller;

import com.example.gym_management_system.dto.AuthRequest;
import com.example.gym_management_system.dto.AuthResponse;
import com.example.gym_management_system.dto.RegisterRequest;
import com.example.gym_management_system.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    // Show login page
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("authRequest", new AuthRequest());
        model.addAttribute("pageTitle", "Login - FitHub");
        return "auth/login";
    }

    // Show register page
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        model.addAttribute("pageTitle", "Register - FitHub");
        return "auth/register";
    }

    // Handle login form submission
    @PostMapping("/login")
    public String login(@Valid @ModelAttribute AuthRequest authRequest,
                       BindingResult result,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request) {
        
        if (result.hasErrors()) {
            return "auth/login";
        }

        try {
            AuthResponse authResponse = authService.authenticate(authRequest);
            
            // Store JWT token in session or cookie for web interface
            request.getSession().setAttribute("jwt_token", authResponse.getAccessToken());
            request.getSession().setAttribute("user_info", authResponse.getUser());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Welcome back, " + authResponse.getUser().getFullName() + "!");
            
            // Redirect based on user role
            String redirectUrl = getRedirectUrlByRole(authResponse.getUser().getRole());
            return "redirect:" + redirectUrl;
            
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/login";
        }
    }

    // Handle register form submission
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest registerRequest,
                          BindingResult result,
                          RedirectAttributes redirectAttributes,
                          HttpServletRequest request) {
        
        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            AuthResponse authResponse = authService.register(registerRequest);
            
            // Store JWT token in session
            request.getSession().setAttribute("jwt_token", authResponse.getAccessToken());
            request.getSession().setAttribute("user_info", authResponse.getUser());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Registration successful! Welcome to FitHub, " + authResponse.getUser().getFullName() + "!");
            
            // Redirect based on user role
            String redirectUrl = getRedirectUrlByRole(authResponse.getUser().getRole());
            return "redirect:" + redirectUrl;
            
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    // Logout
    @PostMapping("/logout")
    public String logout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            // Get token from session
            String token = (String) request.getSession().getAttribute("jwt_token");
            if (token != null) {
                authService.logout(token);
            }
            
            // Invalidate session
            request.getSession().invalidate();
            
            redirectAttributes.addFlashAttribute("successMessage", "You have been logged out successfully.");
            
        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage());
        }
        
        return "redirect:/login";
    }

    // REST API Endpoints for mobile/API clients
    
    @PostMapping("/auth/login")
    @ResponseBody
    public ResponseEntity<AuthResponse> authenticateApi(@Valid @RequestBody AuthRequest authRequest) {
        try {
            AuthResponse authResponse = authService.authenticate(authRequest);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            log.error("API login failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/auth/register")
    @ResponseBody
    public ResponseEntity<AuthResponse> registerApi(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            AuthResponse authResponse = authService.register(registerRequest);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            log.error("API registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/auth/refresh")
    @ResponseBody
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody String refreshToken) {
        try {
            AuthResponse authResponse = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/auth/logout")
    @ResponseBody
    public ResponseEntity<Void> logoutApi(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                authService.logout(token);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("API logout error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Helper method to determine redirect URL based on user role
    private String getRedirectUrlByRole(String role) {
        return switch (role) {
            case "OWNER" -> "/owner/dashboard";
            case "ADMIN" -> "/admin/dashboard";
            case "MANAGER" -> "/manager/dashboard";
            case "TRAINER" -> "/trainer/dashboard";
            case "RECEPTIONIST" -> "/receptionist/dashboard";
            case "MEMBER" -> "/member/dashboard";
            default -> "/dashboard";
        };
    }
}
