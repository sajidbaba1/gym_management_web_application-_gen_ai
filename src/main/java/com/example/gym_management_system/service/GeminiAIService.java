package com.example.gym_management_system.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiAIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getChatResponse(String userMessage, String userRole) {
        try {
            // Create role-specific context
            String systemPrompt = getRoleSpecificPrompt(userRole);
            String fullPrompt = systemPrompt + "\n\nUser: " + userMessage + "\n\nAssistant:";

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> contents = new HashMap<>();
            Map<String, Object> parts = new HashMap<>();
            
            parts.put("text", fullPrompt);
            contents.put("parts", List.of(parts));
            requestBody.put("contents", List.of(contents));
            
            // Add generation config
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 1024);
            requestBody.put("generationConfig", generationConfig);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Make API call
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl, HttpMethod.POST, entity, String.class);

            // Parse response
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                JsonNode candidates = jsonResponse.get("candidates");
                
                if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                    JsonNode firstCandidate = candidates.get(0);
                    JsonNode content = firstCandidate.get("content");
                    JsonNode partsNode = content.get("parts");
                    
                    if (partsNode != null && partsNode.isArray() && partsNode.size() > 0) {
                        return partsNode.get(0).get("text").asText();
                    }
                }
            }

            return "I'm sorry, I couldn't process your request at the moment. Please try again.";

        } catch (Exception e) {
            // Error calling Gemini AI API
            return "I'm experiencing technical difficulties. Please try again later.";
        }
    }

    private String getRoleSpecificPrompt(String userRole) {
        return switch (userRole.toUpperCase()) {
            case "ADMIN" -> """
                You are FitHub AI Assistant for Gym Administrators. You help with:
                - System management and configuration
                - User management and permissions
                - Financial reports and analytics
                - Strategic planning and decision making
                - Staff management and scheduling
                - Compliance and regulatory matters
                
                Provide professional, detailed responses with actionable insights for gym administration.
                """;
                
            case "MANAGER" -> """
                You are FitHub AI Assistant for Gym Managers. You help with:
                - Daily operations management
                - Staff scheduling and coordination
                - Member satisfaction and retention
                - Class scheduling and optimization
                - Performance monitoring and reporting
                - Conflict resolution and problem solving
                
                Provide practical, solution-oriented advice for effective gym management.
                """;
                
            case "TRAINER" -> """
                You are FitHub AI Assistant for Personal Trainers. You help with:
                - Workout planning and exercise programming
                - Nutrition guidance and meal planning
                - Client motivation and coaching techniques
                - Injury prevention and safety protocols
                - Progress tracking and assessment
                - Professional development and certifications
                
                Provide expert fitness advice with safety considerations and evidence-based recommendations.
                """;
                
            case "RECEPTIONIST" -> """
                You are FitHub AI Assistant for Gym Receptionists. You help with:
                - Customer service and communication
                - Membership inquiries and enrollment
                - Scheduling and booking assistance
                - Payment processing and billing questions
                - Facility information and tours
                - Problem resolution and escalation
                
                Provide friendly, helpful responses focused on excellent customer service.
                """;
                
            case "MEMBER" -> """
                You are FitHub AI Assistant for Gym Members. You help with:
                - Workout routines and exercise guidance
                - Fitness goal setting and achievement
                - Nutrition tips and healthy lifestyle advice
                - Class recommendations and scheduling
                - Equipment usage and safety
                - Motivation and wellness support
                
                Provide encouraging, personalized fitness advice to help members achieve their goals.
                """;
                
            default -> """
                You are FitHub AI Assistant. You provide helpful information about:
                - Fitness and exercise
                - Gym facilities and services
                - Health and wellness
                - General gym-related questions
                
                Provide helpful, accurate information while maintaining a friendly and professional tone.
                """;
        };
    }

    public String getWorkoutSuggestion(String fitnessLevel, String goals, String equipment) {
        String prompt = String.format("""
            As a fitness expert, create a personalized workout plan for:
            - Fitness Level: %s
            - Goals: %s
            - Available Equipment: %s
            
            Provide a detailed workout routine with exercises, sets, reps, and safety tips.
            """, fitnessLevel, goals, equipment);
            
        return getChatResponse(prompt, "TRAINER");
    }

    public String getNutritionAdvice(String goals, String dietaryRestrictions, String currentWeight) {
        String prompt = String.format("""
            As a nutrition expert, provide dietary advice for:
            - Goals: %s
            - Dietary Restrictions: %s
            - Current Weight: %s
            
            Include meal suggestions, macronutrient breakdown, and healthy eating tips.
            """, goals, dietaryRestrictions, currentWeight);
            
        return getChatResponse(prompt, "TRAINER");
    }

    public String getBusinessInsight(String topic, String data) {
        String prompt = String.format("""
            As a gym business consultant, analyze this data and provide insights:
            - Topic: %s
            - Data: %s
            
            Provide actionable recommendations for improving gym operations and profitability.
            """, topic, data);
            
        return getChatResponse(prompt, "MANAGER");
    }

    public String getMotivationalMessage(String memberName, String goals, String progress) {
        String prompt = String.format("""
            Create a personalized motivational message for:
            - Member: %s
            - Goals: %s
            - Current Progress: %s
            
            Make it encouraging, specific, and actionable.
            """, memberName, goals, progress);
            
        return getChatResponse(prompt, "TRAINER");
    }
}
