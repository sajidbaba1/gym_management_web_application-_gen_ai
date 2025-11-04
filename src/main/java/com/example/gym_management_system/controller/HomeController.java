package com.example.gym_management_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "FitHub - Premium Gym Management");
        return "index";
    }

    @GetMapping("/home")
    public String homeAlias(Model model) {
        return home(model);
    }
}
