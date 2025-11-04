package com.example.gym_management_system.controller;

import com.example.gym_management_system.entity.Trainer;
import com.example.gym_management_system.entity.User;
import com.example.gym_management_system.service.TrainerService;
import com.example.gym_management_system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/trainers")
@RequiredArgsConstructor
public class TrainerController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainerController.class);

    private final TrainerService trainerService;
    private final UserService userService;

    // List all trainers with pagination and search
    @GetMapping
    public String listTrainers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String search,
            Model model) {

        Page<Trainer> trainersPage;
        
        if (search != null && !search.trim().isEmpty()) {
            trainersPage = trainerService.searchTrainers(search.trim(), page, size);
            model.addAttribute("search", search);
        } else {
            trainersPage = trainerService.getAllTrainers(page, size, sortBy, sortDirection);
        }

        // Get trainer statistics
        TrainerService.TrainerStats stats = trainerService.getTrainerStats();

        model.addAttribute("trainersPage", trainersPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", trainersPage.getTotalPages());
        model.addAttribute("totalElements", trainersPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("stats", stats);
        model.addAttribute("pageTitle", "Trainers - FitHub");

        return "trainers/list";
    }

    // Show add trainer form
    @GetMapping("/add")
    public String showAddTrainerForm(Model model) {
        // Get users with TRAINER role who don't have trainer profiles yet
        List<User> availableTrainerUsers = userService.getUsersByRole(User.Role.TRAINER)
                .stream()
                .filter(user -> trainerService.getTrainerByUserId(user.getId()).isEmpty())
                .toList();

        model.addAttribute("trainer", new Trainer());
        model.addAttribute("availableUsers", availableTrainerUsers);
        model.addAttribute("specializations", Trainer.Specialization.values());
        model.addAttribute("employmentTypes", Trainer.EmploymentType.values());
        model.addAttribute("pageTitle", "Add New Trainer - FitHub");
        model.addAttribute("isEdit", false);
        return "trainers/form";
    }

    // Handle add trainer form submission
    @PostMapping("/add")
    public String addTrainer(@ModelAttribute Trainer trainer, 
                            BindingResult result, 
                            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "trainers/form";
        }

        try {
            Trainer savedTrainer = trainerService.createTrainer(trainer);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Trainer '" + savedTrainer.getFullName() + "' added successfully!");
            return "redirect:/trainers";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/trainers/add";
        }
    }

    // Show trainer details
    @GetMapping("/{id}")
    public String viewTrainer(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Trainer> trainerOpt = trainerService.getTrainerById(id);
        
        if (trainerOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Trainer not found!");
            return "redirect:/trainers";
        }

        Trainer trainer = trainerOpt.get();
        model.addAttribute("trainer", trainer);
        model.addAttribute("pageTitle", trainer.getFullName() + " - Trainer Details");
        
        return "trainers/view";
    }

    // Show edit trainer form
    @GetMapping("/{id}/edit")
    public String showEditTrainerForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Trainer> trainerOpt = trainerService.getTrainerById(id);
        
        if (trainerOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Trainer not found!");
            return "redirect:/trainers";
        }

        model.addAttribute("trainer", trainerOpt.get());
        model.addAttribute("specializations", Trainer.Specialization.values());
        model.addAttribute("employmentTypes", Trainer.EmploymentType.values());
        model.addAttribute("pageTitle", "Edit Trainer - FitHub");
        model.addAttribute("isEdit", true);
        return "trainers/form";
    }

    // Handle edit trainer form submission
    @PostMapping("/{id}/edit")
    public String editTrainer(@PathVariable Long id, 
                             @ModelAttribute Trainer trainer, 
                             BindingResult result, 
                             RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "trainers/form";
        }

        try {
            Trainer updatedTrainer = trainerService.updateTrainer(id, trainer);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Trainer '" + updatedTrainer.getFullName() + "' updated successfully!");
            return "redirect:/trainers/" + id;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/trainers/" + id + "/edit";
        }
    }

    // Delete trainer
    @PostMapping("/{id}/delete")
    public String deleteTrainer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Trainer> trainerOpt = trainerService.getTrainerById(id);
            String trainerName = trainerOpt.map(Trainer::getFullName).orElse("Unknown");
            
            trainerService.deleteTrainer(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Trainer '" + trainerName + "' deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/trainers";
    }

    // Update trainer status
    @PostMapping("/{id}/status")
    public String updateTrainerStatus(@PathVariable Long id, 
                                     @RequestParam String status, 
                                     RedirectAttributes redirectAttributes) {
        try {
            Trainer.TrainerStatus trainerStatus = Trainer.TrainerStatus.valueOf(status.toUpperCase());
            Trainer updatedTrainer = trainerService.updateTrainerStatus(id, trainerStatus);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Trainer status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Failed to update trainer status: " + e.getMessage());
        }
        
        return "redirect:/trainers/" + id;
    }

    // Get trainers by status (AJAX endpoint)
    @GetMapping("/status/{status}")
    @ResponseBody
    public List<Trainer> getTrainersByStatus(@PathVariable String status) {
        try {
            Trainer.TrainerStatus trainerStatus = Trainer.TrainerStatus.valueOf(status.toUpperCase());
            return trainerService.getTrainersByStatus(trainerStatus);
        } catch (IllegalArgumentException e) {
            log.error("Invalid trainer status: {}", status);
            return List.of();
        }
    }

    // Get trainers by specialization (AJAX endpoint)
    @GetMapping("/specialization/{specialization}")
    @ResponseBody
    public List<Trainer> getTrainersBySpecialization(@PathVariable String specialization) {
        try {
            Trainer.Specialization spec = Trainer.Specialization.valueOf(specialization.toUpperCase());
            return trainerService.getTrainersBySpecialization(spec);
        } catch (IllegalArgumentException e) {
            log.error("Invalid specialization: {}", specialization);
            return List.of();
        }
    }

    // Get active trainers (AJAX endpoint)
    @GetMapping("/active")
    @ResponseBody
    public List<Trainer> getActiveTrainers() {
        return trainerService.getActiveTrainers();
    }

    // Get top rated trainers (AJAX endpoint)
    @GetMapping("/top-rated")
    @ResponseBody
    public List<Trainer> getTopRatedTrainers(@RequestParam(defaultValue = "10") int limit) {
        return trainerService.getTopRatedTrainers(limit);
    }

    // Get trainers available on specific day (AJAX endpoint)
    @GetMapping("/available/{day}")
    @ResponseBody
    public List<Trainer> getTrainersAvailableOnDay(@PathVariable String day) {
        return trainerService.getTrainersAvailableOnDay(day);
    }
}
