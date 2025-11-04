package com.example.gym_management_system.controller;

import com.example.gym_management_system.entity.Member;
import com.example.gym_management_system.service.AIFitnessPlanService;
import com.example.gym_management_system.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/ai-fitness")
@RequiredArgsConstructor
public class AIFitnessPlanController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AIFitnessPlanController.class);
    private final AIFitnessPlanService aiFitnessPlanService;
    private final MemberService memberService;

    // AI Fitness Plan Home
    @GetMapping
    public String aiPlanHome(Model model) {
        List<Member> activeMembers = memberService.getActiveMembers();
        model.addAttribute("members", activeMembers);
        model.addAttribute("pageTitle", "AI Fitness Planner - FitHub");
        return "ai-fitness/home";
    }

    // Member Selection for AI Plan
    @GetMapping("/member/{memberId}")
    public String memberPlanPage(@PathVariable Long memberId, Model model) {
        Optional<Member> memberOpt = memberService.getMemberById(memberId);
        
        if (memberOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Member not found!");
            return "redirect:/ai-fitness";
        }

        Member member = memberOpt.get();
        model.addAttribute("member", member);
        model.addAttribute("pageTitle", "AI Plan for " + member.getFullName() + " - FitHub");
        return "ai-fitness/member-plan";
    }

    // Generate Workout Plan
    @PostMapping("/api/workout-plan")
    @ResponseBody
    public ResponseEntity<Map<String, String>> generateWorkoutPlan(@RequestBody Map<String, String> request) {
        try {
            Long memberId = Long.parseLong(request.get("memberId"));
            String goals = request.getOrDefault("goals", "General fitness");
            String preferences = request.getOrDefault("preferences", "Balanced training");
            String limitations = request.getOrDefault("limitations", "None");

            Optional<Member> memberOpt = memberService.getMemberById(memberId);
            if (memberOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Member not found"));
            }

            Member member = memberOpt.get();
            String workoutPlan = aiFitnessPlanService.generatePersonalizedWorkoutPlan(
                member, goals, preferences, limitations);

            Map<String, String> response = new HashMap<>();
            response.put("plan", workoutPlan);
            response.put("type", "workout");
            response.put("memberName", member.getFullName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error generating workout plan: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate workout plan"));
        }
    }

    // Generate Nutrition Plan
    @PostMapping("/api/nutrition-plan")
    @ResponseBody
    public ResponseEntity<Map<String, String>> generateNutritionPlan(@RequestBody Map<String, String> request) {
        try {
            Long memberId = Long.parseLong(request.get("memberId"));
            String dietaryGoals = request.getOrDefault("dietaryGoals", "Healthy eating");
            String restrictions = request.getOrDefault("restrictions", "None");
            String currentDiet = request.getOrDefault("currentDiet", "Standard diet");

            Optional<Member> memberOpt = memberService.getMemberById(memberId);
            if (memberOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Member not found"));
            }

            Member member = memberOpt.get();
            String nutritionPlan = aiFitnessPlanService.generateNutritionPlan(
                member, dietaryGoals, restrictions, currentDiet);

            Map<String, String> response = new HashMap<>();
            response.put("plan", nutritionPlan);
            response.put("type", "nutrition");
            response.put("memberName", member.getFullName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error generating nutrition plan: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate nutrition plan"));
        }
    }

    // Generate Progress Analysis
    @PostMapping("/api/progress-analysis")
    @ResponseBody
    public ResponseEntity<Map<String, String>> generateProgressAnalysis(@RequestBody Map<String, String> request) {
        try {
            Long memberId = Long.parseLong(request.get("memberId"));
            String currentStats = request.getOrDefault("currentStats", "Current fitness level");
            String previousStats = request.getOrDefault("previousStats", "Previous fitness level");

            Optional<Member> memberOpt = memberService.getMemberById(memberId);
            if (memberOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Member not found"));
            }

            Member member = memberOpt.get();
            String progressAnalysis = aiFitnessPlanService.generateProgressAnalysis(
                member, currentStats, previousStats);

            Map<String, String> response = new HashMap<>();
            response.put("analysis", progressAnalysis);
            response.put("type", "progress");
            response.put("memberName", member.getFullName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error generating progress analysis: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate progress analysis"));
        }
    }

    // Generate Motivational Message
    @PostMapping("/api/motivation")
    @ResponseBody
    public ResponseEntity<Map<String, String>> generateMotivation(@RequestBody Map<String, String> request) {
        try {
            Long memberId = Long.parseLong(request.get("memberId"));
            String recentActivity = request.getOrDefault("recentActivity", "Regular gym attendance");
            String challenges = request.getOrDefault("challenges", "Staying motivated");

            Optional<Member> memberOpt = memberService.getMemberById(memberId);
            if (memberOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Member not found"));
            }

            Member member = memberOpt.get();
            String motivation = aiFitnessPlanService.generateMotivationalMessage(
                member, recentActivity, challenges);

            Map<String, String> response = new HashMap<>();
            response.put("message", motivation);
            response.put("type", "motivation");
            response.put("memberName", member.getFullName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error generating motivation: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate motivational message"));
        }
    }

    // Generate Injury Recovery Plan
    @PostMapping("/api/injury-recovery")
    @ResponseBody
    public ResponseEntity<Map<String, String>> generateInjuryRecovery(@RequestBody Map<String, String> request) {
        try {
            Long memberId = Long.parseLong(request.get("memberId"));
            String injuryDetails = request.getOrDefault("injuryDetails", "Minor injury");
            String doctorRecommendations = request.getOrDefault("doctorRecommendations", "Rest and gradual return");

            Optional<Member> memberOpt = memberService.getMemberById(memberId);
            if (memberOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Member not found"));
            }

            Member member = memberOpt.get();
            String recoveryPlan = aiFitnessPlanService.generateInjuryRecoveryPlan(
                member, injuryDetails, doctorRecommendations);

            Map<String, String> response = new HashMap<>();
            response.put("plan", recoveryPlan);
            response.put("type", "recovery");
            response.put("memberName", member.getFullName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error generating recovery plan: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate recovery plan"));
        }
    }

    // Generate Class Recommendations
    @PostMapping("/api/class-recommendations")
    @ResponseBody
    public ResponseEntity<Map<String, String>> generateClassRecommendations(@RequestBody Map<String, String> request) {
        try {
            Long memberId = Long.parseLong(request.get("memberId"));
            String availableClasses = request.getOrDefault("availableClasses", "Various fitness classes");
            String schedule = request.getOrDefault("schedule", "Flexible schedule");

            Optional<Member> memberOpt = memberService.getMemberById(memberId);
            if (memberOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Member not found"));
            }

            Member member = memberOpt.get();
            String recommendations = aiFitnessPlanService.generateClassRecommendations(
                member, availableClasses, schedule);

            Map<String, String> response = new HashMap<>();
            response.put("recommendations", recommendations);
            response.put("type", "classes");
            response.put("memberName", member.getFullName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error generating class recommendations: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate class recommendations"));
        }
    }

    // Generate Comprehensive Plan
    @PostMapping("/api/comprehensive-plan")
    @ResponseBody
    public ResponseEntity<Map<String, String>> generateComprehensivePlan(@RequestBody Map<String, String> request) {
        try {
            Long memberId = Long.parseLong(request.get("memberId"));

            Optional<Member> memberOpt = memberService.getMemberById(memberId);
            if (memberOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Member not found"));
            }

            Member member = memberOpt.get();
            
            // Extract preferences from request
            Map<String, String> preferences = new HashMap<>();
            preferences.put("goals", request.getOrDefault("goals", "General fitness"));
            preferences.put("preferences", request.getOrDefault("preferences", "Balanced training"));
            preferences.put("limitations", request.getOrDefault("limitations", "None"));
            preferences.put("dietGoals", request.getOrDefault("dietGoals", "Healthy eating"));
            preferences.put("restrictions", request.getOrDefault("restrictions", "None"));
            preferences.put("currentDiet", request.getOrDefault("currentDiet", "Standard diet"));
            preferences.put("challenges", request.getOrDefault("challenges", "Time management"));

            Map<String, String> plans = aiFitnessPlanService.generateComprehensivePlan(member, preferences);
            plans.put("memberName", member.getFullName());
            plans.put("type", "comprehensive");

            return ResponseEntity.ok(plans);

        } catch (Exception e) {
            log.error("Error generating comprehensive plan: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate comprehensive plan"));
        }
    }

    // Quick AI Suggestions for all members
    @GetMapping("/api/quick-suggestions")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getQuickSuggestions() {
        try {
            List<Member> activeMembers = memberService.getActiveMembers();
            Map<String, Object> suggestions = new HashMap<>();
            
            // General fitness tips
            suggestions.put("generalTips", List.of(
                "Stay hydrated - drink at least 8 glasses of water daily",
                "Get 7-9 hours of quality sleep for optimal recovery",
                "Include protein in every meal for muscle maintenance",
                "Warm up before workouts and cool down after",
                "Track your progress to stay motivated"
            ));
            
            // Workout of the day
            suggestions.put("workoutOfTheDay", 
                "Today's Focus: Full Body Strength\n" +
                "1. Squats - 3 sets of 12\n" +
                "2. Push-ups - 3 sets of 10\n" +
                "3. Plank - 3 sets of 30 seconds\n" +
                "4. Lunges - 3 sets of 10 each leg\n" +
                "5. Mountain climbers - 3 sets of 20"
            );
            
            // Nutrition tip
            suggestions.put("nutritionTip", 
                "Pre-workout fuel: Have a banana with a small amount of nut butter " +
                "30 minutes before your workout for sustained energy."
            );
            
            suggestions.put("totalMembers", activeMembers.size());
            
            return ResponseEntity.ok(suggestions);

        } catch (Exception e) {
            log.error("Error getting quick suggestions: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get suggestions"));
        }
    }
}
