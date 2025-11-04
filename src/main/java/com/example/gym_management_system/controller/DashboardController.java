package com.example.gym_management_system.controller;

import com.example.gym_management_system.entity.User;
import com.example.gym_management_system.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DashboardController.class);
    private final UserService userService;
    private final MemberService memberService;
    private final GymClassService gymClassService;
    private final TrainerService trainerService;

    // Generic dashboard - redirects based on user role
    @GetMapping
    public String dashboard(Model model) {
        // For now, show admin dashboard since we don't have authentication context
        return "redirect:/admin/dashboard";
    }

    // Admin Dashboard
    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        try {
            // Get statistics
            UserService.UserStats userStats = userService.getUserStats();
            MemberService.MemberStats memberStats = memberService.getMemberStats();
            GymClassService.ClassStats classStats = gymClassService.getClassStats();
            TrainerService.TrainerStats trainerStats = trainerService.getTrainerStats();

            // Get recent data
            var recentMembers = memberService.getRecentMembers(5);
            var upcomingClasses = gymClassService.getUpcomingClasses().stream().limit(5).toList();
            var topTrainers = trainerService.getTopRatedTrainers(5);

            model.addAttribute("userStats", userStats);
            model.addAttribute("memberStats", memberStats);
            model.addAttribute("classStats", classStats);
            model.addAttribute("trainerStats", trainerStats);
            model.addAttribute("recentMembers", recentMembers);
            model.addAttribute("upcomingClasses", upcomingClasses);
            model.addAttribute("topTrainers", topTrainers);
            model.addAttribute("pageTitle", "Admin Dashboard - FitHub");
            model.addAttribute("userRole", "ADMIN");

            return "dashboard/admin";
        } catch (Exception e) {
            log.error("Error loading admin dashboard: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error loading dashboard data");
            return "dashboard/admin";
        }
    }

    // Manager Dashboard
    @GetMapping("/manager")
    public String managerDashboard(Model model) {
        try {
            // Get operational statistics
            MemberService.MemberStats memberStats = memberService.getMemberStats();
            GymClassService.ClassStats classStats = gymClassService.getClassStats();
            TrainerService.TrainerStats trainerStats = trainerService.getTrainerStats();

            // Get today's data
            var todaysClasses = gymClassService.getTodaysClasses();
            var activeMembers = memberService.getActiveMembers().stream().limit(10).toList();
            var availableTrainers = trainerService.getActiveTrainers().stream().limit(8).toList();

            model.addAttribute("memberStats", memberStats);
            model.addAttribute("classStats", classStats);
            model.addAttribute("trainerStats", trainerStats);
            model.addAttribute("todaysClasses", todaysClasses);
            model.addAttribute("activeMembers", activeMembers);
            model.addAttribute("availableTrainers", availableTrainers);
            model.addAttribute("pageTitle", "Manager Dashboard - FitHub");
            model.addAttribute("userRole", "MANAGER");

            return "dashboard/manager";
        } catch (Exception e) {
            log.error("Error loading manager dashboard: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error loading dashboard data");
            return "dashboard/manager";
        }
    }

    // Trainer Dashboard
    @GetMapping("/trainer")
    public String trainerDashboard(Model model) {
        try {
            // For demo purposes, we'll show general trainer info
            // In real implementation, this would be specific to logged-in trainer
            
            var todaysClasses = gymClassService.getTodaysClasses();
            var upcomingClasses = gymClassService.getUpcomingClasses().stream().limit(5).toList();
            var activeMembers = memberService.getActiveMembers().stream().limit(8).toList();

            model.addAttribute("todaysClasses", todaysClasses);
            model.addAttribute("upcomingClasses", upcomingClasses);
            model.addAttribute("activeMembers", activeMembers);
            model.addAttribute("pageTitle", "Trainer Dashboard - FitHub");
            model.addAttribute("userRole", "TRAINER");

            return "dashboard/trainer";
        } catch (Exception e) {
            log.error("Error loading trainer dashboard: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error loading dashboard data");
            return "dashboard/trainer";
        }
    }

    // Receptionist Dashboard
    @GetMapping("/receptionist")
    public String receptionistDashboard(Model model) {
        try {
            // Get member-focused statistics
            MemberService.MemberStats memberStats = memberService.getMemberStats();
            
            var recentMembers = memberService.getRecentMembers(10);
            var expiringMemberships = memberService.getExpiringMemberships().stream().limit(8).toList();
            var todaysClasses = gymClassService.getTodaysClasses();

            model.addAttribute("memberStats", memberStats);
            model.addAttribute("recentMembers", recentMembers);
            model.addAttribute("expiringMemberships", expiringMemberships);
            model.addAttribute("todaysClasses", todaysClasses);
            model.addAttribute("pageTitle", "Receptionist Dashboard - FitHub");
            model.addAttribute("userRole", "RECEPTIONIST");

            return "dashboard/receptionist";
        } catch (Exception e) {
            log.error("Error loading receptionist dashboard: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error loading dashboard data");
            return "dashboard/receptionist";
        }
    }

    // Member Dashboard
    @GetMapping("/member")
    public String memberDashboard(Model model) {
        try {
            // For demo purposes, show general member view
            // In real implementation, this would be specific to logged-in member
            
            var upcomingClasses = gymClassService.getUpcomingClasses().stream().limit(6).toList();
            var availableClasses = gymClassService.getAvailableClasses().stream().limit(8).toList();
            var topTrainers = trainerService.getTopRatedTrainers(4);

            model.addAttribute("upcomingClasses", upcomingClasses);
            model.addAttribute("availableClasses", availableClasses);
            model.addAttribute("topTrainers", topTrainers);
            model.addAttribute("pageTitle", "Member Dashboard - FitHub");
            model.addAttribute("userRole", "MEMBER");

            return "dashboard/member";
        } catch (Exception e) {
            log.error("Error loading member dashboard: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error loading dashboard data");
            return "dashboard/member";
        }
    }
}

// Individual role-specific dashboard controllers
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
class AdminDashboardController {
    
    private final DashboardController dashboardController;
    
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        return dashboardController.adminDashboard(model);
    }
}

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
class ManagerDashboardController {
    
    private final DashboardController dashboardController;
    
    @GetMapping("/dashboard")
    public String managerDashboard(Model model) {
        return dashboardController.managerDashboard(model);
    }
}

// Removed duplicate controller classes - using separate controller files instead
