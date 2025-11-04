package com.example.gym_management_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "gym_classes")
@NoArgsConstructor
@AllArgsConstructor
public class GymClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ClassType classType;

    @Column(nullable = false)
    private String instructor;

    @Column(nullable = false)
    private LocalDate classDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private Integer duration; // in minutes

    @Column(nullable = false)
    private Integer maxCapacity;

    @Column(nullable = false)
    private Integer currentEnrollment = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClassStatus status = ClassStatus.SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DifficultyLevel difficultyLevel;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 100)
    private String room;

    @Column(length = 500)
    private String equipment;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean isRecurring = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RecurrenceType recurrenceType;

    private LocalDate recurrenceEndDate;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Enums
    public enum ClassType {
        YOGA, PILATES, CARDIO, STRENGTH_TRAINING, CROSSFIT, ZUMBA, 
        SPINNING, BOXING, MARTIAL_ARTS, SWIMMING, PERSONAL_TRAINING, 
        GROUP_FITNESS, REHABILITATION, OTHER
    }

    public enum ClassStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, POSTPONED
    }

    public enum DifficultyLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, ALL_LEVELS
    }

    public enum RecurrenceType {
        DAILY, WEEKLY, MONTHLY
    }

    // Helper methods
    public boolean isFull() {
        return currentEnrollment >= maxCapacity;
    }

    public boolean canEnroll() {
        return status == ClassStatus.SCHEDULED && !isFull() && 
               classDate.isAfter(LocalDate.now().minusDays(1));
    }

    public int getAvailableSpots() {
        return maxCapacity - currentEnrollment;
    }

    public double getCapacityPercentage() {
        if (maxCapacity == 0) return 0;
        return (currentEnrollment * 100.0) / maxCapacity;
    }

    public String getTimeRange() {
        return startTime + " - " + endTime;
    }

    public boolean isToday() {
        return classDate.equals(LocalDate.now());
    }

    public boolean isUpcoming() {
        return classDate.isAfter(LocalDate.now()) || 
               (classDate.equals(LocalDate.now()) && startTime.isAfter(LocalTime.now()));
    }

    public boolean isPast() {
        return classDate.isBefore(LocalDate.now()) || 
               (classDate.equals(LocalDate.now()) && endTime.isBefore(LocalTime.now()));
    }

    // Essential getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public ClassType getClassType() { return classType; }
    public void setClassType(ClassType classType) { this.classType = classType; }
    
    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }
    
    public LocalDate getClassDate() { return classDate; }
    public void setClassDate(LocalDate classDate) { this.classDate = classDate; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    
    public Integer getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }
    
    public Integer getCurrentEnrollment() { return currentEnrollment; }
    public void setCurrentEnrollment(Integer currentEnrollment) { this.currentEnrollment = currentEnrollment; }
    
    public ClassStatus getStatus() { return status; }
    public void setStatus(ClassStatus status) { this.status = status; }
    
    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // Additional getters and setters for fields referenced in services
    public String getEquipment() { return null; } // TODO: Add equipment field if needed
    public void setEquipment(String equipment) { } // TODO: Add equipment field if needed
    
    public String getNotes() { return null; } // TODO: Add notes field if needed  
    public void setNotes(String notes) { } // TODO: Add notes field if needed
    
    public String getImageUrl() { return null; } // TODO: Add imageUrl field if needed
    public void setImageUrl(String imageUrl) { } // TODO: Add imageUrl field if needed
    
    public Boolean getIsRecurring() { return false; } // TODO: Add isRecurring field if needed
    public void setIsRecurring(Boolean isRecurring) { } // TODO: Add isRecurring field if needed
    
    public String getRecurrenceType() { return null; } // TODO: Add recurrenceType field if needed
    public void setRecurrenceType(String recurrenceType) { } // TODO: Add recurrenceType field if needed
    
    public LocalDate getRecurrenceEndDate() { return null; } // TODO: Add recurrenceEndDate field if needed
    public void setRecurrenceEndDate(LocalDate recurrenceEndDate) { } // TODO: Add recurrenceEndDate field if needed
}
