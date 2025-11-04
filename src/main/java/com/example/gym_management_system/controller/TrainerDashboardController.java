package com.example.gym_management_system.controller;

import com.example.gym_management_system.entity.Trainer;
import com.example.gym_management_system.entity.User;
import com.example.gym_management_system.service.TrainerService;
import com.example.gym_management_system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/trainer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TRAINER')")
public class TrainerDashboardController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainerDashboardController.class);
    private final TrainerService trainerService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String trainerDashboard(Model model, Authentication authentication) {
        log.info("Trainer accessing dashboard");
        
        String username = authentication.getName();
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get trainer profile
        Optional<Trainer> trainerOpt = trainerService.getTrainerByUserId(user.getId());
        if (trainerOpt.isPresent()) {
            Trainer trainer = trainerOpt.get();
            model.addAttribute("trainer", trainer);
            model.addAttribute("hasProfile", true);
        } else {
            model.addAttribute("hasProfile", false);
        }
        
        // Get trainer statistics
        TrainerService.TrainerStats stats = trainerService.getTrainerStats();
        model.addAttribute("stats", stats);
        model.addAttribute("pageTitle", "Trainer Dashboard - FitHub");
        
        return "dashboards/trainer";
    }

    @GetMapping("/schedule")
    public String trainerSchedule(Model model) {
        log.info("Trainer accessing schedule");
        
        model.addAttribute("pageTitle", "My Schedule - FitHub");
        return "trainer/schedule";
    }

    @GetMapping("/progress")
    public String memberProgress(Model model) {
        log.info("Trainer accessing member progress tracking");
        
        model.addAttribute("pageTitle", "Member Progress - FitHub");
        return "trainer/progress";
    }
}
