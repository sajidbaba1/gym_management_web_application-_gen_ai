package com.example.gym_management_system.repository;

import com.example.gym_management_system.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // Find by email
    Optional<Member> findByEmail(String email);

    // Find by phone
    Optional<Member> findByPhone(String phone);

    // Find by status
    List<Member> findByStatus(Member.MembershipStatus status);

    // Find by membership type
    List<Member> findByMembershipType(Member.MembershipType membershipType);

    // Find active members
    List<Member> findByStatusOrderByCreatedAtDesc(Member.MembershipStatus status);

    // Search members by name, email, or phone (enhanced search)
    @Query("SELECT m FROM Member m WHERE " +
           "LOWER(CONCAT(m.firstName, ' ', m.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(m.phone, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(m.city, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Member> searchMembers(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Find members with expiring memberships
    @Query("SELECT m FROM Member m WHERE m.membershipEndDate BETWEEN :startDate AND :endDate AND m.status = 'ACTIVE'")
    List<Member> findMembersWithExpiringMemberships(@Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);

    // Count members by status
    long countByStatus(Member.MembershipStatus status);

    // Count members by membership type
    long countByMembershipType(Member.MembershipType membershipType);

    // Find members by city
    List<Member> findByCityIgnoreCase(String city);

    // Find members joined in date range
    @Query("SELECT m FROM Member m WHERE m.membershipStartDate BETWEEN :startDate AND :endDate")
    List<Member> findMembersJoinedBetween(@Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);

    // Get top members by completed sessions
    @Query("SELECT m FROM Member m WHERE m.status = 'ACTIVE' ORDER BY m.completedSessions DESC")
    List<Member> findTopMembersByCompletedSessions(Pageable pageable);

    // Check if email exists (excluding specific member)
    @Query("SELECT COUNT(m) > 0 FROM Member m WHERE m.email = :email AND m.id != :memberId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("memberId") Long memberId);

    // Check if phone exists (excluding specific member)
    @Query("SELECT COUNT(m) > 0 FROM Member m WHERE m.phone = :phone AND m.id != :memberId")
    boolean existsByPhoneAndIdNot(@Param("phone") String phone, @Param("memberId") Long memberId);
}
