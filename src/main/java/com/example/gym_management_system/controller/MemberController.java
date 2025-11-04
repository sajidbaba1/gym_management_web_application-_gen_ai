package com.example.gym_management_system.controller;

import com.example.gym_management_system.entity.Member;
import com.example.gym_management_system.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MemberController.class);
    private final MemberService memberService;

    // List all members with pagination and search
    @GetMapping
    public String listMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String search,
            Model model) {

        Page<Member> membersPage;
        
        if (search != null && !search.trim().isEmpty()) {
            log.info("Searching members with term: '{}'", search.trim());
            membersPage = memberService.searchMembers(search.trim(), page, size);
            model.addAttribute("search", search.trim());
            log.info("Found {} members matching search term", membersPage.getTotalElements());
        } else {
            membersPage = memberService.getAllMembers(page, size, sortBy, sortDirection);
        }

        // Get member statistics
        MemberService.MemberStats stats = memberService.getMemberStats();

        model.addAttribute("membersPage", membersPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", membersPage.getTotalPages());
        model.addAttribute("totalElements", membersPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("stats", stats);
        model.addAttribute("pageTitle", "Members - FitHub");

        return "members/list";
    }

    // Show add member form
    @GetMapping("/add")
    public String showAddMemberForm(Model model) {
        model.addAttribute("member", new Member());
        model.addAttribute("pageTitle", "Add New Member - FitHub");
        model.addAttribute("isEdit", false);
        return "members/form";
    }

    // Handle add member form submission
    @PostMapping("/add")
    public String addMember(@ModelAttribute Member member, 
                           BindingResult result, 
                           RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "members/form";
        }

        try {
            Member savedMember = memberService.createMember(member);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Member '" + savedMember.getFullName() + "' added successfully!");
            return "redirect:/members";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/members/add";
        }
    }

    // Show member details
    @GetMapping("/{id}")
    public String viewMember(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Member> memberOpt = memberService.getMemberById(id);
        
        if (memberOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Member not found!");
            return "redirect:/members";
        }

        Member member = memberOpt.get();
        model.addAttribute("member", member);
        model.addAttribute("pageTitle", member.getFullName() + " - Member Details");
        
        return "members/view";
    }

    // Show edit member form
    @GetMapping("/{id}/edit")
    public String showEditMemberForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Member> memberOpt = memberService.getMemberById(id);
        
        if (memberOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Member not found!");
            return "redirect:/members";
        }

        model.addAttribute("member", memberOpt.get());
        model.addAttribute("pageTitle", "Edit Member - FitHub");
        model.addAttribute("isEdit", true);
        return "members/form";
    }

    // Handle edit member form submission
    @PostMapping("/{id}/edit")
    public String editMember(@PathVariable Long id, 
                            @ModelAttribute Member member, 
                            BindingResult result, 
                            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "members/form";
        }

        try {
            Member updatedMember = memberService.updateMember(id, member);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Member '" + updatedMember.getFullName() + "' updated successfully!");
            return "redirect:/members/" + id;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/members/" + id + "/edit";
        }
    }

    // Delete member
    @PostMapping("/{id}/delete")
    public String deleteMember(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Member> memberOpt = memberService.getMemberById(id);
            String memberName = memberOpt.map(Member::getFullName).orElse("Unknown");
            
            memberService.deleteMember(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Member '" + memberName + "' deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/members";
    }

    // Get members by status (AJAX endpoint)
    @GetMapping("/status/{status}")
    @ResponseBody
    public List<Member> getMembersByStatus(@PathVariable String status) {
        try {
            Member.MembershipStatus membershipStatus = Member.MembershipStatus.valueOf(status.toUpperCase());
            return memberService.getMembersByStatus(membershipStatus);
        } catch (IllegalArgumentException e) {
            log.error("Invalid membership status: {}", status);
            return List.of();
        }
    }

    // Get members with expiring memberships (AJAX endpoint)
    @GetMapping("/expiring")
    @ResponseBody
    public List<Member> getMembersWithExpiringMemberships() {
        return memberService.getMembersWithExpiringMemberships();
    }

    // Get top members (AJAX endpoint)
    @GetMapping("/top")
    @ResponseBody
    public List<Member> getTopMembers(@RequestParam(defaultValue = "10") int limit) {
        return memberService.getTopMembersByCompletedSessions(limit);
    }

    // Update member status
    @PostMapping("/{id}/status")
    public String updateMemberStatus(@PathVariable Long id, 
                                   @RequestParam String status, 
                                   RedirectAttributes redirectAttributes) {
        try {
            Optional<Member> memberOpt = memberService.getMemberById(id);
            if (memberOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Member not found!");
                return "redirect:/members";
            }

            Member member = memberOpt.get();
            member.setStatus(Member.MembershipStatus.valueOf(status.toUpperCase()));
            memberService.updateMember(id, member);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Member status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Failed to update member status: " + e.getMessage());
        }
        
        return "redirect:/members/" + id;
    }
}
