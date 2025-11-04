package com.example.gym_management_system.controller;

import com.example.gym_management_system.service.GeminiAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ChatbotController.class);
    private final GeminiAIService geminiAIService;

    @PostMapping("/chat")
    @ResponseBody
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            String userRole = request.getOrDefault("role", "MEMBER");
            
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Message cannot be empty"));
            }

            String response = geminiAIService.getChatResponse(message, userRole);
            
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("response", response);
            responseMap.put("role", userRole);
            
            return ResponseEntity.ok(responseMap);
            
        } catch (Exception e) {
            log.error("Error in chatbot controller: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Sorry, I'm experiencing technical difficulties. Please try again."));
        }
    }

    @PostMapping("/workout-suggestion")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getWorkoutSuggestion(@RequestBody Map<String, String> request) {
        try {
            String fitnessLevel = request.getOrDefault("fitnessLevel", "Beginner");
            String goals = request.getOrDefault("goals", "General fitness");
            String equipment = request.getOrDefault("equipment", "Basic gym equipment");
            
            String response = geminiAIService.getWorkoutSuggestion(fitnessLevel, goals, equipment);
            
            return ResponseEntity.ok(Map.of("response", response));
            
        } catch (Exception e) {
            log.error("Error getting workout suggestion: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Unable to generate workout suggestion"));
        }
    }

    @PostMapping("/nutrition-advice")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getNutritionAdvice(@RequestBody Map<String, String> request) {
        try {
            String goals = request.getOrDefault("goals", "General health");
            String restrictions = request.getOrDefault("restrictions", "None");
            String weight = request.getOrDefault("weight", "Not specified");
            
            String response = geminiAIService.getNutritionAdvice(goals, restrictions, weight);
            
            return ResponseEntity.ok(Map.of("response", response));
            
        } catch (Exception e) {
            log.error("Error getting nutrition advice: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Unable to generate nutrition advice"));
        }
    }

    @PostMapping("/business-insight")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getBusinessInsight(@RequestBody Map<String, String> request) {
        try {
            String topic = request.getOrDefault("topic", "General business");
            String data = request.getOrDefault("data", "No data provided");
            
            String response = geminiAIService.getBusinessInsight(topic, data);
            
            return ResponseEntity.ok(Map.of("response", response));
            
        } catch (Exception e) {
            log.error("Error getting business insight: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Unable to generate business insight"));
        }
    }

    @PostMapping("/motivation")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getMotivation(@RequestBody Map<String, String> request) {
        try {
            String memberName = request.getOrDefault("memberName", "Member");
            String goals = request.getOrDefault("goals", "Fitness goals");
            String progress = request.getOrDefault("progress", "Making progress");
            
            String response = geminiAIService.getMotivationalMessage(memberName, goals, progress);
            
            return ResponseEntity.ok(Map.of("response", response));
            
        } catch (Exception e) {
            log.error("Error getting motivational message: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Unable to generate motivational message"));
        }
    }
}
