package com.example.gym_management_system.service;

import com.example.gym_management_system.entity.Trainer;
import com.example.gym_management_system.entity.User;
import com.example.gym_management_system.repository.TrainerRepository;
import com.example.gym_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;

    // Create a new trainer
    public Trainer createTrainer(Trainer trainer) {
        // Creating new trainer for user
        
        // Validate user exists and is a trainer
        User user = userRepository.findById(trainer.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() != User.Role.TRAINER) {
            throw new RuntimeException("User must have TRAINER role");
        }
        
        // Check if trainer profile already exists for this user
        if (trainerRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("Trainer profile already exists for this user");
        }
        
        // Validate employee ID uniqueness
        if (trainer.getEmployeeId() != null && 
            trainerRepository.findByEmployeeId(trainer.getEmployeeId()).isPresent()) {
            throw new RuntimeException("Employee ID already exists: " + trainer.getEmployeeId());
        }
        
        // Set default values
        if (trainer.getStatus() == null) {
            trainer.setStatus(Trainer.TrainerStatus.ACTIVE);
        }
        
        if (trainer.getHireDate() == null) {
            trainer.setHireDate(LocalDate.now());
        }
        
        Trainer savedTrainer = trainerRepository.save(trainer);
        // Trainer created successfully
        return savedTrainer;
    }

    // Get all trainers with pagination
    public Page<Trainer> getAllTrainers(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return trainerRepository.findAll(pageable);
    }

    // Get trainer by ID
    public Optional<Trainer> getTrainerById(Long id) {
        return trainerRepository.findById(id);
    }

    // Get trainer by user ID
    public Optional<Trainer> getTrainerByUserId(Long userId) {
        return trainerRepository.findByUserId(userId);
    }

    // Get trainer by employee ID
    public Optional<Trainer> getTrainerByEmployeeId(String employeeId) {
        return trainerRepository.findByEmployeeId(employeeId);
    }

    // Update trainer
    public Trainer updateTrainer(Long id, Trainer trainerDetails) {
        // Updating trainer
        
        Trainer existingTrainer = trainerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trainer not found with ID: " + id));
        
        // Validate employee ID uniqueness (excluding current trainer)
        if (trainerDetails.getEmployeeId() != null &&
            !trainerDetails.getEmployeeId().equals(existingTrainer.getEmployeeId()) &&
            trainerRepository.existsByEmployeeIdAndIdNot(trainerDetails.getEmployeeId(), id)) {
            throw new RuntimeException("Employee ID already exists: " + trainerDetails.getEmployeeId());
        }
        
        // Update fields
        existingTrainer.setEmployeeId(trainerDetails.getEmployeeId());
        existingTrainer.setBio(trainerDetails.getBio());
        existingTrainer.setSpecializations(trainerDetails.getSpecializations());
        existingTrainer.setCertifications(trainerDetails.getCertifications());
        existingTrainer.setExperienceYears(trainerDetails.getExperienceYears());
        existingTrainer.setEmploymentType(trainerDetails.getEmploymentType());
        existingTrainer.setHourlyRate(trainerDetails.getHourlyRate());
        existingTrainer.setMonthlySalary(trainerDetails.getMonthlySalary());
        existingTrainer.setStatus(trainerDetails.getStatus());
        existingTrainer.setNotes(trainerDetails.getNotes());
        existingTrainer.setProfileImageUrl(trainerDetails.getProfileImageUrl());
        
        // Update availability
        existingTrainer.setMondayStart(trainerDetails.getMondayStart());
        existingTrainer.setMondayEnd(trainerDetails.getMondayEnd());
        existingTrainer.setTuesdayStart(trainerDetails.getTuesdayStart());
        existingTrainer.setTuesdayEnd(trainerDetails.getTuesdayEnd());
        existingTrainer.setWednesdayStart(trainerDetails.getWednesdayStart());
        existingTrainer.setWednesdayEnd(trainerDetails.getWednesdayEnd());
        existingTrainer.setThursdayStart(trainerDetails.getThursdayStart());
        existingTrainer.setThursdayEnd(trainerDetails.getThursdayEnd());
        existingTrainer.setFridayStart(trainerDetails.getFridayStart());
        existingTrainer.setFridayEnd(trainerDetails.getFridayEnd());
        existingTrainer.setSaturdayStart(trainerDetails.getSaturdayStart());
        existingTrainer.setSaturdayEnd(trainerDetails.getSaturdayEnd());
        existingTrainer.setSundayStart(trainerDetails.getSundayStart());
        existingTrainer.setSundayEnd(trainerDetails.getSundayEnd());
        
        Trainer updatedTrainer = trainerRepository.save(existingTrainer);
        // Trainer updated successfully
        return updatedTrainer;
    }

    // Delete trainer
    public void deleteTrainer(Long id) {
        // Deleting trainer
        if (!trainerRepository.existsById(id)) {
            throw new RuntimeException("Trainer not found with ID: " + id);
        }
        
        trainerRepository.deleteById(id);
        // Trainer deleted successfully
    }

    // Search trainers
    public Page<Trainer> searchTrainers(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return trainerRepository.searchTrainers(searchTerm, pageable);
    }

    // Get active trainers
    public List<Trainer> getActiveTrainers() {
        return trainerRepository.findByStatusOrderByCreatedAtDesc(Trainer.TrainerStatus.ACTIVE);
    }
    // Get trainers by status
    public List<Trainer> getTrainersByStatus(Trainer.TrainerStatus status) {
        return trainerRepository.findByStatus(status);
    }

    // Get trainers by employment type
    public List<Trainer> getTrainersByEmploymentType(Trainer.EmploymentType employmentType) {
        return trainerRepository.findByEmploymentType(employmentType);
    }

    // Get trainers by specialization
    public List<Trainer> getTrainersBySpecialization(Trainer.Specialization specialization) {
        return trainerRepository.findBySpecialization(specialization);
    }

    // Get top rated trainers
    public List<Trainer> getTopRatedTrainers(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return trainerRepository.findTopRatedTrainers(pageable);
    }

    // Get most experienced trainers
    public List<Trainer> getMostExperiencedTrainers(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return trainerRepository.findMostExperiencedTrainers(pageable);
    }

    // Get trainers available on specific day
    public List<Trainer> getTrainersAvailableOnDay(String day) {
        return trainerRepository.findAvailableOnDay(day.toUpperCase());
    }

    // Update trainer status
    public Trainer updateTrainerStatus(Long trainerId, Trainer.TrainerStatus status) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new RuntimeException("Trainer not found with ID: " + trainerId));
        
        trainer.setStatus(status);
        return trainerRepository.save(trainer);
    }

    // Add rating to trainer
    public Trainer addRating(Long trainerId, BigDecimal rating) {
        if (rating.compareTo(BigDecimal.ONE) < 0 || rating.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }
        
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new RuntimeException("Trainer not found with ID: " + trainerId));
        
        trainer.updateRating(rating);
        return trainerRepository.save(trainer);
    }

    // Update trainer class count
    public void updateTrainerClassCount(Long trainerId, int classCount) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new RuntimeException("Trainer not found with ID: " + trainerId));
        
        trainer.setTotalClasses(classCount);
        trainerRepository.save(trainer);
    }

    // Update trainer member count
    public void updateTrainerMemberCount(Long trainerId, int memberCount) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new RuntimeException("Trainer not found with ID: " + trainerId));
        
        trainer.setTotalMembers(memberCount);
        trainerRepository.save(trainer);
    }

    // Get trainer statistics
    public TrainerStats getTrainerStats() {
        long totalTrainers = trainerRepository.count();
        long activeTrainers = trainerRepository.countByStatus(Trainer.TrainerStatus.ACTIVE);
        long fullTimeTrainers = trainerRepository.countByEmploymentType(Trainer.EmploymentType.FULL_TIME);
        long partTimeTrainers = trainerRepository.countByEmploymentType(Trainer.EmploymentType.PART_TIME);
        
        return new TrainerStats(totalTrainers, activeTrainers, fullTimeTrainers, partTimeTrainers);
    }

    // Get new trainers (hired in last 30 days)
    public List<Trainer> getNewTrainers() {
        LocalDate cutoffDate = LocalDate.now().minusDays(30);
        return trainerRepository.findNewTrainers(cutoffDate);
    }

    // Inner class for trainer statistics
    public static class TrainerStats {
        private final long totalTrainers;
        private final long activeTrainers;
        private final long fullTimeTrainers;
        private final long partTimeTrainers;

        public TrainerStats(long totalTrainers, long activeTrainers, long fullTimeTrainers, long partTimeTrainers) {
            this.totalTrainers = totalTrainers;
            this.activeTrainers = activeTrainers;
            this.fullTimeTrainers = fullTimeTrainers;
            this.partTimeTrainers = partTimeTrainers;
        }

        // Getters
        public long getTotalTrainers() { return totalTrainers; }
        public long getActiveTrainers() { return activeTrainers; }
        public long getFullTimeTrainers() { return fullTimeTrainers; }
        public long getPartTimeTrainers() { return partTimeTrainers; }
    }
}
