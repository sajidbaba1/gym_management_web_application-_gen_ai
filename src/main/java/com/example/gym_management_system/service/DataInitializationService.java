package com.example.gym_management_system.service;

import com.example.gym_management_system.entity.User;
import com.example.gym_management_system.entity.Member;
import com.example.gym_management_system.repository.UserRepository;
import com.example.gym_management_system.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DataInitializationService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultUsers();
        // initializeSampleMembers(); // Temporarily disabled
    }

    private void initializeDefaultUsers() {
        // Initializing default users

        // Create Owner user
        if (!userRepository.existsByUsername("owner")) {
            User owner = new User();
            owner.setUsername("owner");
            owner.setEmail("owner@fithub.com");
            owner.setPassword(passwordEncoder.encode("owner123"));
            owner.setFirstName("Gym");
            owner.setLastName("Owner");
            owner.setRole(User.Role.OWNER);
            owner.setStatus(User.UserStatus.ACTIVE);
            owner.setEmailVerified(true);
            owner.setEnabled(true);
            userRepository.save(owner);
            // Created owner user
        }

        // Create Admin user
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@fithub.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setRole(User.Role.ADMIN);
            admin.setStatus(User.UserStatus.ACTIVE);
            admin.setEmailVerified(true);
            admin.setEnabled(true);
            userRepository.save(admin);
            // Created admin user
        }

        // Create Manager user
        if (!userRepository.existsByUsername("manager")) {
            User manager = new User();
            manager.setUsername("manager");
            manager.setEmail("manager@fithub.com");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setFirstName("John");
            manager.setLastName("Manager");
            manager.setRole(User.Role.MANAGER);
            manager.setStatus(User.UserStatus.ACTIVE);
            manager.setEmailVerified(true);
            manager.setEnabled(true);
            userRepository.save(manager);
            // Created manager user
        }

        // Create Trainer user
        if (!userRepository.existsByUsername("trainer")) {
            User trainer = new User();
            trainer.setUsername("trainer");
            trainer.setEmail("trainer@fithub.com");
            trainer.setPassword(passwordEncoder.encode("trainer123"));
            trainer.setFirstName("Sarah");
            trainer.setLastName("Fitness");
            trainer.setRole(User.Role.TRAINER);
            trainer.setStatus(User.UserStatus.ACTIVE);
            trainer.setEmailVerified(true);
            trainer.setEnabled(true);
            userRepository.save(trainer);
            // Created trainer user
        }

        // Create Receptionist user
        if (!userRepository.existsByUsername("reception")) {
            User receptionist = new User();
            receptionist.setUsername("reception");
            receptionist.setEmail("reception@fithub.com");
            receptionist.setPassword(passwordEncoder.encode("reception123"));
            receptionist.setFirstName("Emily");
            receptionist.setLastName("Reception");
            receptionist.setRole(User.Role.RECEPTIONIST);
            receptionist.setStatus(User.UserStatus.ACTIVE);
            receptionist.setEmailVerified(true);
            receptionist.setEnabled(true);
            userRepository.save(receptionist);
            // Created receptionist user
        }

        // Create Member user
        if (!userRepository.existsByUsername("member")) {
            User member = new User();
            member.setUsername("member");
            member.setEmail("member@fithub.com");
            member.setPassword(passwordEncoder.encode("member123"));
            member.setFirstName("Mike");
            member.setLastName("Johnson");
            member.setRole(User.Role.MEMBER);
            member.setStatus(User.UserStatus.ACTIVE);
            member.setEmailVerified(true);
            member.setEnabled(true);
            userRepository.save(member);
            // Created member user
        }

        // Default users initialization completed
    }

    private void initializeSampleMembers() {
        // Initializing sample members for testing
        
        // Only create sample members if there are no existing members
        if (memberRepository.count() == 0) {
            // Sample Member 1
            Member member1 = new Member();
            member1.setFirstName("John");
            member1.setLastName("Doe");
            member1.setEmail("john.doe@example.com");
            member1.setPhone("+1-555-0101");
            member1.setAddress("123 Main Street");
            member1.setCity("New York");
            member1.setState("NY");
            member1.setZipCode("10001");
            member1.setDateOfBirth(LocalDate.of(1990, 5, 15));
            member1.setGender(Member.Gender.MALE);
            member1.setMembershipType(Member.MembershipType.PREMIUM);
            member1.setStatus(Member.MembershipStatus.ACTIVE);
            member1.setMembershipStartDate(LocalDate.now().minusMonths(6));
            member1.setMembershipEndDate(LocalDate.now().plusMonths(6));
            member1.setMonthlyFee(new BigDecimal("79.99"));
            member1.setCompletedSessions(25);
            member1.setTotalSessions(30);
            member1.setEmergencyContactName("Jane Doe");
            member1.setEmergencyContactPhone("+1-555-0102");
            memberRepository.save(member1);

            // Sample Member 2
            Member member2 = new Member();
            member2.setFirstName("Sarah");
            member2.setLastName("Johnson");
            member2.setEmail("sarah.johnson@example.com");
            member2.setPhone("+1-555-0201");
            member2.setAddress("456 Oak Avenue");
            member2.setCity("Los Angeles");
            member2.setState("CA");
            member2.setZipCode("90210");
            member2.setDateOfBirth(LocalDate.of(1985, 8, 22));
            member2.setGender(Member.Gender.FEMALE);
            member2.setMembershipType(Member.MembershipType.VIP);
            member2.setStatus(Member.MembershipStatus.ACTIVE);
            member2.setMembershipStartDate(LocalDate.now().minusMonths(12));
            member2.setMembershipEndDate(LocalDate.now().plusMonths(12));
            member2.setMonthlyFee(new BigDecimal("129.99"));
            member2.setCompletedSessions(45);
            member2.setTotalSessions(50);
            member2.setEmergencyContactName("Mike Johnson");
            member2.setEmergencyContactPhone("+1-555-0202");
            memberRepository.save(member2);

            // Sample Member 3
            Member member3 = new Member();
            member3.setFirstName("Michael");
            member3.setLastName("Smith");
            member3.setEmail("michael.smith@example.com");
            member3.setPhone("+1-555-0301");
            member3.setAddress("789 Pine Street");
            member3.setCity("Chicago");
            member3.setState("IL");
            member3.setZipCode("60601");
            member3.setDateOfBirth(LocalDate.of(1992, 12, 10));
            member3.setGender(Member.Gender.MALE);
            member3.setMembershipType(Member.MembershipType.STANDARD);
            member3.setStatus(Member.MembershipStatus.ACTIVE);
            member3.setMembershipStartDate(LocalDate.now().minusMonths(3));
            member3.setMembershipEndDate(LocalDate.now().plusMonths(9));
            member3.setMonthlyFee(new BigDecimal("49.99"));
            member3.setCompletedSessions(15);
            member3.setTotalSessions(20);
            memberRepository.save(member3);

            // Sample Member 4
            Member member4 = new Member();
            member4.setFirstName("Emily");
            member4.setLastName("Davis");
            member4.setEmail("emily.davis@example.com");
            member4.setPhone("+1-555-0401");
            member4.setAddress("321 Elm Street");
            member4.setCity("Miami");
            member4.setState("FL");
            member4.setZipCode("33101");
            member4.setDateOfBirth(LocalDate.of(1988, 3, 5));
            member4.setGender(Member.Gender.FEMALE);
            member4.setMembershipType(Member.MembershipType.PREMIUM);
            member4.setStatus(Member.MembershipStatus.ACTIVE);
            member4.setMembershipStartDate(LocalDate.now().minusMonths(8));
            member4.setMembershipEndDate(LocalDate.now().plusMonths(4));
            member4.setMonthlyFee(new BigDecimal("79.99"));
            member4.setCompletedSessions(35);
            member4.setTotalSessions(40);
            member4.setEmergencyContactName("Robert Davis");
            member4.setEmergencyContactPhone("+1-555-0402");
            memberRepository.save(member4);

            // Created sample members for testing
        } else {
            // Sample members already exist, skipping initialization
        }
    }
}
