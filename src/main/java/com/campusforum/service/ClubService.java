package com.campusforum.service;

import com.campusforum.model.*;
import com.campusforum.repository.ClubMemberRepository;
import com.campusforum.repository.ClubRepository;
import com.campusforum.repository.ClubRequestRepository;
import com.campusforum.repository.DepartmentRepository;
import com.campusforum.repository.NotificationRepository;
import com.campusforum.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubRequestRepository clubRequestRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public ClubService(ClubRepository clubRepository,
                       ClubRequestRepository clubRequestRepository,
                       ClubMemberRepository clubMemberRepository,
                       DepartmentRepository departmentRepository,
                       UserRepository userRepository,
                       NotificationRepository notificationRepository) {
        this.clubRepository = clubRepository;
        this.clubRequestRepository = clubRequestRepository;
        this.clubMemberRepository = clubMemberRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    public List<Club> getAllClubs() {
        return clubRepository.findAll();
    }

    public List<Club> getActiveClubs() {
        return clubRepository.findByStatus("ACTIVE");
    }

    public List<Club> getClubsByDepartment(Long departmentId) {
        if (departmentId == null) return getAllClubs();
        return clubRepository.findByDepartmentId(departmentId);
    }

    public List<User> getEligibleLeaders() {
        return userRepository.findByRoleInOrderByFullNameAsc(java.util.List.of(Role.STUDENT, Role.CLUB_LEADER));
    }

    public Club getClubById(Long id) {
        return clubRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Club not found with ID: " + id));
    }

    public Club createClub(String clubName, String description, Long departmentId, Long leaderId) {
        if (clubRepository.existsByClubName(clubName.trim())) {
            throw new IllegalArgumentException("Club name already exists: " + clubName);
        }

        Club club = new Club();
        club.setClubName(clubName.trim());
        club.setDescription(description != null ? description.trim() : "");
        club.setStatus("ACTIVE");

        if (departmentId != null) {
            departmentRepository.findById(departmentId).ifPresent(club::setDepartment);
        }
        if (leaderId != null) {
            userRepository.findById(leaderId).ifPresent(user -> {
                user.setRole(Role.CLUB_LEADER);
                user.setDesignation("CLUB_LEADER");
                userRepository.save(user);
                club.setLeader(user);
            });
        }

        Club saved = clubRepository.save(club);
        if (saved.getLeader() != null) {
            ensureClubLeaderMembership(saved, saved.getLeader());
        }
        return saved;
    }

    public Club updateClub(Long id, String clubName, String description, Long departmentId, Long leaderId, String status) {
        Club club = getClubById(id);
        club.setClubName(clubName.trim());
        club.setDescription(description != null ? description.trim() : "");
        if (status != null) club.setStatus(status);

        if (departmentId != null) {
            departmentRepository.findById(departmentId).ifPresent(club::setDepartment);
        } else {
            club.setDepartment(null);
        }

        if (leaderId != null) {
            userRepository.findById(leaderId).ifPresent(user -> {
                user.setRole(Role.CLUB_LEADER);
                user.setDesignation("CLUB_LEADER");
                userRepository.save(user);
                club.setLeader(user);
            });
        } else {
            club.setLeader(null);
        }

        Club saved = clubRepository.save(club);
        if (saved.getLeader() != null) {
            ensureClubLeaderMembership(saved, saved.getLeader());
        }
        return saved;
    }

    private void ensureClubLeaderMembership(Club club, User leader) {
        Optional<ClubMember> memberOpt = clubMemberRepository.findByClubIdAndStudentId(club.getId(), leader.getId());
        if (memberOpt.isPresent()) {
            ClubMember cm = memberOpt.get();
            cm.setStatus("APPROVED");
            cm.setJoinedAt(LocalDateTime.now());
            clubMemberRepository.save(cm);
        } else {
            ClubMember cm = new ClubMember(club, leader, "Club Leader Membership");
            cm.setStatus("APPROVED");
            cm.setJoinedAt(LocalDateTime.now());
            clubMemberRepository.save(cm);
        }
    }

    public void deleteClub(Long id) {
        if (!clubRepository.existsById(id)) {
            throw new IllegalArgumentException("Club not found with ID: " + id);
        }
        clubRepository.deleteById(id);
    }

    @Transactional
    public Club appointClubLeader(Long clubId, Long studentId, User appointedBy) {
        Club club = getClubById(clubId);
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        User oldLeader = club.getLeader();
        if (oldLeader != null && !oldLeader.getId().equals(student.getId())) {
            List<Club> otherClubs = clubRepository.findByLeaderId(oldLeader.getId());
            if (otherClubs.size() <= 1) {
                oldLeader.setRole(Role.STUDENT);
                oldLeader.setDesignation("STUDENT");
                userRepository.save(oldLeader);
            }
        }

        student.setRole(Role.CLUB_LEADER);
        student.setDesignation("CLUB_LEADER");
        userRepository.save(student);

        club.setLeader(student);
        Club savedClub = clubRepository.save(club);
        ensureClubLeaderMembership(savedClub, student);

        String approverName = appointedBy != null ? appointedBy.getFullName() : "Administration";
        notificationRepository.save(new Notification(
                student.getId(),
                "Appointed as Club Leader: " + savedClub.getClubName(),
                "Congratulations! You have been appointed as the Club Leader for " + savedClub.getClubName() + " by " + approverName + ".",
                "CLUB",
                savedClub.getId()
        ));

        return savedClub;
    }

    @Transactional
    public Club removeClubLeader(Long clubId) {
        Club club = getClubById(clubId);
        User leader = club.getLeader();
        if (leader != null) {
            club.setLeader(null);
            clubRepository.save(club);

            List<Club> otherClubs = clubRepository.findByLeaderId(leader.getId());
            if (otherClubs.isEmpty()) {
                leader.setRole(Role.STUDENT);
                leader.setDesignation("STUDENT");
                userRepository.save(leader);
            }
        }
        return club;
    }

    public boolean isStudentClubMember(Long userId, Long clubId) {
        if (userId == null || clubId == null) return false;
        Club club = clubRepository.findById(clubId).orElse(null);
        if (club == null) return false;
        if (club.getLeader() != null && club.getLeader().getId().equals(userId)) {
            return true;
        }
        return clubMemberRepository.existsByClubIdAndStudentIdAndStatus(clubId, userId, "APPROVED");
    }

    @Transactional
    public ClubMember applyToJoinClub(User student, Long clubId, String reason, Map<String, String> details) {
        Club club = getClubById(clubId);
        Optional<ClubMember> existing = clubMemberRepository.findByClubIdAndStudentId(clubId, student.getId());

        String prn = (details != null && details.get("prnRollNo") != null && !details.get("prnRollNo").isBlank()) ? details.get("prnRollNo").trim() : student.getPrnRollNo();
        String dept = (details != null && details.get("departmentName") != null && !details.get("departmentName").isBlank()) ? details.get("departmentName").trim() : (student.getDepartment() != null ? student.getDepartment().getDepartmentName() : "");
        String course = (details != null && details.get("course") != null && !details.get("course").isBlank()) ? details.get("course").trim() : student.getCourse();
        String year = (details != null && details.get("academicYear") != null && !details.get("academicYear").isBlank()) ? details.get("academicYear").trim() : student.getYear();
        String division = (details != null && details.get("division") != null && !details.get("division").isBlank()) ? details.get("division").trim() : student.getDivision();
        String phone = (details != null && details.get("phone") != null && !details.get("phone").isBlank()) ? details.get("phone").trim() : student.getPhone();
        String email = (details != null && details.get("email") != null && !details.get("email").isBlank()) ? details.get("email").trim() : student.getEmail();
        String name = (details != null && details.get("studentName") != null && !details.get("studentName").isBlank()) ? details.get("studentName").trim() : student.getFullName();

        boolean userUpdated = false;
        if ((student.getPrnRollNo() == null || student.getPrnRollNo().isBlank()) && prn != null && !prn.isBlank()) {
            student.setPrnRollNo(prn);
            userUpdated = true;
        }
        if ((student.getCourse() == null || student.getCourse().isBlank()) && course != null && !course.isBlank()) {
            student.setCourse(course);
            userUpdated = true;
        }
        if ((student.getYear() == null || student.getYear().isBlank()) && year != null && !year.isBlank()) {
            student.setYear(year);
            userUpdated = true;
        }
        if ((student.getDivision() == null || student.getDivision().isBlank()) && division != null && !division.isBlank()) {
            student.setDivision(division);
            userUpdated = true;
        }
        if ((student.getPhone() == null || student.getPhone().isBlank()) && phone != null && !phone.isBlank()) {
            student.setPhone(phone);
            userUpdated = true;
        }
        if (userUpdated) {
            userRepository.save(student);
        }

        ClubMember cm;
        if (existing.isPresent()) {
            cm = existing.get();
            if ("APPROVED".equalsIgnoreCase(cm.getStatus())) {
                throw new IllegalArgumentException("You are already an active approved member of " + club.getClubName());
            }
            if ("PENDING".equalsIgnoreCase(cm.getStatus())) {
                throw new IllegalArgumentException("Your application to join " + club.getClubName() + " is already pending review.");
            }
            cm.setStatus("PENDING");
            cm.setJoinReason(reason != null ? reason.trim() : "Interested in participating.");
            cm.setReviewedBy(null);
            cm.setReviewedAt(null);
            cm.setJoinedAt(null);
        } else {
            cm = new ClubMember(club, student, reason != null ? reason.trim() : "Interested in participating.");
        }

        cm.setStudentName(name);
        cm.setEmail(email);
        cm.setPrnRollNo(prn);
        cm.setDepartmentName(dept);
        cm.setCourse(course);
        cm.setAcademicYear(year);
        cm.setDivision(division);
        cm.setPhone(phone);

        ClubMember saved = clubMemberRepository.save(cm);
        notifyLeaderOfNewApplicant(club, student);
        return saved;
    }

    @Transactional
    public ClubMember applyToJoinClub(User student, Long clubId, String reason) {
        return applyToJoinClub(student, clubId, reason, Collections.emptyMap());
    }

    private void notifyLeaderOfNewApplicant(Club club, User student) {
        if (club.getLeader() != null) {
            notificationRepository.save(new Notification(
                    club.getLeader().getId(),
                    "New Membership Application: " + club.getClubName(),
                    student.getFullName() + " applied to join your club.",
                    "CLUB",
                    club.getId()
            ));
        }
    }

    public List<ClubMember> getLeaderPendingJoinRequests(Long leaderId) {
        return clubMemberRepository.findPendingRequestsByLeaderId(leaderId);
    }

    public List<ClubMember> getClubMembers(Long clubId) {
        return clubMemberRepository.findByClubIdOrderByCreatedAtDesc(clubId);
    }

    public List<ClubMember> getApprovedClubMembers(Long clubId) {
        return clubMemberRepository.findByClubIdAndStatus(clubId, "APPROVED");
    }

    public List<ClubMember> getStudentMemberships(Long studentId) {
        return clubMemberRepository.findByStudentId(studentId);
    }

    @Transactional
    public ClubMember approveClubMember(Long membershipId, User reviewer) {
        ClubMember cm = clubMemberRepository.findById(membershipId)
                .orElseThrow(() -> new IllegalArgumentException("Membership application not found"));
        cm.setStatus("APPROVED");
        cm.setReviewedBy(reviewer);
        cm.setReviewedAt(LocalDateTime.now());
        cm.setJoinedAt(LocalDateTime.now());
        ClubMember saved = clubMemberRepository.save(cm);

        notificationRepository.save(new Notification(
                cm.getStudent().getId(),
                "Club Membership Approved: " + cm.getClub().getClubName(),
                "Congratulations! You are now an official member of " + cm.getClub().getClubName() + ".",
                "CLUB",
                cm.getClub().getId()
        ));

        return saved;
    }

    @Transactional
    public ClubMember rejectClubMember(Long membershipId, User reviewer, String reason) {
        ClubMember cm = clubMemberRepository.findById(membershipId)
                .orElseThrow(() -> new IllegalArgumentException("Membership application not found"));
        cm.setStatus("REJECTED");
        cm.setReviewedBy(reviewer);
        cm.setReviewedAt(LocalDateTime.now());
        ClubMember saved = clubMemberRepository.save(cm);

        notificationRepository.save(new Notification(
                cm.getStudent().getId(),
                "Club Application Status: " + cm.getClub().getClubName(),
                "Your application to join " + cm.getClub().getClubName() + " was reviewed: " + (reason != null ? reason : "Declined."),
                "CLUB",
                cm.getClub().getId()
        ));

        return saved;
    }

    public List<Club> getClubsByLeaderId(Long leaderId) {
        return clubRepository.findByLeaderId(leaderId);
    }

    public ClubRequest submitClubRequest(User student, Long clubId, String reason) {
        Club club = getClubById(clubId);
        ClubRequest request = new ClubRequest(student, club, reason.trim());
        return clubRequestRepository.save(request);
    }

    public List<ClubRequest> getAllClubRequests() {
        return clubRequestRepository.findAll();
    }

    public List<ClubRequest> getStudentClubRequests(Long studentId) {
        return clubRequestRepository.findByStudentId(studentId);
    }

    public List<ClubRequest> getPendingClubRequests() {
        return clubRequestRepository.findByStatusOrderByCreatedAtDesc("PENDING");
    }

    public List<ClubRequest> getPendingClubRequestsForDepartment(Long deptId) {
        return clubRequestRepository.findByClubDepartmentIdAndStatusOrderByCreatedAtDesc(deptId, "PENDING");
    }

    public ClubRequest approveClubRequest(Long requestId) {
        return approveClubRequest(requestId, null);
    }

    public ClubRequest approveClubRequest(Long requestId, User reviewer) {
        ClubRequest req = clubRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        req.setStatus("APPROVED");
        req.setReviewedBy(reviewer);
        req.setReviewedAt(LocalDateTime.now());

        Club club = req.getClub();
        User student = req.getStudent();
        student.setRole(Role.CLUB_LEADER);
        student.setDesignation("CLUB_LEADER");
        userRepository.save(student);

        club.setLeader(student);
        clubRepository.save(club);
        ensureClubLeaderMembership(club, student);

        String approverTitle = (reviewer != null) ? reviewer.getFullName() : "Administration";
        notificationRepository.save(new Notification(
                student.getId(),
                "Club Leadership Approved: " + club.getClubName(),
                "Congratulations! Your appointment to lead " + club.getClubName() + " has been approved by " + approverTitle + ".",
                "CLUB",
                club.getId()
        ));

        return clubRequestRepository.save(req);
    }

    public ClubRequest rejectClubRequest(Long requestId) {
        return rejectClubRequest(requestId, null, "Application not accepted at this time.");
    }

    public ClubRequest rejectClubRequest(Long requestId, User reviewer, String reason) {
        ClubRequest req = clubRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        req.setStatus("REJECTED");
        req.setReviewedBy(reviewer);
        req.setReviewedAt(LocalDateTime.now());
        req.setRejectionReason(reason != null ? reason.trim() : "Application rejected by administration.");

        notificationRepository.save(new Notification(
                req.getStudent().getId(),
                "Club Leadership Status: " + req.getClub().getClubName(),
                "Your application for leadership in " + req.getClub().getClubName() + " was reviewed: " + (reason != null ? reason : "Declined."),
                "CLUB",
                req.getClub().getId()
        ));

        return clubRequestRepository.save(req);
    }

    public long countClubs() {
        return clubRepository.count();
    }

    public long countPendingClubRequests() {
        return clubRequestRepository.countByStatus("PENDING");
    }

    public long countPendingClubRequestsForDepartment(Long deptId) {
        return clubRequestRepository.countByClubDepartmentIdAndStatus(deptId, "PENDING");
    }
}
