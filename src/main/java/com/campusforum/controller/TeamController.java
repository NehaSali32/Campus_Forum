package com.campusforum.controller;

import com.campusforum.dto.ApiResponse;
import com.campusforum.model.Role;
import com.campusforum.model.TeamMember;
import com.campusforum.model.User;
import com.campusforum.service.TeamMemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/team")
public class TeamController {

    private final TeamMemberService teamMemberService;

    public TeamController(TeamMemberService teamMemberService) {
        this.teamMemberService = teamMemberService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TeamMember>>> getTeamMembers() {
        return ResponseEntity.ok(ApiResponse.ok("Team members loaded", teamMemberService.getAllMembers()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TeamMember>> createTeamMember(@RequestBody Map<String, String> req, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin authorization required"));
        }

        try {
            TeamMember created = teamMemberService.createMember(
                    req.get("name"),
                    req.get("role"),
                    req.get("bio"),
                    req.get("image"),
                    req.get("email"),
                    req.get("linkedin"),
                    req.get("github")
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Team member created!", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create team member: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamMember>> updateTeamMember(@PathVariable Long id, @RequestBody Map<String, String> req, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin authorization required"));
        }

        try {
            TeamMember updated = teamMemberService.updateMember(
                    id,
                    req.get("name"),
                    req.get("role"),
                    req.get("bio"),
                    req.get("image"),
                    req.get("email"),
                    req.get("linkedin"),
                    req.get("github")
            );
            return ResponseEntity.ok(ApiResponse.ok("Team member updated!", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update team member: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTeamMember(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin authorization required"));
        }

        teamMemberService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.ok("Team member deleted."));
    }
}
