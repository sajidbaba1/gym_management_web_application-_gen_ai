package com.example.gym_management_system.repository;

import com.example.gym_management_system.entity.Trainer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    // Find by user
    Optional<Trainer> findByUserId(Long userId);

    // Find by employee ID
    Optional<Trainer> findByEmployeeId(String employeeId);

    // Find by status
    List<Trainer> findByStatus(Trainer.TrainerStatus status);

    // Find active trainers
    List<Trainer> findByStatusOrderByCreatedAtDesc(Trainer.TrainerStatus status);

    // Find by employment type
    List<Trainer> findByEmploymentType(Trainer.EmploymentType employmentType);

    // Find by specialization
    @Query("SELECT t FROM Trainer t JOIN t.specializations s WHERE s = :specialization AND t.status = 'ACTIVE'")
    List<Trainer> findBySpecialization(@Param("specialization") Trainer.Specialization specialization);

    // Search trainers
    @Query("SELECT t FROM Trainer t WHERE " +
           "LOWER(t.user.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.user.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.employeeId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.user.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Trainer> searchTrainers(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Find trainers by experience range
    @Query("SELECT t FROM Trainer t WHERE t.experienceYears BETWEEN :minYears AND :maxYears AND t.status = 'ACTIVE'")
    List<Trainer> findByExperienceRange(@Param("minYears") Integer minYears, @Param("maxYears") Integer maxYears);

    // Find trainers by rating range
    @Query("SELECT t FROM Trainer t WHERE t.rating BETWEEN :minRating AND :maxRating AND t.status = 'ACTIVE'")
    List<Trainer> findByRatingRange(@Param("minRating") BigDecimal minRating, @Param("maxRating") BigDecimal maxRating);

    // Find top rated trainers
    @Query("SELECT t FROM Trainer t WHERE t.status = 'ACTIVE' AND t.totalRatings > 0 ORDER BY t.rating DESC")
    List<Trainer> findTopRatedTrainers(Pageable pageable);

    // Find most experienced trainers
    @Query("SELECT t FROM Trainer t WHERE t.status = 'ACTIVE' ORDER BY t.experienceYears DESC")
    List<Trainer> findMostExperiencedTrainers(Pageable pageable);

    // Find trainers hired in date range
    @Query("SELECT t FROM Trainer t WHERE t.hireDate BETWEEN :startDate AND :endDate")
    List<Trainer> findTrainersHiredBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Count trainers by status
    long countByStatus(Trainer.TrainerStatus status);

    // Count trainers by employment type
    long countByEmploymentType(Trainer.EmploymentType employmentType);

    // Find trainers available on specific day
    @Query("SELECT t FROM Trainer t WHERE t.status = 'ACTIVE' AND " +
           "((:day = 'MONDAY' AND t.mondayStart IS NOT NULL) OR " +
           "(:day = 'TUESDAY' AND t.tuesdayStart IS NOT NULL) OR " +
           "(:day = 'WEDNESDAY' AND t.wednesdayStart IS NOT NULL) OR " +
           "(:day = 'THURSDAY' AND t.thursdayStart IS NOT NULL) OR " +
           "(:day = 'FRIDAY' AND t.fridayStart IS NOT NULL) OR " +
           "(:day = 'SATURDAY' AND t.saturdayStart IS NOT NULL) OR " +
           "(:day = 'SUNDAY' AND t.sundayStart IS NOT NULL))")
    List<Trainer> findAvailableOnDay(@Param("day") String day);

    // Get trainer statistics
    @Query("SELECT t.employmentType, COUNT(t), AVG(t.rating), AVG(t.experienceYears) " +
           "FROM Trainer t WHERE t.status = 'ACTIVE' GROUP BY t.employmentType")
    List<Object[]> getTrainerStatistics();

    // Find trainers with specific certification
    @Query("SELECT t FROM Trainer t JOIN t.certifications c WHERE LOWER(c) LIKE LOWER(CONCAT('%', :certification, '%')) AND t.status = 'ACTIVE'")
    List<Trainer> findByCertification(@Param("certification") String certification);

    // Check if employee ID exists (excluding specific trainer)
    @Query("SELECT COUNT(t) > 0 FROM Trainer t WHERE t.employeeId = :employeeId AND t.id != :trainerId")
    boolean existsByEmployeeIdAndIdNot(@Param("employeeId") String employeeId, @Param("trainerId") Long trainerId);

    // Find trainers with high workload (many classes)
    @Query("SELECT t FROM Trainer t WHERE t.totalClasses > :classThreshold AND t.status = 'ACTIVE' ORDER BY t.totalClasses DESC")
    List<Trainer> findHighWorkloadTrainers(@Param("classThreshold") Integer classThreshold);

    // Find new trainers (hired in last 30 days)
    @Query("SELECT t FROM Trainer t WHERE t.hireDate >= :cutoffDate AND t.status = 'ACTIVE'")
    List<Trainer> findNewTrainers(@Param("cutoffDate") LocalDate cutoffDate);
}
