package com.example.gym_management_system.service;

import com.example.gym_management_system.entity.Member;
import com.example.gym_management_system.entity.GymClass;
import com.example.gym_management_system.entity.Trainer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final MemberService memberService;
    private final GymClassService gymClassService;
    private final TrainerService trainerService;
    private final UserService userService;

    // Overall Dashboard Analytics
    public DashboardAnalytics getDashboardAnalytics() {
        DashboardAnalytics analytics = new DashboardAnalytics();
        
        // Member Analytics
        MemberService.MemberStats memberStats = memberService.getMemberStats();
        analytics.setTotalMembers(memberStats.getTotalMembers());
        analytics.setActiveMembers(memberStats.getActiveMembers());
        analytics.setPremiumMembers(memberStats.getPremiumMembers());
        analytics.setVipMembers(memberStats.getVipMembers());
        
        // Class Analytics
        GymClassService.ClassStats classStats = gymClassService.getClassStats();
        analytics.setTotalClasses(classStats.getTotalClasses());
        analytics.setScheduledClasses(classStats.getScheduledClasses());
        analytics.setCompletedClasses(classStats.getCompletedClasses());
        analytics.setCancelledClasses(classStats.getCancelledClasses());
        
        // Trainer Analytics
        TrainerService.TrainerStats trainerStats = trainerService.getTrainerStats();
        analytics.setTotalTrainers(trainerStats.getTotalTrainers());
        analytics.setActiveTrainers(trainerStats.getActiveTrainers());
        analytics.setFullTimeTrainers(trainerStats.getFullTimeTrainers());
        analytics.setPartTimeTrainers(trainerStats.getPartTimeTrainers());
        
        // User Analytics
        UserService.UserStats userStats = userService.getUserStats();
        analytics.setTotalUsers(userStats.getTotalUsers());
        analytics.setActiveUsers(userStats.getActiveUsers());
        
        // Revenue Analytics (mock data for now)
        analytics.setMonthlyRevenue(calculateMonthlyRevenue());
        analytics.setYearlyRevenue(calculateYearlyRevenue());
        
        return analytics;
    }

    // Member-specific Analytics
    public MemberAnalytics getMemberAnalytics(String role) {
        MemberAnalytics analytics = new MemberAnalytics();
        
        List<Member> allMembers = memberService.getActiveMembers();
        
        // Membership Type Distribution
        Map<String, Long> membershipTypes = allMembers.stream()
            .collect(Collectors.groupingBy(
                m -> m.getMembershipType().toString(),
                Collectors.counting()
            ));
        analytics.setMembershipTypeDistribution(membershipTypes);
        
        // Gender Distribution
        Map<String, Long> genderDistribution = allMembers.stream()
            .filter(m -> m.getGender() != null)
            .collect(Collectors.groupingBy(
                m -> m.getGender().toString(),
                Collectors.counting()
            ));
        analytics.setGenderDistribution(genderDistribution);
        
        // Age Group Distribution
        Map<String, Long> ageGroups = allMembers.stream()
            .filter(m -> m.getDateOfBirth() != null)
            .collect(Collectors.groupingBy(
                this::getAgeGroup,
                Collectors.counting()
            ));
        analytics.setAgeGroupDistribution(ageGroups);
        
        // Monthly Registrations (last 12 months)
        analytics.setMonthlyRegistrations(getMonthlyRegistrations());
        
        // Progress Analytics
        analytics.setAverageProgress(calculateAverageProgress(allMembers));
        analytics.setTopPerformers(getTopPerformingMembers());
        
        return analytics;
    }

    // Class-specific Analytics
    public ClassAnalytics getClassAnalytics(String role) {
        ClassAnalytics analytics = new ClassAnalytics();
        
        List<GymClass> allClasses = gymClassService.getAllClasses(0, 1000, "createdAt", "desc").getContent();
        
        // Class Type Distribution
        Map<String, Long> classTypes = allClasses.stream()
            .collect(Collectors.groupingBy(
                c -> c.getClassType().toString(),
                Collectors.counting()
            ));
        analytics.setClassTypeDistribution(classTypes);
        
        // Capacity Utilization
        Map<String, Double> capacityUtilization = allClasses.stream()
            .collect(Collectors.groupingBy(
                c -> c.getClassType().toString(),
                Collectors.averagingDouble(GymClass::getCapacityPercentage)
            ));
        analytics.setCapacityUtilization(capacityUtilization);
        
        // Popular Time Slots
        analytics.setPopularTimeSlots(getPopularTimeSlots(allClasses));
        
        // Trainer Performance
        analytics.setTrainerPerformance(getTrainerPerformance());
        
        return analytics;
    }

    // Trainer-specific Analytics
    public TrainerAnalytics getTrainerAnalytics(String role) {
        TrainerAnalytics analytics = new TrainerAnalytics();
        
        List<Trainer> allTrainers = trainerService.getActiveTrainers();
        
        // Specialization Distribution
        Map<String, Long> specializations = new HashMap<>();
        allTrainers.forEach(trainer -> {
            if (trainer.getSpecializations() != null) {
                trainer.getSpecializations().forEach(spec -> 
                    specializations.merge(spec.toString(), 1L, Long::sum)
                );
            }
        });
        analytics.setSpecializationDistribution(specializations);
        
        // Experience Distribution
        Map<String, Long> experienceGroups = allTrainers.stream()
            .collect(Collectors.groupingBy(
                this::getExperienceGroup,
                Collectors.counting()
            ));
        analytics.setExperienceDistribution(experienceGroups);
        
        // Rating Distribution
        analytics.setAverageRating(calculateAverageTrainerRating(allTrainers));
        analytics.setTopRatedTrainers(trainerService.getTopRatedTrainers(10));
        
        // Employment Type Distribution
        Map<String, Long> employmentTypes = allTrainers.stream()
            .collect(Collectors.groupingBy(
                t -> t.getEmploymentType().toString(),
                Collectors.counting()
            ));
        analytics.setEmploymentTypeDistribution(employmentTypes);
        
        return analytics;
    }

    // Financial Analytics
    public FinancialAnalytics getFinancialAnalytics(String role) {
        FinancialAnalytics analytics = new FinancialAnalytics();
        
        // Revenue by membership type
        Map<String, BigDecimal> revenueByType = calculateRevenueByMembershipType();
        analytics.setRevenueByMembershipType(revenueByType);
        
        // Monthly revenue trend
        analytics.setMonthlyRevenueTrend(getMonthlyRevenueTrend());
        
        // Projected revenue
        analytics.setProjectedRevenue(calculateProjectedRevenue());
        
        // Cost analysis (mock data)
        analytics.setOperationalCosts(calculateOperationalCosts());
        
        return analytics;
    }

    // Helper methods
    private String getAgeGroup(Member member) {
        if (member.getDateOfBirth() == null) return "Unknown";
        
        int age = LocalDate.now().getYear() - member.getDateOfBirth().getYear();
        if (age < 18) return "Under 18";
        if (age < 25) return "18-24";
        if (age < 35) return "25-34";
        if (age < 45) return "35-44";
        if (age < 55) return "45-54";
        return "55+";
    }

    private String getExperienceGroup(Trainer trainer) {
        int years = trainer.getExperienceYears();
        if (years < 1) return "Less than 1 year";
        if (years < 3) return "1-2 years";
        if (years < 5) return "3-4 years";
        if (years < 10) return "5-9 years";
        return "10+ years";
    }

    private Map<String, Long> getMonthlyRegistrations() {
        // Mock implementation - in real app, query database
        Map<String, Long> registrations = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            registrations.put(month.getMonth().toString(), (long) (Math.random() * 50 + 10));
        }
        return registrations;
    }

    private double calculateAverageProgress(List<Member> members) {
        return members.stream()
            .mapToDouble(Member::getProgressPercentage)
            .average()
            .orElse(0.0);
    }

    private List<Member> getTopPerformingMembers() {
        return memberService.getActiveMembers().stream()
            .sorted((m1, m2) -> Double.compare(m2.getProgressPercentage(), m1.getProgressPercentage()))
            .limit(10)
            .collect(Collectors.toList());
    }

    private Map<String, Long> getPopularTimeSlots(List<GymClass> classes) {
        return classes.stream()
            .collect(Collectors.groupingBy(
                c -> getTimeSlot(c.getStartTime().getHour()),
                Collectors.counting()
            ));
    }

    private String getTimeSlot(int hour) {
        if (hour < 6) return "Early Morning (5-6 AM)";
        if (hour < 9) return "Morning (6-9 AM)";
        if (hour < 12) return "Late Morning (9-12 PM)";
        if (hour < 15) return "Afternoon (12-3 PM)";
        if (hour < 18) return "Late Afternoon (3-6 PM)";
        if (hour < 21) return "Evening (6-9 PM)";
        return "Night (9+ PM)";
    }

    private Map<String, Double> getTrainerPerformance() {
        return trainerService.getActiveTrainers().stream()
            .collect(Collectors.toMap(
                Trainer::getFullName,
                t -> t.getAverageRating().doubleValue()
            ));
    }

    private double calculateAverageTrainerRating(List<Trainer> trainers) {
        return trainers.stream()
            .filter(t -> t.getTotalRatings() > 0)
            .mapToDouble(t -> t.getAverageRating().doubleValue())
            .average()
            .orElse(0.0);
    }

    private BigDecimal calculateMonthlyRevenue() {
        // Mock calculation - in real app, sum actual payments
        List<Member> activeMembers = memberService.getActiveMembers();
        return activeMembers.stream()
            .filter(m -> m.getMonthlyFee() != null)
            .map(Member::getMonthlyFee)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateYearlyRevenue() {
        return calculateMonthlyRevenue().multiply(BigDecimal.valueOf(12));
    }

    private Map<String, BigDecimal> calculateRevenueByMembershipType() {
        Map<String, BigDecimal> revenue = new HashMap<>();
        List<Member> activeMembers = memberService.getActiveMembers();
        
        for (Member member : activeMembers) {
            if (member.getMonthlyFee() != null) {
                String type = member.getMembershipType().toString();
                revenue.merge(type, member.getMonthlyFee(), BigDecimal::add);
            }
        }
        
        return revenue;
    }

    private Map<String, BigDecimal> getMonthlyRevenueTrend() {
        // Mock implementation
        Map<String, BigDecimal> trend = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        BigDecimal baseRevenue = calculateMonthlyRevenue();
        
        for (int i = 11; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            BigDecimal monthlyRevenue = baseRevenue.multiply(
                BigDecimal.valueOf(0.8 + Math.random() * 0.4)
            );
            trend.put(month.getMonth().toString(), monthlyRevenue);
        }
        
        return trend;
    }

    private BigDecimal calculateProjectedRevenue() {
        return calculateMonthlyRevenue().multiply(BigDecimal.valueOf(1.15)); // 15% growth projection
    }

    private Map<String, BigDecimal> calculateOperationalCosts() {
        Map<String, BigDecimal> costs = new HashMap<>();
        costs.put("Staff Salaries", BigDecimal.valueOf(50000));
        costs.put("Equipment Maintenance", BigDecimal.valueOf(5000));
        costs.put("Utilities", BigDecimal.valueOf(8000));
        costs.put("Rent", BigDecimal.valueOf(15000));
        costs.put("Marketing", BigDecimal.valueOf(3000));
        costs.put("Insurance", BigDecimal.valueOf(2000));
        return costs;
    }

    // Analytics Data Classes
    public static class DashboardAnalytics {
        private long totalMembers, activeMembers, premiumMembers, vipMembers;
        private long totalClasses, scheduledClasses, completedClasses, cancelledClasses;
        private long totalTrainers, activeTrainers, fullTimeTrainers, partTimeTrainers;
        private long totalUsers, activeUsers;
        private BigDecimal monthlyRevenue, yearlyRevenue;

        // Getters and setters
        public long getTotalMembers() { return totalMembers; }
        public void setTotalMembers(long totalMembers) { this.totalMembers = totalMembers; }
        public long getActiveMembers() { return activeMembers; }
        public void setActiveMembers(long activeMembers) { this.activeMembers = activeMembers; }
        public long getPremiumMembers() { return premiumMembers; }
        public void setPremiumMembers(long premiumMembers) { this.premiumMembers = premiumMembers; }
        public long getVipMembers() { return vipMembers; }
        public void setVipMembers(long vipMembers) { this.vipMembers = vipMembers; }
        public long getTotalClasses() { return totalClasses; }
        public void setTotalClasses(long totalClasses) { this.totalClasses = totalClasses; }
        public long getScheduledClasses() { return scheduledClasses; }
        public void setScheduledClasses(long scheduledClasses) { this.scheduledClasses = scheduledClasses; }
        public long getCompletedClasses() { return completedClasses; }
        public void setCompletedClasses(long completedClasses) { this.completedClasses = completedClasses; }
        public long getCancelledClasses() { return cancelledClasses; }
        public void setCancelledClasses(long cancelledClasses) { this.cancelledClasses = cancelledClasses; }
        public long getTotalTrainers() { return totalTrainers; }
        public void setTotalTrainers(long totalTrainers) { this.totalTrainers = totalTrainers; }
        public long getActiveTrainers() { return activeTrainers; }
        public void setActiveTrainers(long activeTrainers) { this.activeTrainers = activeTrainers; }
        public long getFullTimeTrainers() { return fullTimeTrainers; }
        public void setFullTimeTrainers(long fullTimeTrainers) { this.fullTimeTrainers = fullTimeTrainers; }
        public long getPartTimeTrainers() { return partTimeTrainers; }
        public void setPartTimeTrainers(long partTimeTrainers) { this.partTimeTrainers = partTimeTrainers; }
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        public long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }
        public BigDecimal getMonthlyRevenue() { return monthlyRevenue; }
        public void setMonthlyRevenue(BigDecimal monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }
        public BigDecimal getYearlyRevenue() { return yearlyRevenue; }
        public void setYearlyRevenue(BigDecimal yearlyRevenue) { this.yearlyRevenue = yearlyRevenue; }
    }

    public static class MemberAnalytics {
        private Map<String, Long> membershipTypeDistribution;
        private Map<String, Long> genderDistribution;
        private Map<String, Long> ageGroupDistribution;
        private Map<String, Long> monthlyRegistrations;
        private double averageProgress;
        private List<Member> topPerformers;

        // Getters and setters
        public Map<String, Long> getMembershipTypeDistribution() { return membershipTypeDistribution; }
        public void setMembershipTypeDistribution(Map<String, Long> membershipTypeDistribution) { this.membershipTypeDistribution = membershipTypeDistribution; }
        public Map<String, Long> getGenderDistribution() { return genderDistribution; }
        public void setGenderDistribution(Map<String, Long> genderDistribution) { this.genderDistribution = genderDistribution; }
        public Map<String, Long> getAgeGroupDistribution() { return ageGroupDistribution; }
        public void setAgeGroupDistribution(Map<String, Long> ageGroupDistribution) { this.ageGroupDistribution = ageGroupDistribution; }
        public Map<String, Long> getMonthlyRegistrations() { return monthlyRegistrations; }
        public void setMonthlyRegistrations(Map<String, Long> monthlyRegistrations) { this.monthlyRegistrations = monthlyRegistrations; }
        public double getAverageProgress() { return averageProgress; }
        public void setAverageProgress(double averageProgress) { this.averageProgress = averageProgress; }
        public List<Member> getTopPerformers() { return topPerformers; }
        public void setTopPerformers(List<Member> topPerformers) { this.topPerformers = topPerformers; }
    }

    public static class ClassAnalytics {
        private Map<String, Long> classTypeDistribution;
        private Map<String, Double> capacityUtilization;
        private Map<String, Long> popularTimeSlots;
        private Map<String, Double> trainerPerformance;

        // Getters and setters
        public Map<String, Long> getClassTypeDistribution() { return classTypeDistribution; }
        public void setClassTypeDistribution(Map<String, Long> classTypeDistribution) { this.classTypeDistribution = classTypeDistribution; }
        public Map<String, Double> getCapacityUtilization() { return capacityUtilization; }
        public void setCapacityUtilization(Map<String, Double> capacityUtilization) { this.capacityUtilization = capacityUtilization; }
        public Map<String, Long> getPopularTimeSlots() { return popularTimeSlots; }
        public void setPopularTimeSlots(Map<String, Long> popularTimeSlots) { this.popularTimeSlots = popularTimeSlots; }
        public Map<String, Double> getTrainerPerformance() { return trainerPerformance; }
        public void setTrainerPerformance(Map<String, Double> trainerPerformance) { this.trainerPerformance = trainerPerformance; }
    }

    public static class TrainerAnalytics {
        private Map<String, Long> specializationDistribution;
        private Map<String, Long> experienceDistribution;
        private Map<String, Long> employmentTypeDistribution;
        private double averageRating;
        private List<Trainer> topRatedTrainers;

        // Getters and setters
        public Map<String, Long> getSpecializationDistribution() { return specializationDistribution; }
        public void setSpecializationDistribution(Map<String, Long> specializationDistribution) { this.specializationDistribution = specializationDistribution; }
        public Map<String, Long> getExperienceDistribution() { return experienceDistribution; }
        public void setExperienceDistribution(Map<String, Long> experienceDistribution) { this.experienceDistribution = experienceDistribution; }
        public Map<String, Long> getEmploymentTypeDistribution() { return employmentTypeDistribution; }
        public void setEmploymentTypeDistribution(Map<String, Long> employmentTypeDistribution) { this.employmentTypeDistribution = employmentTypeDistribution; }
        public double getAverageRating() { return averageRating; }
        public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
        public List<Trainer> getTopRatedTrainers() { return topRatedTrainers; }
        public void setTopRatedTrainers(List<Trainer> topRatedTrainers) { this.topRatedTrainers = topRatedTrainers; }
    }

    public static class FinancialAnalytics {
        private Map<String, BigDecimal> revenueByMembershipType;
        private Map<String, BigDecimal> monthlyRevenueTrend;
        private Map<String, BigDecimal> operationalCosts;
        private BigDecimal projectedRevenue;

        // Getters and setters
        public Map<String, BigDecimal> getRevenueByMembershipType() { return revenueByMembershipType; }
        public void setRevenueByMembershipType(Map<String, BigDecimal> revenueByMembershipType) { this.revenueByMembershipType = revenueByMembershipType; }
        public Map<String, BigDecimal> getMonthlyRevenueTrend() { return monthlyRevenueTrend; }
        public void setMonthlyRevenueTrend(Map<String, BigDecimal> monthlyRevenueTrend) { this.monthlyRevenueTrend = monthlyRevenueTrend; }
        public Map<String, BigDecimal> getOperationalCosts() { return operationalCosts; }
        public void setOperationalCosts(Map<String, BigDecimal> operationalCosts) { this.operationalCosts = operationalCosts; }
        public BigDecimal getProjectedRevenue() { return projectedRevenue; }
        public void setProjectedRevenue(BigDecimal projectedRevenue) { this.projectedRevenue = projectedRevenue; }
    }
}
