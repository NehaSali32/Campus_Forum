package com.campusforum.controller;

import com.campusforum.dto.ApiResponse;
import com.campusforum.model.Club;
import com.campusforum.model.ClubRequest;
import com.campusforum.model.Role;
import com.campusforum.model.User;
import com.campusforum.service.ClubService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clubs")
public class ClubController {

    private final ClubService clubService;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Club>>> getClubs() {
        return ResponseEntity.ok(ApiResponse.ok("Clubs loaded", clubService.getActiveClubs()));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Club>>> getAllClubs(HttpSession session) {
        return ResponseEntity.ok(ApiResponse.ok("All clubs", clubService.getAllClubs()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Club>> getClubById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Club details", clubService.getClubById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/eligible-leaders")
    public ResponseEntity<ApiResponse<List<User>>> getEligibleLeaders(HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.DEAN && currentUser.getRole() != Role.HOD)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Administrative privileges required"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Eligible student leaders", clubService.getEligibleLeaders()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Club>> createClub(@RequestBody Map<String, Object> req, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.DEAN && currentUser.getRole() != Role.HOD)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Only HOD, Dean, or Admin can create clubs."));
        }

        try {
            String name = (String) req.get("clubName");
            String desc = (String) req.get("description");
            Long deptId = req.get("departmentId") != null && !req.get("departmentId").toString().isBlank()
                    ? Long.valueOf(req.get("departmentId").toString()) : null;
            
            // If HOD creates and doesn't specify department, bind to their department
            if (currentUser.getRole() == Role.HOD && deptId == null && currentUser.getDepartment() != null) {
                deptId = currentUser.getDepartment().getId();
            }

            Long leaderId = req.get("leaderId") != null && !req.get("leaderId").toString().isBlank()
                    ? Long.valueOf(req.get("leaderId").toString()) : null;

            Club created = clubService.createClub(name, desc, deptId, leaderId);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Club created successfully!", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create club: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Club>> updateClub(@PathVariable Long id, @RequestBody Map<String, Object> req, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.DEAN && currentUser.getRole() != Role.HOD)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Administrative privileges required"));
        }

        try {
            String name = (String) req.get("clubName");
            String desc = (String) req.get("description");
            String status = (String) req.get("status");
            Long deptId = req.get("departmentId") != null && !req.get("departmentId").toString().isBlank()
                    ? Long.valueOf(req.get("departmentId").toString()) : null;
            Long leaderId = req.get("leaderId") != null && !req.get("leaderId").toString().isBlank()
                    ? Long.valueOf(req.get("leaderId").toString()) : null;

            Club updated = clubService.updateClub(id, name, desc, deptId, leaderId, status);
            return ResponseEntity.ok(ApiResponse.ok("Club updated successfully!", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update club: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteClub(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.DEAN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin or Dean authorization required"));
        }

        try {
            clubService.deleteClub(id);
            return ResponseEntity.ok(ApiResponse.ok("Club deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete club: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/appoint-leader")
    public ResponseEntity<ApiResponse<Club>> appointLeader(@PathVariable Long id, @RequestBody Map<String, Object> req, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.DEAN && currentUser.getRole() != Role.HOD)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Only HOD, Dean, or Admin can appoint club leaders."));
        }

        try {
            Long studentId = Long.valueOf(req.get("studentId").toString());
            Club club = clubService.appointClubLeader(id, studentId, currentUser);
            return ResponseEntity.ok(ApiResponse.ok("Student appointed as Club Leader successfully!", club));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Leader appointment failed: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/remove-leader")
    public ResponseEntity<ApiResponse<Club>> removeLeader(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.DEAN && currentUser.getRole() != Role.HOD)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Administrative privileges required"));
        }

        try {
            Club club = clubService.removeClubLeader(id);
            return ResponseEntity.ok(ApiResponse.ok("Club leader removed successfully.", club));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to remove leader: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<ApiResponse<com.campusforum.model.ClubMember>> applyToJoinClub(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> req,
            HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Please sign in to apply to join this club."));
        }

        String reason = (req != null && req.containsKey("reason")) ? req.get("reason") : "Interested in participating in club activities.";
        try {
            com.campusforum.model.ClubMember member = clubService.applyToJoinClub(currentUser, id, reason, req);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Application submitted to Club Leader for review!", member));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/leader/requests")
    public ResponseEntity<ApiResponse<List<com.campusforum.model.ClubMember>>> getLeaderPendingRequests(HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }

        List<com.campusforum.model.ClubMember> requests = clubService.getLeaderPendingJoinRequests(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Pending club join requests", requests));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<com.campusforum.model.ClubMember>>> getClubMembers(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Club members", clubService.getClubMembers(id)));
    }

    @GetMapping("/my-memberships")
    public ResponseEntity<ApiResponse<List<com.campusforum.model.ClubMember>>> getMyMemberships(HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }

        return ResponseEntity.ok(ApiResponse.ok("My club memberships", clubService.getStudentMemberships(currentUser.getId())));
    }

    @GetMapping("/my-led-clubs")
    public ResponseEntity<ApiResponse<List<Club>>> getMyLedClubs(HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }

        return ResponseEntity.ok(ApiResponse.ok("Clubs led by user", clubService.getClubsByLeaderId(currentUser.getId())));
    }

    @PutMapping("/memberships/{id}/approve")
    public ResponseEntity<ApiResponse<com.campusforum.model.ClubMember>> approveClubMember(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }

        try {
            com.campusforum.model.ClubMember approved = clubService.approveClubMember(id, currentUser);
            return ResponseEntity.ok(ApiResponse.ok("Member application approved successfully!", approved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/memberships/{id}/reject")
    public ResponseEntity<ApiResponse<com.campusforum.model.ClubMember>> rejectClubMember(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }

        try {
            String reason = body != null ? body.get("reason") : "Declined by Club Leadership.";
            com.campusforum.model.ClubMember rejected = clubService.rejectClubMember(id, currentUser, reason);
            return ResponseEntity.ok(ApiResponse.ok("Member application rejected.", rejected));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<ClubRequest>>> getAllClubRequests(HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }

        if (currentUser.getRole() == Role.ADMIN) {
            return ResponseEntity.ok(ApiResponse.ok("All club requests", clubService.getAllClubRequests()));
        } else {
            return ResponseEntity.ok(ApiResponse.ok("My club requests", clubService.getStudentClubRequests(currentUser.getId())));
        }
    }
}
