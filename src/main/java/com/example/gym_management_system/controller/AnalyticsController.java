package com.example.gym_management_system.controller;

import com.example.gym_management_system.service.AnalyticsService;
import com.example.gym_management_system.service.ReportExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final ReportExportService reportExportService;

    // Main Analytics Dashboard
    @GetMapping
    public String analyticsHome(@RequestParam(defaultValue = "ADMIN") String role, Model model) {
        try {
            // Get comprehensive analytics based on role
            AnalyticsService.DashboardAnalytics dashboardAnalytics = analyticsService.getDashboardAnalytics();
            AnalyticsService.MemberAnalytics memberAnalytics = analyticsService.getMemberAnalytics(role);
            AnalyticsService.ClassAnalytics classAnalytics = analyticsService.getClassAnalytics(role);
            AnalyticsService.TrainerAnalytics trainerAnalytics = analyticsService.getTrainerAnalytics(role);
            AnalyticsService.FinancialAnalytics financialAnalytics = analyticsService.getFinancialAnalytics(role);

            model.addAttribute("dashboardAnalytics", dashboardAnalytics);
            model.addAttribute("memberAnalytics", memberAnalytics);
            model.addAttribute("classAnalytics", classAnalytics);
            model.addAttribute("trainerAnalytics", trainerAnalytics);
            model.addAttribute("financialAnalytics", financialAnalytics);
            model.addAttribute("userRole", role);
            model.addAttribute("pageTitle", "Analytics Dashboard - FitHub");

            return "analytics/dashboard";
        } catch (Exception e) {
            log.error("Error loading analytics dashboard: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error loading analytics data");
            return "analytics/dashboard";
        }
    }

    // Member Analytics
    @GetMapping("/members")
    public String memberAnalytics(@RequestParam(defaultValue = "ADMIN") String role, Model model) {
        try {
            AnalyticsService.MemberAnalytics analytics = analyticsService.getMemberAnalytics(role);
            model.addAttribute("analytics", analytics);
            model.addAttribute("userRole", role);
            model.addAttribute("pageTitle", "Member Analytics - FitHub");
            return "analytics/members";
        } catch (Exception e) {
            log.error("Error loading member analytics: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error loading member analytics");
            return "analytics/members";
        }
    }

    // Class Analytics
    @GetMapping("/classes")
    public String classAnalytics(@RequestParam(defaultValue = "ADMIN") String role, Model model) {
        try {
            AnalyticsService.ClassAnalytics analytics = analyticsService.getClassAnalytics(role);
            model.addAttribute("analytics", analytics);
            model.addAttribute("userRole", role);
            model.addAttribute("pageTitle", "Class Analytics - FitHub");
            return "analytics/classes";
        } catch (Exception e) {
            log.error("Error loading class analytics: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error loading class analytics");
            return "analytics/classes";
        }
    }

    // Trainer Analytics
    @GetMapping("/trainers")
    public String trainerAnalytics(@RequestParam(defaultValue = "ADMIN") String role, Model model) {
        try {
            AnalyticsService.TrainerAnalytics analytics = analyticsService.getTrainerAnalytics(role);
            model.addAttribute("analytics", analytics);
            model.addAttribute("userRole", role);
            model.addAttribute("pageTitle", "Trainer Analytics - FitHub");
            return "analytics/trainers";
        } catch (Exception e) {
            log.error("Error loading trainer analytics: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error loading trainer analytics");
            return "analytics/trainers";
        }
    }

    // Financial Analytics
    @GetMapping("/financial")
    public String financialAnalytics(@RequestParam(defaultValue = "ADMIN") String role, Model model) {
        try {
            AnalyticsService.FinancialAnalytics analytics = analyticsService.getFinancialAnalytics(role);
            model.addAttribute("analytics", analytics);
            model.addAttribute("userRole", role);
            model.addAttribute("pageTitle", "Financial Analytics - FitHub");
            return "analytics/financial";
        } catch (Exception e) {
            log.error("Error loading financial analytics: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error loading financial analytics");
            return "analytics/financial";
        }
    }

    // Export Reports - PDF
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPDF(
            @RequestParam(defaultValue = "comprehensive") String reportType,
            @RequestParam(defaultValue = "ADMIN") String role) {
        try {
            byte[] pdfData = reportExportService.generatePDFReport(reportType, role);
            
            String filename = String.format("FitHub_%s_Report_%s.pdf", 
                reportType, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfData.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfData);
                
        } catch (Exception e) {
            log.error("Error generating PDF report: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Export Reports - CSV
    @GetMapping("/export/csv")
    public ResponseEntity<String> exportCSV(
            @RequestParam(defaultValue = "members") String reportType,
            @RequestParam(defaultValue = "ADMIN") String role) {
        try {
            String csvData = reportExportService.generateCSVReport(reportType, role);
            
            String filename = String.format("FitHub_%s_Report_%s.csv", 
                reportType, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
                
        } catch (Exception e) {
            log.error("Error generating CSV report: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Export Reports - Excel
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(defaultValue = "comprehensive") String reportType,
            @RequestParam(defaultValue = "ADMIN") String role) {
        try {
            byte[] excelData = reportExportService.generateExcelReport(reportType, role);
            
            String filename = String.format("FitHub_%s_Report_%s.xlsx", 
                reportType, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelData.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
                
        } catch (Exception e) {
            log.error("Error generating Excel report: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // API Endpoints for AJAX requests
    @GetMapping("/api/dashboard")
    @ResponseBody
    public AnalyticsService.DashboardAnalytics getDashboardAnalyticsAPI(@RequestParam(defaultValue = "ADMIN") String role) {
        return analyticsService.getDashboardAnalytics();
    }

    @GetMapping("/api/members")
    @ResponseBody
    public AnalyticsService.MemberAnalytics getMemberAnalyticsAPI(@RequestParam(defaultValue = "ADMIN") String role) {
        return analyticsService.getMemberAnalytics(role);
    }

    @GetMapping("/api/classes")
    @ResponseBody
    public AnalyticsService.ClassAnalytics getClassAnalyticsAPI(@RequestParam(defaultValue = "ADMIN") String role) {
        return analyticsService.getClassAnalytics(role);
    }

    @GetMapping("/api/trainers")
    @ResponseBody
    public AnalyticsService.TrainerAnalytics getTrainerAnalyticsAPI(@RequestParam(defaultValue = "ADMIN") String role) {
        return analyticsService.getTrainerAnalytics(role);
    }

    @GetMapping("/api/financial")
    @ResponseBody
    public AnalyticsService.FinancialAnalytics getFinancialAnalyticsAPI(@RequestParam(defaultValue = "ADMIN") String role) {
        return analyticsService.getFinancialAnalytics(role);
    }
}
