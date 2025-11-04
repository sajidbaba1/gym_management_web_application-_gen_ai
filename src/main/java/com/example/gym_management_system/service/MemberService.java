package com.example.gym_management_system.service;

import com.example.gym_management_system.entity.Member;
import com.example.gym_management_system.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    // Create a new member
    public Member createMember(Member member) {
        // Creating new member
        
        // Validate email uniqueness
        if (memberRepository.findByEmail(member.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + member.getEmail());
        }
        
        // Validate phone uniqueness if provided
        if (member.getPhone() != null && memberRepository.findByPhone(member.getPhone()).isPresent()) {
            throw new RuntimeException("Phone number already exists: " + member.getPhone());
        }
        
        // Set default values
        if (member.getMembershipStartDate() == null) {
            member.setMembershipStartDate(LocalDate.now());
        }
        
        if (member.getStatus() == null) {
            member.setStatus(Member.MembershipStatus.ACTIVE);
        }
        
        if (member.getMembershipType() == null) {
            member.setMembershipType(Member.MembershipType.STANDARD);
        }
        
        Member savedMember = memberRepository.save(member);
        // Member created successfully
        return savedMember;
    }

    // Get all members with pagination
    public Page<Member> getAllMembers(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return memberRepository.findAll(pageable);
    }

    // Get member by ID
    public Optional<Member> getMemberById(Long id) {
        return memberRepository.findById(id);
    }

    // Get member by email
    public Optional<Member> getMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    // Update member
    public Member updateMember(Long id, Member memberDetails) {
        // Updating member
        
        Member existingMember = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with ID: " + id));
        
        // Validate email uniqueness (excluding current member)
        if (!existingMember.getEmail().equals(memberDetails.getEmail()) &&
            memberRepository.existsByEmailAndIdNot(memberDetails.getEmail(), id)) {
            throw new RuntimeException("Email already exists: " + memberDetails.getEmail());
        }
        
        // Validate phone uniqueness (excluding current member)
        if (memberDetails.getPhone() != null && 
            !memberDetails.getPhone().equals(existingMember.getPhone()) &&
            memberRepository.existsByPhoneAndIdNot(memberDetails.getPhone(), id)) {
            throw new RuntimeException("Phone number already exists: " + memberDetails.getPhone());
        }
        
        // Update fields
        existingMember.setFirstName(memberDetails.getFirstName());
        existingMember.setLastName(memberDetails.getLastName());
        existingMember.setEmail(memberDetails.getEmail());
        existingMember.setPhone(memberDetails.getPhone());
        existingMember.setAddress(memberDetails.getAddress());
        existingMember.setCity(memberDetails.getCity());
        existingMember.setState(memberDetails.getState());
        existingMember.setZipCode(memberDetails.getZipCode());
        existingMember.setDateOfBirth(memberDetails.getDateOfBirth());
        existingMember.setGender(memberDetails.getGender());
        existingMember.setEmergencyContactName(memberDetails.getEmergencyContactName());
        existingMember.setEmergencyContactPhone(memberDetails.getEmergencyContactPhone());
        existingMember.setMembershipType(memberDetails.getMembershipType());
        existingMember.setStatus(memberDetails.getStatus());
        existingMember.setMembershipEndDate(memberDetails.getMembershipEndDate());
        existingMember.setMonthlyFee(memberDetails.getMonthlyFee());
        existingMember.setNotes(memberDetails.getNotes());
        existingMember.setProfileImageUrl(memberDetails.getProfileImageUrl());
        
        Member updatedMember = memberRepository.save(existingMember);
        // Member updated successfully
        return updatedMember;
    }

    // Delete member
    public void deleteMember(Long id) {
        // Deleting member
        
        if (!memberRepository.existsById(id)) {
            throw new RuntimeException("Member not found with ID: " + id);
        }
        
        memberRepository.deleteById(id);
        // Member deleted successfully
    }

    // Search members
    public Page<Member> searchMembers(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return memberRepository.searchMembers(searchTerm, pageable);
    }

    // Get active members
    public List<Member> getActiveMembers() {
        return memberRepository.findByStatusOrderByCreatedAtDesc(Member.MembershipStatus.ACTIVE);
    }

    // Get members by status
    public List<Member> getMembersByStatus(Member.MembershipStatus status) {
        return memberRepository.findByStatus(status);
    }

    // Get members by membership type
    public List<Member> getMembersByMembershipType(Member.MembershipType membershipType) {
        return memberRepository.findByMembershipType(membershipType);
    }

    // Get members with expiring memberships (next 30 days)
    public List<Member> getMembersWithExpiringMemberships() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        return memberRepository.findMembersWithExpiringMemberships(today, thirtyDaysFromNow);
    }

    // Get member statistics
    public MemberStats getMemberStats() {
        long totalMembers = memberRepository.count();
        long activeMembers = memberRepository.countByStatus(Member.MembershipStatus.ACTIVE);
        long premiumMembers = memberRepository.countByMembershipType(Member.MembershipType.PREMIUM);
        long vipMembers = memberRepository.countByMembershipType(Member.MembershipType.VIP);
        
        return new MemberStats(totalMembers, activeMembers, premiumMembers, vipMembers);
    }

    // Get top members by completed sessions
    public List<Member> getTopMembersByCompletedSessions(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return memberRepository.findTopMembersByCompletedSessions(pageable);
    }

    // Update member session count
    public void updateMemberSessions(Long memberId, int completedSessions, int totalSessions) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with ID: " + memberId));
        
        member.setCompletedSessions(completedSessions);
        member.setTotalSessions(totalSessions);
        memberRepository.save(member);
    }

    // Deactivate expired memberships
    @Transactional
    public void deactivateExpiredMemberships() {
        // Checking for expired memberships
        List<Member> activeMembers = getActiveMembers();
        
        int deactivatedCount = 0;
        for (Member member : activeMembers) {
            if (member.isMembershipExpired()) {
                member.setStatus(Member.MembershipStatus.EXPIRED);
                memberRepository.save(member);
                deactivatedCount++;
                // Deactivated expired membership
            }
        }
        
        // Deactivated expired memberships
    }

    // Get recent members
    public List<Member> getRecentMembers(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        return memberRepository.findAll(pageable).getContent();
    }

    // Get expiring memberships
    public List<Member> getExpiringMemberships() {
        LocalDate cutoffDate = LocalDate.now().plusDays(30);
        return memberRepository.findMembersWithExpiringMemberships(LocalDate.now(), cutoffDate);
    }

    // Inner class for member statistics
    public static class MemberStats {
        private final long totalMembers;
        private final long activeMembers;
        private final long premiumMembers;
        private final long vipMembers;

        public MemberStats(long totalMembers, long activeMembers, long premiumMembers, long vipMembers) {
            this.totalMembers = totalMembers;
            this.activeMembers = activeMembers;
            this.premiumMembers = premiumMembers;
            this.vipMembers = vipMembers;
        }

        // Getters
        public long getTotalMembers() { return totalMembers; }
        public long getActiveMembers() { return activeMembers; }
        public long getPremiumMembers() { return premiumMembers; }
        public long getVipMembers() { return vipMembers; }
    }
}
