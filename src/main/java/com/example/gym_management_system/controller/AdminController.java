package com.example.gym_management_system.controller;

import com.example.gym_management_system.entity.User;
import com.example.gym_management_system.service.MemberService;
import com.example.gym_management_system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final MemberService memberService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Admin accessing dashboard
        
        // Get statistics
        MemberService.MemberStats memberStats = memberService.getMemberStats();
        long totalUsers = userService.getTotalUsers();
        long totalTrainers = userService.getUserCountByRole(User.Role.TRAINER);
        
        model.addAttribute("memberStats", memberStats);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalTrainers", totalTrainers);
        model.addAttribute("pageTitle", "Admin Dashboard - FitHub");
        
        return "dashboards/admin";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        // Admin accessing user management
        
        model.addAttribute("pageTitle", "User Management - FitHub");
        return "admin/users";
    }

    @GetMapping("/system")
    public String systemSettings(Model model) {
        // Admin accessing system settings
        
        model.addAttribute("pageTitle", "System Settings - FitHub");
        return "admin/system";
    }
}
