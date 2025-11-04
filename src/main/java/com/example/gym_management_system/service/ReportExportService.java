package com.example.gym_management_system.service;

import com.example.gym_management_system.entity.Member;
import com.example.gym_management_system.entity.GymClass;
import com.example.gym_management_system.entity.Trainer;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportExportService {

    private final AnalyticsService analyticsService;
    private final MemberService memberService;
    private final GymClassService gymClassService;
    private final TrainerService trainerService;

    // Generate comprehensive PDF report
    public byte[] generatePDFReport(String reportType, String userRole) {
        try {
            String htmlContent = generateHTMLReport(reportType, userRole);
            return convertHTMLToPDF(htmlContent);
        } catch (Exception e) {
            log.error("Error generating PDF report: {}", e.getMessage());
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    // Generate CSV export
    public String generateCSVReport(String reportType, String userRole) {
        try {
            StringWriter stringWriter = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(stringWriter);

            switch (reportType.toLowerCase()) {
                case "members" -> exportMembersToCSV(csvWriter, userRole);
                case "classes" -> exportClassesToCSV(csvWriter, userRole);
                case "trainers" -> exportTrainersToCSV(csvWriter, userRole);
                case "analytics" -> exportAnalyticsToCSV(csvWriter, userRole);
                case "financial" -> exportFinancialToCSV(csvWriter, userRole);
                default -> throw new IllegalArgumentException("Invalid report type: " + reportType);
            }

            csvWriter.close();
            return stringWriter.toString();
        } catch (Exception e) {
            log.error("Error generating CSV report: {}", e.getMessage());
            throw new RuntimeException("Failed to generate CSV report", e);
        }
    }

    // Generate Excel export
    public byte[] generateExcelReport(String reportType, String userRole) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            switch (reportType.toLowerCase()) {
                case "members" -> exportMembersToExcel(workbook, userRole);
                case "classes" -> exportClassesToExcel(workbook, userRole);
                case "trainers" -> exportTrainersToExcel(workbook, userRole);
                case "analytics" -> exportAnalyticsToExcel(workbook, userRole);
                case "comprehensive" -> exportComprehensiveToExcel(workbook, userRole);
                default -> throw new IllegalArgumentException("Invalid report type: " + reportType);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error generating Excel report: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    private String generateHTMLReport(String reportType, String userRole) {
        StringBuilder html = new StringBuilder();
        
        // HTML Header with styling
        html.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>FitHub Analytics Report</title>
                <style>
                    body { font-family: 'Arial', sans-serif; margin: 20px; color: #333; }
                    .header { background: linear-gradient(135deg, #3b82f6, #8b5cf6); color: white; padding: 20px; border-radius: 10px; margin-bottom: 20px; }
                    .logo { font-size: 28px; font-weight: bold; margin-bottom: 5px; }
                    .subtitle { font-size: 14px; opacity: 0.9; }
                    .section { background: white; border: 1px solid #e5e7eb; border-radius: 8px; padding: 20px; margin-bottom: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    .section-title { font-size: 18px; font-weight: bold; color: #1f2937; margin-bottom: 15px; border-bottom: 2px solid #3b82f6; padding-bottom: 5px; }
                    .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; margin-bottom: 20px; }
                    .stat-card { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 15px; text-align: center; }
                    .stat-number { font-size: 24px; font-weight: bold; color: #3b82f6; }
                    .stat-label { font-size: 12px; color: #64748b; margin-top: 5px; }
                    table { width: 100%; border-collapse: collapse; margin-top: 10px; }
                    th, td { border: 1px solid #e5e7eb; padding: 12px; text-align: left; }
                    th { background: #f8fafc; font-weight: bold; color: #374151; }
                    tr:nth-child(even) { background: #f9fafb; }
                    .chart-placeholder { background: #f3f4f6; border: 2px dashed #d1d5db; border-radius: 8px; padding: 40px; text-align: center; color: #6b7280; margin: 15px 0; }
                    .footer { text-align: center; color: #6b7280; font-size: 12px; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="logo">🏋️ FitHub Analytics Report</div>
                    <div class="subtitle">Generated on """ + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a")) + """
                     | Role: """ + userRole + """
                    </div>
                </div>
            """);

        // Generate content based on report type
        switch (reportType.toLowerCase()) {
            case "dashboard" -> html.append(generateDashboardHTML(userRole));
            case "members" -> html.append(generateMembersHTML(userRole));
            case "classes" -> html.append(generateClassesHTML(userRole));
            case "trainers" -> html.append(generateTrainersHTML(userRole));
            case "financial" -> html.append(generateFinancialHTML(userRole));
            default -> html.append(generateComprehensiveHTML(userRole));
        }

        // HTML Footer
        html.append("""
                <div class="footer">
                    <p>© 2024 FitHub Premium Gym Management System</p>
                    <p>This report contains confidential business information</p>
                </div>
            </body>
            </html>
            """);

        return html.toString();
    }

    private String generateDashboardHTML(String userRole) {
        AnalyticsService.DashboardAnalytics analytics = analyticsService.getDashboardAnalytics();
        
        return String.format("""
            <div class="section">
                <div class="section-title">📊 Dashboard Overview</div>
                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-number">%d</div>
                        <div class="stat-label">Total Members</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-number">%d</div>
                        <div class="stat-label">Active Members</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-number">%d</div>
                        <div class="stat-label">Total Classes</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-number">%d</div>
                        <div class="stat-label">Active Trainers</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-number">$%s</div>
                        <div class="stat-label">Monthly Revenue</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-number">$%s</div>
                        <div class="stat-label">Yearly Revenue</div>
                    </div>
                </div>
                <div class="chart-placeholder">
                    📈 Revenue Trend Chart<br>
                    <small>Visual charts would be rendered here in the actual implementation</small>
                </div>
            </div>
            """, 
            analytics.getTotalMembers(), analytics.getActiveMembers(), 
            analytics.getTotalClasses(), analytics.getActiveTrainers(),
            analytics.getMonthlyRevenue(), analytics.getYearlyRevenue()
        );
    }

    private String generateMembersHTML(String userRole) {
        AnalyticsService.MemberAnalytics analytics = analyticsService.getMemberAnalytics(userRole);
        List<Member> recentMembers = memberService.getRecentMembers(10);
        
        StringBuilder html = new StringBuilder();
        html.append("""
            <div class="section">
                <div class="section-title">👥 Member Analytics</div>
                <div class="stats-grid">
            """);
        
        // Membership type distribution
        if (analytics.getMembershipTypeDistribution() != null) {
            analytics.getMembershipTypeDistribution().forEach((type, count) -> 
                html.append(String.format("""
                    <div class="stat-card">
                        <div class="stat-number">%d</div>
                        <div class="stat-label">%s Members</div>
                    </div>
                    """, count, type))
            );
        }
        
        html.append("</div>");
        
        // Recent members table
        html.append("""
            <h3>Recent Member Registrations</h3>
            <table>
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Membership Type</th>
                        <th>Join Date</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
            """);
        
        for (Member member : recentMembers) {
            html.append(String.format("""
                <tr>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                </tr>
                """, 
                member.getFullName(), member.getEmail(), 
                member.getMembershipType(), member.getMembershipStartDate(),
                member.getStatus()
            ));
        }
        
        html.append("</tbody></table></div>");
        return html.toString();
    }

    private String generateClassesHTML(String userRole) {
        AnalyticsService.ClassAnalytics analytics = analyticsService.getClassAnalytics(userRole);
        List<GymClass> upcomingClasses = gymClassService.getUpcomingClasses().stream().limit(10).toList();
        
        StringBuilder html = new StringBuilder();
        html.append("""
            <div class="section">
                <div class="section-title">🏃‍♀️ Class Analytics</div>
                <div class="chart-placeholder">
                    📊 Class Type Distribution Chart
                </div>
                <h3>Upcoming Classes</h3>
                <table>
                    <thead>
                        <tr>
                            <th>Class Name</th>
                            <th>Type</th>
                            <th>Instructor</th>
                            <th>Date</th>
                            <th>Time</th>
                            <th>Capacity</th>
                        </tr>
                    </thead>
                    <tbody>
            """);
        
        for (GymClass gymClass : upcomingClasses) {
            html.append(String.format("""
                <tr>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%d/%d (%.1f%%)</td>
                </tr>
                """, 
                gymClass.getName(), gymClass.getClassType(), gymClass.getInstructor(),
                gymClass.getClassDate(), gymClass.getTimeRange(),
                gymClass.getCurrentEnrollment(), gymClass.getMaxCapacity(),
                gymClass.getCapacityPercentage()
            ));
        }
        
        html.append("</tbody></table></div>");
        return html.toString();
    }

    private String generateTrainersHTML(String userRole) {
        List<Trainer> topTrainers = trainerService.getTopRatedTrainers(10);
        
        StringBuilder html = new StringBuilder();
        html.append("""
            <div class="section">
                <div class="section-title">💪 Trainer Performance</div>
                <h3>Top Rated Trainers</h3>
                <table>
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Specializations</th>
                            <th>Experience</th>
                            <th>Rating</th>
                            <th>Total Classes</th>
                            <th>Employment Type</th>
                        </tr>
                    </thead>
                    <tbody>
            """);
        
        for (Trainer trainer : topTrainers) {
            html.append(String.format("""
                <tr>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%d years</td>
                    <td>⭐ %.1f</td>
                    <td>%d</td>
                    <td>%s</td>
                </tr>
                """, 
                trainer.getFullName(), trainer.getSpecializationsString(),
                trainer.getExperienceYears(), trainer.getAverageRating().doubleValue(),
                trainer.getTotalClasses(), trainer.getEmploymentType()
            ));
        }
        
        html.append("</tbody></table></div>");
        return html.toString();
    }

    private String generateFinancialHTML(String userRole) {
        AnalyticsService.FinancialAnalytics analytics = analyticsService.getFinancialAnalytics(userRole);
        
        StringBuilder html = new StringBuilder();
        html.append("""
            <div class="section">
                <div class="section-title">💰 Financial Analytics</div>
                <div class="chart-placeholder">
                    📈 Revenue Trend Chart
                </div>
                <h3>Revenue by Membership Type</h3>
                <table>
                    <thead>
                        <tr>
                            <th>Membership Type</th>
                            <th>Monthly Revenue</th>
                        </tr>
                    </thead>
                    <tbody>
            """);
        
        if (analytics.getRevenueByMembershipType() != null) {
            analytics.getRevenueByMembershipType().forEach((type, revenue) -> 
                html.append(String.format("""
                    <tr>
                        <td>%s</td>
                        <td>$%s</td>
                    </tr>
                    """, type, revenue))
            );
        }
        
        html.append("</tbody></table></div>");
        return html.toString();
    }

    private String generateComprehensiveHTML(String userRole) {
        return generateDashboardHTML(userRole) + 
               generateMembersHTML(userRole) + 
               generateClassesHTML(userRole) + 
               generateTrainersHTML(userRole) + 
               generateFinancialHTML(userRole);
    }

    private byte[] convertHTMLToPDF(String htmlContent) {
        // For now, return HTML as bytes - in production, use iText HTML2PDF
        // This is a simplified implementation
        return htmlContent.getBytes();
    }

    // CSV Export Methods
    private void exportMembersToCSV(CSVWriter writer, String userRole) {
        // Header
        writer.writeNext(new String[]{"Name", "Email", "Phone", "Membership Type", "Status", "Join Date", "Progress"});
        
        // Data
        List<Member> members = memberService.getActiveMembers();
        for (Member member : members) {
            writer.writeNext(new String[]{
                member.getFullName(),
                member.getEmail(),
                member.getPhone() != null ? member.getPhone() : "",
                member.getMembershipType().toString(),
                member.getStatus().toString(),
                member.getMembershipStartDate().toString(),
                member.getProgressPercentage() + "%"
            });
        }
    }

    private void exportClassesToCSV(CSVWriter writer, String userRole) {
        writer.writeNext(new String[]{"Class Name", "Type", "Instructor", "Date", "Time", "Capacity", "Status"});
        
        List<GymClass> classes = gymClassService.getAllClasses(0, 1000, "classDate", "asc").getContent();
        for (GymClass gymClass : classes) {
            writer.writeNext(new String[]{
                gymClass.getName(),
                gymClass.getClassType().toString(),
                gymClass.getInstructor(),
                gymClass.getClassDate().toString(),
                gymClass.getTimeRange(),
                gymClass.getCurrentEnrollment() + "/" + gymClass.getMaxCapacity(),
                gymClass.getStatus().toString()
            });
        }
    }

    private void exportTrainersToCSV(CSVWriter writer, String userRole) {
        writer.writeNext(new String[]{"Name", "Email", "Specializations", "Experience", "Rating", "Employment Type"});
        
        List<Trainer> trainers = trainerService.getActiveTrainers();
        for (Trainer trainer : trainers) {
            writer.writeNext(new String[]{
                trainer.getFullName(),
                trainer.getEmail() != null ? trainer.getEmail() : "",
                trainer.getSpecializationsString(),
                trainer.getExperienceYears() + " years",
                trainer.getAverageRating().toString(),
                trainer.getEmploymentType().toString()
            });
        }
    }

    private void exportAnalyticsToCSV(CSVWriter writer, String userRole) {
        AnalyticsService.DashboardAnalytics analytics = analyticsService.getDashboardAnalytics();
        
        writer.writeNext(new String[]{"Metric", "Value"});
        writer.writeNext(new String[]{"Total Members", String.valueOf(analytics.getTotalMembers())});
        writer.writeNext(new String[]{"Active Members", String.valueOf(analytics.getActiveMembers())});
        writer.writeNext(new String[]{"Total Classes", String.valueOf(analytics.getTotalClasses())});
        writer.writeNext(new String[]{"Active Trainers", String.valueOf(analytics.getActiveTrainers())});
        writer.writeNext(new String[]{"Monthly Revenue", analytics.getMonthlyRevenue().toString()});
        writer.writeNext(new String[]{"Yearly Revenue", analytics.getYearlyRevenue().toString()});
    }

    private void exportFinancialToCSV(CSVWriter writer, String userRole) {
        AnalyticsService.FinancialAnalytics analytics = analyticsService.getFinancialAnalytics(userRole);
        
        writer.writeNext(new String[]{"Revenue Type", "Amount"});
        if (analytics.getRevenueByMembershipType() != null) {
            analytics.getRevenueByMembershipType().forEach((type, amount) -> 
                writer.writeNext(new String[]{type, amount.toString()})
            );
        }
    }

    // Excel Export Methods
    private void exportMembersToExcel(Workbook workbook, String userRole) {
        Sheet sheet = workbook.createSheet("Members");
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Name", "Email", "Phone", "Membership Type", "Status", "Join Date", "Progress"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            
            // Style header
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            cell.setCellStyle(headerStyle);
        }
        
        // Add data rows
        List<Member> members = memberService.getActiveMembers();
        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            Row row = sheet.createRow(i + 1);
            
            row.createCell(0).setCellValue(member.getFullName());
            row.createCell(1).setCellValue(member.getEmail());
            row.createCell(2).setCellValue(member.getPhone() != null ? member.getPhone() : "");
            row.createCell(3).setCellValue(member.getMembershipType().toString());
            row.createCell(4).setCellValue(member.getStatus().toString());
            row.createCell(5).setCellValue(member.getMembershipStartDate().toString());
            row.createCell(6).setCellValue(member.getProgressPercentage() + "%");
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void exportClassesToExcel(Workbook workbook, String userRole) {
        Sheet sheet = workbook.createSheet("Classes");
        
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Class Name", "Type", "Instructor", "Date", "Time", "Capacity", "Status"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        
        List<GymClass> classes = gymClassService.getAllClasses(0, 1000, "classDate", "asc").getContent();
        for (int i = 0; i < classes.size(); i++) {
            GymClass gymClass = classes.get(i);
            Row row = sheet.createRow(i + 1);
            
            row.createCell(0).setCellValue(gymClass.getName());
            row.createCell(1).setCellValue(gymClass.getClassType().toString());
            row.createCell(2).setCellValue(gymClass.getInstructor());
            row.createCell(3).setCellValue(gymClass.getClassDate().toString());
            row.createCell(4).setCellValue(gymClass.getTimeRange());
            row.createCell(5).setCellValue(gymClass.getCurrentEnrollment() + "/" + gymClass.getMaxCapacity());
            row.createCell(6).setCellValue(gymClass.getStatus().toString());
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void exportTrainersToExcel(Workbook workbook, String userRole) {
        Sheet sheet = workbook.createSheet("Trainers");
        
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Name", "Email", "Specializations", "Experience", "Rating", "Employment Type"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        
        List<Trainer> trainers = trainerService.getActiveTrainers();
        for (int i = 0; i < trainers.size(); i++) {
            Trainer trainer = trainers.get(i);
            Row row = sheet.createRow(i + 1);
            
            row.createCell(0).setCellValue(trainer.getFullName());
            row.createCell(1).setCellValue(trainer.getEmail() != null ? trainer.getEmail() : "");
            row.createCell(2).setCellValue(trainer.getSpecializationsString());
            row.createCell(3).setCellValue(trainer.getExperienceYears() + " years");
            row.createCell(4).setCellValue(trainer.getAverageRating().doubleValue());
            row.createCell(5).setCellValue(trainer.getEmploymentType().toString());
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void exportAnalyticsToExcel(Workbook workbook, String userRole) {
        Sheet sheet = workbook.createSheet("Analytics");
        AnalyticsService.DashboardAnalytics analytics = analyticsService.getDashboardAnalytics();
        
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Metric");
        headerRow.createCell(1).setCellValue("Value");
        
        int rowNum = 1;
        sheet.createRow(rowNum++).createCell(0).setCellValue("Total Members");
        sheet.getRow(rowNum-1).createCell(1).setCellValue(analytics.getTotalMembers());
        
        sheet.createRow(rowNum++).createCell(0).setCellValue("Active Members");
        sheet.getRow(rowNum-1).createCell(1).setCellValue(analytics.getActiveMembers());
        
        sheet.createRow(rowNum++).createCell(0).setCellValue("Total Classes");
        sheet.getRow(rowNum-1).createCell(1).setCellValue(analytics.getTotalClasses());
        
        sheet.createRow(rowNum++).createCell(0).setCellValue("Active Trainers");
        sheet.getRow(rowNum-1).createCell(1).setCellValue(analytics.getActiveTrainers());
        
        sheet.createRow(rowNum++).createCell(0).setCellValue("Monthly Revenue");
        sheet.getRow(rowNum-1).createCell(1).setCellValue(analytics.getMonthlyRevenue().doubleValue());
        
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void exportComprehensiveToExcel(Workbook workbook, String userRole) {
        exportMembersToExcel(workbook, userRole);
        exportClassesToExcel(workbook, userRole);
        exportTrainersToExcel(workbook, userRole);
        exportAnalyticsToExcel(workbook, userRole);
    }
}
