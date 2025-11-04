package com.example.gym_management_system.repository;

import com.example.gym_management_system.entity.GymClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface GymClassRepository extends JpaRepository<GymClass, Long> {

    // Find by status
    List<GymClass> findByStatus(GymClass.ClassStatus status);

    // Find by class type
    List<GymClass> findByClassType(GymClass.ClassType classType);

    // Find by instructor
    List<GymClass> findByInstructorIgnoreCase(String instructor);

    // Find by date
    List<GymClass> findByClassDate(LocalDate classDate);

    // Find by date range
    @Query("SELECT c FROM GymClass c WHERE c.classDate BETWEEN :startDate AND :endDate ORDER BY c.classDate, c.startTime")
    List<GymClass> findByClassDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find upcoming classes
    @Query("SELECT c FROM GymClass c WHERE c.classDate > :currentDate OR " +
           "(c.classDate = :currentDate AND c.startTime > :currentTime) " +
           "ORDER BY c.classDate, c.startTime")
    List<GymClass> findUpcomingClasses(@Param("currentDate") LocalDate currentDate, 
                                      @Param("currentTime") LocalTime currentTime);

    // Find today's classes
    @Query("SELECT c FROM GymClass c WHERE c.classDate = :today ORDER BY c.startTime")
    List<GymClass> findTodaysClasses(@Param("today") LocalDate today);

    // Find classes by room
    List<GymClass> findByRoomIgnoreCase(String room);

    // Search classes
    @Query("SELECT c FROM GymClass c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.instructor) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<GymClass> searchClasses(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Find available classes (not full)
    @Query("SELECT c FROM GymClass c WHERE c.currentEnrollment < c.maxCapacity AND c.status = 'SCHEDULED'")
    List<GymClass> findAvailableClasses();

    // Find classes by difficulty level
    List<GymClass> findByDifficultyLevel(GymClass.DifficultyLevel difficultyLevel);

    // Count classes by status
    long countByStatus(GymClass.ClassStatus status);

    // Count classes by type
    long countByClassType(GymClass.ClassType classType);

    // Find classes with low enrollment (less than 50% capacity)
    @Query("SELECT c FROM GymClass c WHERE (c.currentEnrollment * 100.0 / c.maxCapacity) < 50 AND c.status = 'SCHEDULED'")
    List<GymClass> findClassesWithLowEnrollment();

    // Find popular classes (more than 80% capacity)
    @Query("SELECT c FROM GymClass c WHERE (c.currentEnrollment * 100.0 / c.maxCapacity) > 80 AND c.status = 'SCHEDULED'")
    List<GymClass> findPopularClasses();

    // Find classes by instructor and date range
    @Query("SELECT c FROM GymClass c WHERE LOWER(c.instructor) = LOWER(:instructor) AND " +
           "c.classDate BETWEEN :startDate AND :endDate ORDER BY c.classDate, c.startTime")
    List<GymClass> findByInstructorAndDateRange(@Param("instructor") String instructor,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    // Check for scheduling conflicts
    @Query("SELECT COUNT(c) > 0 FROM GymClass c WHERE c.room = :room AND c.classDate = :classDate AND " +
           "((c.startTime <= :startTime AND c.endTime > :startTime) OR " +
           "(c.startTime < :endTime AND c.endTime >= :endTime) OR " +
           "(c.startTime >= :startTime AND c.endTime <= :endTime)) AND " +
           "c.status IN ('SCHEDULED', 'IN_PROGRESS') AND " +
           "(:classId IS NULL OR c.id != :classId)")
    boolean hasSchedulingConflict(@Param("room") String room,
                                 @Param("classDate") LocalDate classDate,
                                 @Param("startTime") LocalTime startTime,
                                 @Param("endTime") LocalTime endTime,
                                 @Param("classId") Long classId);

    // Find recurring classes
    List<GymClass> findByIsRecurringTrue();

    // Get class statistics
    @Query("SELECT c.classType, COUNT(c), AVG(c.currentEnrollment), AVG(c.maxCapacity) " +
           "FROM GymClass c GROUP BY c.classType")
    List<Object[]> getClassStatistics();
}
