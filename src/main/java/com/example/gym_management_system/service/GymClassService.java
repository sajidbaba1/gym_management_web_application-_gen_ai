package com.example.gym_management_system.service;

import com.example.gym_management_system.entity.GymClass;
import com.example.gym_management_system.repository.GymClassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class GymClassService {

    private final GymClassRepository gymClassRepository;

    // Create a new class
    public GymClass createClass(GymClass gymClass) {
        // Creating new class
        
        // Validate scheduling conflicts
        if (hasSchedulingConflict(gymClass)) {
            throw new RuntimeException("Scheduling conflict detected for room " + gymClass.getRoom() + 
                                     " on " + gymClass.getClassDate() + " at " + gymClass.getTimeRange());
        }
        
        // Calculate duration if not set
        if (gymClass.getDuration() == null && gymClass.getStartTime() != null && gymClass.getEndTime() != null) {
            int duration = (int) java.time.Duration.between(gymClass.getStartTime(), gymClass.getEndTime()).toMinutes();
            gymClass.setDuration(duration);
        }
        
        // Set default values
        if (gymClass.getCurrentEnrollment() == null) {
            gymClass.setCurrentEnrollment(0);
        }
        
        if (gymClass.getStatus() == null) {
            gymClass.setStatus(GymClass.ClassStatus.SCHEDULED);
        }
        
        GymClass savedClass = gymClassRepository.save(gymClass);
        // Class created successfully
        return savedClass;
    }

    // Get all classes with pagination
    public Page<GymClass> getAllClasses(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return gymClassRepository.findAll(pageable);
    }

    // Get class by ID
    public Optional<GymClass> getClassById(Long id) {
        return gymClassRepository.findById(id);
    }

    // Update class
    public GymClass updateClass(Long id, GymClass classDetails) {
        // Updating class
        
        GymClass existingClass = gymClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found with ID: " + id));
        
        // Validate scheduling conflicts (excluding current class)
        if (hasSchedulingConflict(classDetails, id)) {
            throw new RuntimeException("Scheduling conflict detected for room " + classDetails.getRoom() + 
                                     " on " + classDetails.getClassDate() + " at " + classDetails.getTimeRange());
        }
        
        // Update fields
        existingClass.setName(classDetails.getName());
        existingClass.setDescription(classDetails.getDescription());
        existingClass.setClassType(classDetails.getClassType());
        existingClass.setInstructor(classDetails.getInstructor());
        existingClass.setClassDate(classDetails.getClassDate());
        existingClass.setStartTime(classDetails.getStartTime());
        existingClass.setEndTime(classDetails.getEndTime());
        existingClass.setMaxCapacity(classDetails.getMaxCapacity());
        existingClass.setStatus(classDetails.getStatus());
        existingClass.setDifficultyLevel(classDetails.getDifficultyLevel());
        existingClass.setPrice(classDetails.getPrice());
        existingClass.setRoom(classDetails.getRoom());
        existingClass.setEquipment(classDetails.getEquipment());
        existingClass.setNotes(classDetails.getNotes());
        existingClass.setImageUrl(classDetails.getImageUrl());
        existingClass.setIsRecurring(classDetails.getIsRecurring());
        existingClass.setRecurrenceType(classDetails.getRecurrenceType());
        existingClass.setRecurrenceEndDate(classDetails.getRecurrenceEndDate());
        
        // Recalculate duration
        if (classDetails.getStartTime() != null && classDetails.getEndTime() != null) {
            int duration = (int) java.time.Duration.between(classDetails.getStartTime(), classDetails.getEndTime()).toMinutes();
            existingClass.setDuration(duration);
        }
        
        GymClass updatedClass = gymClassRepository.save(existingClass);
        // Class updated successfully
        return updatedClass;
    }

    // Delete class
    public void deleteClass(Long id) {
        // Deleting class
        
        if (!gymClassRepository.existsById(id)) {
            throw new RuntimeException("Class not found with ID: " + id);
        }
        
        gymClassRepository.deleteById(id);
        // Class deleted successfully
    }

    // Search classes
    public Page<GymClass> searchClasses(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("classDate", "startTime"));
        return gymClassRepository.searchClasses(searchTerm, pageable);
    }

    // Get upcoming classes
    public List<GymClass> getUpcomingClasses() {
        return gymClassRepository.findUpcomingClasses(LocalDate.now(), LocalTime.now());
    }

    // Get today's classes
    public List<GymClass> getTodaysClasses() {
        return gymClassRepository.findTodaysClasses(LocalDate.now());
    }

    // Get classes by status
    public List<GymClass> getClassesByStatus(GymClass.ClassStatus status) {
        return gymClassRepository.findByStatus(status);
    }

    // Get classes by type
    public List<GymClass> getClassesByType(GymClass.ClassType classType) {
        return gymClassRepository.findByClassType(classType);
    }

    // Get classes by instructor
    public List<GymClass> getClassesByInstructor(String instructor) {
        return gymClassRepository.findByInstructorIgnoreCase(instructor);
    }

    // Get classes by date range
    public List<GymClass> getClassesByDateRange(LocalDate startDate, LocalDate endDate) {
        return gymClassRepository.findByClassDateBetween(startDate, endDate);
    }

    // Get available classes
    public List<GymClass> getAvailableClasses() {
        return gymClassRepository.findAvailableClasses();
    }

    // Enroll member in class
    public GymClass enrollMember(Long classId) {
        GymClass gymClass = gymClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found with ID: " + classId));
        
        if (!gymClass.canEnroll()) {
            throw new RuntimeException("Cannot enroll in this class. Class may be full, cancelled, or in the past.");
        }
        
        gymClass.setCurrentEnrollment(gymClass.getCurrentEnrollment() + 1);
        return gymClassRepository.save(gymClass);
    }

    // Unenroll member from class
    public GymClass unenrollMember(Long classId) {
        GymClass gymClass = gymClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found with ID: " + classId));
        
        if (gymClass.getCurrentEnrollment() > 0) {
            gymClass.setCurrentEnrollment(gymClass.getCurrentEnrollment() - 1);
        }
        
        return gymClassRepository.save(gymClass);
    }

    // Update class status
    public GymClass updateClassStatus(Long classId, GymClass.ClassStatus status) {
        GymClass gymClass = gymClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found with ID: " + classId));
        
        gymClass.setStatus(status);
        return gymClassRepository.save(gymClass);
    }

    // Check for scheduling conflicts
    private boolean hasSchedulingConflict(GymClass gymClass) {
        return hasSchedulingConflict(gymClass, null);
    }

    private boolean hasSchedulingConflict(GymClass gymClass, Long excludeClassId) {
        if (gymClass.getRoom() == null || gymClass.getClassDate() == null || 
            gymClass.getStartTime() == null || gymClass.getEndTime() == null) {
            return false;
        }
        
        return gymClassRepository.hasSchedulingConflict(
                gymClass.getRoom(),
                gymClass.getClassDate(),
                gymClass.getStartTime(),
                gymClass.getEndTime(),
                excludeClassId
        );
    }

    // Get class statistics
    public ClassStats getClassStats() {
        long totalClasses = gymClassRepository.count();
        long scheduledClasses = gymClassRepository.countByStatus(GymClass.ClassStatus.SCHEDULED);
        long completedClasses = gymClassRepository.countByStatus(GymClass.ClassStatus.COMPLETED);
        long cancelledClasses = gymClassRepository.countByStatus(GymClass.ClassStatus.CANCELLED);
        
        return new ClassStats(totalClasses, scheduledClasses, completedClasses, cancelledClasses);
    }

    // Get popular classes
    public List<GymClass> getPopularClasses() {
        return gymClassRepository.findPopularClasses();
    }

    // Get classes with low enrollment
    public List<GymClass> getClassesWithLowEnrollment() {
        return gymClassRepository.findClassesWithLowEnrollment();
    }

    // Inner class for statistics
    public static class ClassStats {
        private final long totalClasses;
        private final long scheduledClasses;
        private final long completedClasses;
        private final long cancelledClasses;

        public ClassStats(long totalClasses, long scheduledClasses, long completedClasses, long cancelledClasses) {
            this.totalClasses = totalClasses;
            this.scheduledClasses = scheduledClasses;
            this.completedClasses = completedClasses;
            this.cancelledClasses = cancelledClasses;
        }

        // Getters
        public long getTotalClasses() { return totalClasses; }
        public long getScheduledClasses() { return scheduledClasses; }
        public long getCompletedClasses() { return completedClasses; }
        public long getCancelledClasses() { return cancelledClasses; }
    }
}
