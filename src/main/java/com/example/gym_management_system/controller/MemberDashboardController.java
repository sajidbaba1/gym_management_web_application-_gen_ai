package com.example.gym_management_system.controller;

import com.example.gym_management_system.entity.Member;
import com.example.gym_management_system.entity.Trainer;
import com.example.gym_management_system.entity.User;
import com.example.gym_management_system.service.MemberService;
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

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
public class MemberDashboardController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MemberDashboardController.class);

    private final MemberService memberService;
    private final TrainerService trainerService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        log.info("Member accessing dashboard");
        
        String username = authentication.getName();
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get member profile if exists
        Optional<Member> memberOpt = memberService.getMemberByEmail(user.getEmail());
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            model.addAttribute("member", member);
            model.addAttribute("hasProfile", true);
        } else {
            model.addAttribute("hasProfile", false);
        }
        
        // Get available trainers for selection
        List<Trainer> availableTrainers = trainerService.getActiveTrainers();
        model.addAttribute("availableTrainers", availableTrainers);
        
        model.addAttribute("pageTitle", "Member Dashboard - FitHub");
        return "dashboards/member";
    }

    @GetMapping("/trainers")
    public String selectTrainers(Model model) {
        log.info("Member accessing trainer selection");
        
        // Get all active trainers
        List<Trainer> trainers = trainerService.getActiveTrainers();
        model.addAttribute("trainers", trainers);
        model.addAttribute("pageTitle", "Select Trainers - FitHub");
        
        return "member/trainers";
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        log.info("Member accessing profile");
        
        String username = authentication.getName();
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get member profile
        Optional<Member> memberOpt = memberService.getMemberByEmail(user.getEmail());
        if (memberOpt.isPresent()) {
            model.addAttribute("member", memberOpt.get());
        }
        
        model.addAttribute("pageTitle", "My Profile - FitHub");
        return "member/profile";
    }
}
