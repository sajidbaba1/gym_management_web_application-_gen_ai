package com.example.gym_management_system.service;

import com.example.gym_management_system.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIFitnessPlanService {

    private final GeminiAIService geminiAIService;

    public String generatePersonalizedWorkoutPlan(Member member, String goals, String preferences, String limitations) {
        String memberInfo = buildMemberProfile(member);
        
        String prompt = String.format("""
            Create a comprehensive personalized workout plan for this gym member:
            
            MEMBER PROFILE:
            %s
            
            FITNESS GOALS: %s
            PREFERENCES: %s
            LIMITATIONS/INJURIES: %s
            
            Please provide:
            1. 4-week progressive workout plan
            2. Weekly schedule (days, exercises, sets, reps)
            3. Progression guidelines
            4. Safety considerations
            5. Expected results timeline
            6. Nutrition recommendations
            
            Format as a detailed, structured plan that can be easily followed.
            """, memberInfo, goals, preferences, limitations);
            
        return geminiAIService.getChatResponse(prompt, "TRAINER");
    }

    public String generateNutritionPlan(Member member, String dietaryGoals, String restrictions, String currentDiet) {
        String memberInfo = buildMemberProfile(member);
        
        String prompt = String.format("""
            Create a personalized nutrition plan for this gym member:
            
            MEMBER PROFILE:
            %s
            
            DIETARY GOALS: %s
            RESTRICTIONS/ALLERGIES: %s
            CURRENT DIET: %s
            
            Please provide:
            1. Daily caloric needs calculation
            2. Macronutrient breakdown (protein, carbs, fats)
            3. 7-day meal plan with recipes
            4. Pre/post workout nutrition
            5. Supplement recommendations
            6. Hydration guidelines
            7. Progress tracking tips
            
            Make it practical and easy to follow.
            """, memberInfo, dietaryGoals, restrictions, currentDiet);
            
        return geminiAIService.getChatResponse(prompt, "TRAINER");
    }

    public String generateProgressAnalysis(Member member, String currentStats, String previousStats) {
        String memberInfo = buildMemberProfile(member);
        
        String prompt = String.format("""
            Analyze the fitness progress for this gym member:
            
            MEMBER PROFILE:
            %s
            
            CURRENT STATS: %s
            PREVIOUS STATS: %s
            
            Please provide:
            1. Progress analysis and achievements
            2. Areas of improvement
            3. Potential challenges identified
            4. Adjusted recommendations
            5. Motivation and encouragement
            6. Next phase goals
            7. Timeline for next assessment
            
            Be encouraging while providing actionable insights.
            """, memberInfo, currentStats, previousStats);
            
        return geminiAIService.getChatResponse(prompt, "TRAINER");
    }

    public String generateMotivationalMessage(Member member, String recentActivity, String challenges) {
        String memberInfo = buildMemberProfile(member);
        
        String prompt = String.format("""
            Create a personalized motivational message for this gym member:
            
            MEMBER PROFILE:
            %s
            
            RECENT ACTIVITY: %s
            CURRENT CHALLENGES: %s
            
            Create an encouraging, personalized message that:
            1. Acknowledges their efforts
            2. Addresses their challenges positively
            3. Provides specific motivation
            4. Suggests actionable next steps
            5. Reinforces their goals
            
            Keep it upbeat, personal, and inspiring!
            """, memberInfo, recentActivity, challenges);
            
        return geminiAIService.getChatResponse(prompt, "TRAINER");
    }

    public String generateInjuryRecoveryPlan(Member member, String injuryDetails, String doctorRecommendations) {
        String memberInfo = buildMemberProfile(member);
        
        String prompt = String.format("""
            Create a safe injury recovery and rehabilitation plan:
            
            MEMBER PROFILE:
            %s
            
            INJURY DETAILS: %s
            DOCTOR'S RECOMMENDATIONS: %s
            
            Please provide:
            1. Safe exercise modifications
            2. Recovery timeline phases
            3. Exercises to avoid
            4. Recommended activities
            5. Pain management tips
            6. When to seek medical attention
            7. Gradual return to full activity plan
            
            IMPORTANT: Emphasize safety and medical clearance requirements.
            """, memberInfo, injuryDetails, doctorRecommendations);
            
        return geminiAIService.getChatResponse(prompt, "TRAINER");
    }

    public String generateClassRecommendations(Member member, String availableClasses, String schedule) {
        String memberInfo = buildMemberProfile(member);
        
        String prompt = String.format("""
            Recommend the best gym classes for this member:
            
            MEMBER PROFILE:
            %s
            
            AVAILABLE CLASSES: %s
            MEMBER'S SCHEDULE: %s
            
            Please provide:
            1. Top 5 recommended classes with reasons
            2. Beginner-friendly options
            3. Progressive class sequence
            4. Schedule optimization
            5. Class combination strategies
            6. Expected benefits from each class
            
            Prioritize member's fitness level and goals.
            """, memberInfo, availableClasses, schedule);
            
        return geminiAIService.getChatResponse(prompt, "TRAINER");
    }

    private String buildMemberProfile(Member member) {
        StringBuilder profile = new StringBuilder();
        
        profile.append("Name: ").append(member.getFullName()).append("\n");
        profile.append("Membership Type: ").append(member.getMembershipType()).append("\n");
        profile.append("Member Since: ").append(member.getMembershipStartDate()).append("\n");
        
        if (member.getDateOfBirth() != null) {
            int age = Period.between(member.getDateOfBirth(), LocalDate.now()).getYears();
            profile.append("Age: ").append(age).append(" years\n");
        }
        
        if (member.getGender() != null) {
            profile.append("Gender: ").append(member.getGender()).append("\n");
        }
        
        profile.append("Completed Sessions: ").append(member.getCompletedSessions()).append("/").append(member.getTotalSessions()).append("\n");
        profile.append("Progress: ").append(member.getProgressPercentage()).append("%\n");
        
        if (member.getNotes() != null && !member.getNotes().isEmpty()) {
            profile.append("Additional Notes: ").append(member.getNotes()).append("\n");
        }
        
        return profile.toString();
    }

    public Map<String, String> generateComprehensivePlan(Member member, Map<String, String> preferences) {
        Map<String, String> plans = new HashMap<>();
        
        // Generate workout plan
        String workoutPlan = generatePersonalizedWorkoutPlan(
            member,
            preferences.getOrDefault("goals", "General fitness"),
            preferences.getOrDefault("preferences", "Balanced training"),
            preferences.getOrDefault("limitations", "None")
        );
        plans.put("workout", workoutPlan);
        
        // Generate nutrition plan
        String nutritionPlan = generateNutritionPlan(
            member,
            preferences.getOrDefault("dietGoals", "Healthy eating"),
            preferences.getOrDefault("restrictions", "None"),
            preferences.getOrDefault("currentDiet", "Standard diet")
        );
        plans.put("nutrition", nutritionPlan);
        
        // Generate motivational message
        String motivation = generateMotivationalMessage(
            member,
            "Starting new fitness journey",
            preferences.getOrDefault("challenges", "Time management")
        );
        plans.put("motivation", motivation);
        
        return plans;
    }
}
