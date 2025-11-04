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
@RequestMapping("/owner")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class OwnerController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OwnerController.class);

    private final MemberService memberService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        log.info("Owner accessing dashboard");
        
        // Get comprehensive statistics
        MemberService.MemberStats memberStats = memberService.getMemberStats();
        
        // Get user statistics
        long totalUsers = userService.getTotalUsers();
        long totalTrainers = userService.getUserCountByRole(User.Role.TRAINER);
        long totalAdmins = userService.getUserCountByRole(User.Role.ADMIN);
        long totalManagers = userService.getUserCountByRole(User.Role.MANAGER);
        
        model.addAttribute("memberStats", memberStats);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalTrainers", totalTrainers);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("totalManagers", totalManagers);
        model.addAttribute("pageTitle", "Owner Dashboard - FitHub");
        
        return "dashboards/owner";
    }

    @GetMapping("/analytics")
    public String analytics(Model model) {
        log.info("Owner accessing analytics");
        
        // Add comprehensive analytics data
        MemberService.MemberStats memberStats = memberService.getMemberStats();
        model.addAttribute("memberStats", memberStats);
        model.addAttribute("pageTitle", "Business Analytics - FitHub");
        
        return "dashboards/owner-analytics";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        log.info("Owner accessing system settings");
        
        model.addAttribute("pageTitle", "System Settings - FitHub");
        return "dashboards/owner-settings";
    }
}
