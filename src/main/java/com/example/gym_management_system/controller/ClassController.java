package com.example.gym_management_system.controller;

import com.example.gym_management_system.entity.GymClass;
import com.example.gym_management_system.service.GymClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/classes")
@RequiredArgsConstructor
public class ClassController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClassController.class);
    private final GymClassService gymClassService;

    // List all classes with pagination and search
    @GetMapping
    public String listClasses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "classDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String search,
            Model model) {

        Page<GymClass> classesPage;
        
        if (search != null && !search.trim().isEmpty()) {
            classesPage = gymClassService.searchClasses(search.trim(), page, size);
            model.addAttribute("search", search);
        } else {
            classesPage = gymClassService.getAllClasses(page, size, sortBy, sortDirection);
        }

        // Get class statistics
        GymClassService.ClassStats stats = gymClassService.getClassStats();

        model.addAttribute("classesPage", classesPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", classesPage.getTotalPages());
        model.addAttribute("totalElements", classesPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("stats", stats);
        model.addAttribute("pageTitle", "Classes - FitHub");

        return "classes/list";
    }

    // Show add class form
    @GetMapping("/add")
    public String showAddClassForm(Model model) {
        model.addAttribute("gymClass", new GymClass());
        model.addAttribute("pageTitle", "Add New Class - FitHub");
        model.addAttribute("isEdit", false);
        return "classes/form";
    }

    // Handle add class form submission
    @PostMapping("/add")
    public String addClass(@ModelAttribute GymClass gymClass, 
                          BindingResult result, 
                          RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "classes/form";
        }

        try {
            GymClass savedClass = gymClassService.createClass(gymClass);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Class '" + savedClass.getName() + "' added successfully!");
            return "redirect:/classes";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/classes/add";
        }
    }

    // Show class details
    @GetMapping("/{id}")
    public String viewClass(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<GymClass> classOpt = gymClassService.getClassById(id);
        
        if (classOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Class not found!");
            return "redirect:/classes";
        }

        GymClass gymClass = classOpt.get();
        model.addAttribute("gymClass", gymClass);
        model.addAttribute("pageTitle", gymClass.getName() + " - Class Details");
        
        return "classes/view";
    }

    // Show edit class form
    @GetMapping("/{id}/edit")
    public String showEditClassForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<GymClass> classOpt = gymClassService.getClassById(id);
        
        if (classOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Class not found!");
            return "redirect:/classes";
        }

        model.addAttribute("gymClass", classOpt.get());
        model.addAttribute("pageTitle", "Edit Class - FitHub");
        model.addAttribute("isEdit", true);
        return "classes/form";
    }

    // Handle edit class form submission
    @PostMapping("/{id}/edit")
    public String editClass(@PathVariable Long id, 
                           @ModelAttribute GymClass gymClass, 
                           BindingResult result, 
                           RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "classes/form";
        }

        try {
            GymClass updatedClass = gymClassService.updateClass(id, gymClass);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Class '" + updatedClass.getName() + "' updated successfully!");
            return "redirect:/classes/" + id;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/classes/" + id + "/edit";
        }
    }

    // Delete class
    @PostMapping("/{id}/delete")
    public String deleteClass(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<GymClass> classOpt = gymClassService.getClassById(id);
            String className = classOpt.map(GymClass::getName).orElse("Unknown");
            
            gymClassService.deleteClass(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Class '" + className + "' deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/classes";
    }

    // Enroll in class
    @PostMapping("/{id}/enroll")
    public String enrollInClass(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            GymClass updatedClass = gymClassService.enrollMember(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Successfully enrolled in '" + updatedClass.getName() + "'!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/classes/" + id;
    }

    // Unenroll from class
    @PostMapping("/{id}/unenroll")
    public String unenrollFromClass(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            GymClass updatedClass = gymClassService.unenrollMember(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Successfully unenrolled from '" + updatedClass.getName() + "'!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/classes/" + id;
    }

    // Update class status
    @PostMapping("/{id}/status")
    public String updateClassStatus(@PathVariable Long id, 
                                   @RequestParam String status, 
                                   RedirectAttributes redirectAttributes) {
        try {
            GymClass.ClassStatus classStatus = GymClass.ClassStatus.valueOf(status.toUpperCase());
            GymClass updatedClass = gymClassService.updateClassStatus(id, classStatus);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Class status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Failed to update class status: " + e.getMessage());
        }
        
        return "redirect:/classes/" + id;
    }

    // Get classes by status (AJAX endpoint)
    @GetMapping("/status/{status}")
    @ResponseBody
    public List<GymClass> getClassesByStatus(@PathVariable String status) {
        try {
            GymClass.ClassStatus classStatus = GymClass.ClassStatus.valueOf(status.toUpperCase());
            return gymClassService.getClassesByStatus(classStatus);
        } catch (IllegalArgumentException e) {
            log.error("Invalid class status: {}", status);
            return List.of();
        }
    }

    // Get upcoming classes (AJAX endpoint)
    @GetMapping("/upcoming")
    @ResponseBody
    public List<GymClass> getUpcomingClasses() {
        return gymClassService.getUpcomingClasses();
    }

    // Get today's classes (AJAX endpoint)
    @GetMapping("/today")
    @ResponseBody
    public List<GymClass> getTodaysClasses() {
        return gymClassService.getTodaysClasses();
    }

    // Get popular classes (AJAX endpoint)
    @GetMapping("/popular")
    @ResponseBody
    public List<GymClass> getPopularClasses() {
        return gymClassService.getPopularClasses();
    }
}
