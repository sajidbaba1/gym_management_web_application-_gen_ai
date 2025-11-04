package com.example.gym_management_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "trainers")
@NoArgsConstructor
@AllArgsConstructor
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(length = 100)
    private String employeeId;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "trainer_specializations", joinColumns = @JoinColumn(name = "trainer_id"))
    @Column(name = "specialization")
    private List<Specialization> specializations;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "trainer_certifications", joinColumns = @JoinColumn(name = "trainer_id"))
    @Column(name = "certification")
    private List<String> certifications;

    @Column(nullable = false)
    private Integer experienceYears = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmploymentType employmentType = EmploymentType.FULL_TIME;

    @Column(precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(precision = 10, scale = 2)
    private BigDecimal monthlySalary;

    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainerStatus status = TrainerStatus.ACTIVE;

    // Availability
    private LocalTime mondayStart;
    private LocalTime mondayEnd;
    private LocalTime tuesdayStart;
    private LocalTime tuesdayEnd;
    private LocalTime wednesdayStart;
    private LocalTime wednesdayEnd;
    private LocalTime thursdayStart;
    private LocalTime thursdayEnd;
    private LocalTime fridayStart;
    private LocalTime fridayEnd;
    private LocalTime saturdayStart;
    private LocalTime saturdayEnd;
    private LocalTime sundayStart;
    private LocalTime sundayEnd;

    // Performance metrics
    @Column(nullable = false)
    private Integer totalClasses = 0;

    @Column(nullable = false)
    private Integer totalMembers = 0;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer totalRatings = 0;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(length = 500)
    private String profileImageUrl;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Enums
    public enum Specialization {
        PERSONAL_TRAINING("Personal Training"),
        YOGA("Yoga"),
        PILATES("Pilates"),
        CARDIO("Cardio Training"),
        STRENGTH_TRAINING("Strength Training"),
        CROSSFIT("CrossFit"),
        ZUMBA("Zumba"),
        SPINNING("Spinning"),
        BOXING("Boxing"),
        MARTIAL_ARTS("Martial Arts"),
        SWIMMING("Swimming"),
        REHABILITATION("Rehabilitation"),
        NUTRITION("Nutrition Coaching"),
        WEIGHT_LOSS("Weight Loss"),
        MUSCLE_BUILDING("Muscle Building"),
        FLEXIBILITY("Flexibility Training"),
        SPORTS_SPECIFIC("Sports Specific Training");

        private final String displayName;

        Specialization(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum EmploymentType {
        FULL_TIME, PART_TIME, CONTRACT, FREELANCE
    }

    public enum TrainerStatus {
        ACTIVE, INACTIVE, ON_LEAVE, TERMINATED
    }

    // Helper methods
    public String getFullName() {
        return user != null ? user.getFullName() : "Unknown";
    }

    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }

    public String getPhone() {
        return user != null ? user.getPhone() : null;
    }

    public boolean isActive() {
        return status == TrainerStatus.ACTIVE;
    }

    public boolean isAvailableOn(String dayOfWeek) {
        return switch (dayOfWeek.toLowerCase()) {
            case "monday" -> mondayStart != null && mondayEnd != null;
            case "tuesday" -> tuesdayStart != null && tuesdayEnd != null;
            case "wednesday" -> wednesdayStart != null && wednesdayEnd != null;
            case "thursday" -> thursdayStart != null && thursdayEnd != null;
            case "friday" -> fridayStart != null && fridayEnd != null;
            case "saturday" -> saturdayStart != null && saturdayEnd != null;
            case "sunday" -> sundayStart != null && sundayEnd != null;
            default -> false;
        };
    }

    public String getAvailabilityForDay(String dayOfWeek) {
        return switch (dayOfWeek.toLowerCase()) {
            case "monday" -> formatTimeRange(mondayStart, mondayEnd);
            case "tuesday" -> formatTimeRange(tuesdayStart, tuesdayEnd);
            case "wednesday" -> formatTimeRange(wednesdayStart, wednesdayEnd);
            case "thursday" -> formatTimeRange(thursdayStart, thursdayEnd);
            case "friday" -> formatTimeRange(fridayStart, fridayEnd);
            case "saturday" -> formatTimeRange(saturdayStart, saturdayEnd);
            case "sunday" -> formatTimeRange(sundayStart, sundayEnd);
            default -> "Not Available";
        };
    }

    private String formatTimeRange(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return "Not Available";
        }
        return start + " - " + end;
    }

    public BigDecimal getAverageRating() {
        return rating != null ? rating : BigDecimal.ZERO;
    }

    public void updateRating(BigDecimal newRating) {
        if (totalRatings == 0) {
            this.rating = newRating;
            this.totalRatings = 1;
        } else {
            BigDecimal totalScore = rating.multiply(BigDecimal.valueOf(totalRatings));
            totalScore = totalScore.add(newRating);
            this.totalRatings++;
            this.rating = totalScore.divide(BigDecimal.valueOf(totalRatings), 2, RoundingMode.HALF_UP);
        }
    }

    public String getSpecializationsString() {
        if (specializations == null || specializations.isEmpty()) {
            return "No specializations";
        }
        return specializations.stream()
                .map(Specialization::getDisplayName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("No specializations");
    }

    public String getCertificationsString() {
        if (certifications == null || certifications.isEmpty()) {
            return "No certifications";
        }
        return String.join(", ", certifications);
    }

    // Essential getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public List<Specialization> getSpecializations() { return specializations; }
    public void setSpecializations(List<Specialization> specializations) { this.specializations = specializations; }

    public List<String> getCertifications() { return certifications; }
    public void setCertifications(List<String> certifications) { this.certifications = certifications; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    public EmploymentType getEmploymentType() { return employmentType; }
    public void setEmploymentType(EmploymentType employmentType) { this.employmentType = employmentType; }

    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }

    public BigDecimal getMonthlySalary() { return monthlySalary; }
    public void setMonthlySalary(BigDecimal monthlySalary) { this.monthlySalary = monthlySalary; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public TrainerStatus getStatus() { return status; }
    public void setStatus(TrainerStatus status) { this.status = status; }

    public LocalTime getMondayStart() { return mondayStart; }
    public void setMondayStart(LocalTime mondayStart) { this.mondayStart = mondayStart; }
    public LocalTime getMondayEnd() { return mondayEnd; }
    public void setMondayEnd(LocalTime mondayEnd) { this.mondayEnd = mondayEnd; }

    public LocalTime getTuesdayStart() { return tuesdayStart; }
    public void setTuesdayStart(LocalTime tuesdayStart) { this.tuesdayStart = tuesdayStart; }
    public LocalTime getTuesdayEnd() { return tuesdayEnd; }
    public void setTuesdayEnd(LocalTime tuesdayEnd) { this.tuesdayEnd = tuesdayEnd; }

    public LocalTime getWednesdayStart() { return wednesdayStart; }
    public void setWednesdayStart(LocalTime wednesdayStart) { this.wednesdayStart = wednesdayStart; }
    public LocalTime getWednesdayEnd() { return wednesdayEnd; }
    public void setWednesdayEnd(LocalTime wednesdayEnd) { this.wednesdayEnd = wednesdayEnd; }

    public LocalTime getThursdayStart() { return thursdayStart; }
    public void setThursdayStart(LocalTime thursdayStart) { this.thursdayStart = thursdayStart; }
    public LocalTime getThursdayEnd() { return thursdayEnd; }
    public void setThursdayEnd(LocalTime thursdayEnd) { this.thursdayEnd = thursdayEnd; }

    public LocalTime getFridayStart() { return fridayStart; }
    public void setFridayStart(LocalTime fridayStart) { this.fridayStart = fridayStart; }
    public LocalTime getFridayEnd() { return fridayEnd; }
    public void setFridayEnd(LocalTime fridayEnd) { this.fridayEnd = fridayEnd; }

    public LocalTime getSaturdayStart() { return saturdayStart; }
    public void setSaturdayStart(LocalTime saturdayStart) { this.saturdayStart = saturdayStart; }
    public LocalTime getSaturdayEnd() { return saturdayEnd; }
    public void setSaturdayEnd(LocalTime saturdayEnd) { this.saturdayEnd = saturdayEnd; }

    public LocalTime getSundayStart() { return sundayStart; }
    public void setSundayStart(LocalTime sundayStart) { this.sundayStart = sundayStart; }
    public LocalTime getSundayEnd() { return sundayEnd; }
    public void setSundayEnd(LocalTime sundayEnd) { this.sundayEnd = sundayEnd; }

    public Integer getTotalClasses() { return totalClasses; }
    public void setTotalClasses(Integer totalClasses) { this.totalClasses = totalClasses; }

    public Integer getTotalMembers() { return totalMembers; }
    public void setTotalMembers(Integer totalMembers) { this.totalMembers = totalMembers; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public Integer getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Integer totalRatings) { this.totalRatings = totalRatings; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
