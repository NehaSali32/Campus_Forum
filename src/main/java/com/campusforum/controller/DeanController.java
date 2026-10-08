package com.campusforum.controller;

import com.campusforum.dto.ApiResponse;
import com.campusforum.model.*;
import com.campusforum.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dean")
public class DeanController {

    private final UserService userService;
    private final EventService eventService;
    private final ClubService clubService;
    private final DepartmentService departmentService;

    public DeanController(UserService userService,
                          EventService eventService,
                          ClubService clubService,
                          DepartmentService departmentService) {
        this.userService = userService;
        this.eventService = eventService;
        this.clubService = clubService;
        this.departmentService = departmentService;
    }

    private User getDeanUser(HttpSession session) {
        User user = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (user == null) return null;
        if (user.getRole() == Role.DEAN || user.getRole() == Role.ADMIN) {
            return user;
        }
        return null;
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverview(HttpSession session) {
        User dean = getDeanUser(session);
        if (dean == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Dean privileges required."));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("dean", dean);
        data.put("totalStudents", userService.countStudents());
        data.put("totalFaculty", userService.countFaculty());
        data.put("totalHods", userService.countHods());
        data.put("totalDepartments", departmentService.countDepartments());
        data.put("totalClubs", clubService.countClubs());
        data.put("pendingEventsCount", eventService.countPendingEvents());
        data.put("pendingClubRequestsCount", clubService.countPendingClubRequests());

        return ResponseEntity.ok(ApiResponse.ok("Dean institutional overview", data));
    }

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<List<Event>>> getEvents(
            @RequestParam(required = false, defaultValue = "PENDING") String status,
            HttpSession session) {
        User dean = getDeanUser(session);
        if (dean == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Dean privileges required."));
        }

        if ("PENDING".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(ApiResponse.ok("Pending events for institutional approval", eventService.getPendingEvents()));
        }
        return ResponseEntity.ok(ApiResponse.ok("All institutional events", eventService.getAllEventsForAdmin()));
    }

    @PutMapping("/events/{id}/approve")
    public ResponseEntity<ApiResponse<Event>> approveEvent(@PathVariable Long id, HttpSession session) {
        User dean = getDeanUser(session);
        if (dean == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Dean privileges required."));
        }
        try {
            Event approved = eventService.approveEvent(id, dean);
            return ResponseEntity.ok(ApiResponse.ok("Event approved by Dean of Academics", approved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/events/{id}/reject")
    public ResponseEntity<ApiResponse<Event>> rejectEvent(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            HttpSession session) {
        User dean = getDeanUser(session);
        if (dean == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Dean privileges required."));
        }
        try {
            String reason = body != null ? body.get("reason") : "Declined by Dean of Academics.";
            Event rejected = eventService.rejectEvent(id, dean, reason);
            return ResponseEntity.ok(ApiResponse.ok("Event rejected by Dean", rejected));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id, HttpSession session) {
        User dean = getDeanUser(session);
        if (dean == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Dean privileges required."));
        }
        try {
            eventService.deleteEvent(id);
            return ResponseEntity.ok(ApiResponse.ok("Event deleted successfully by Dean."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Event not found or already deleted."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Failed to delete event: " + e.getMessage()));
        }
    }

    @GetMapping("/clubs")
    public ResponseEntity<ApiResponse<List<Club>>> getAllClubs(HttpSession session) {
        User dean = getDeanUser(session);
        if (dean == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Dean privileges required."));
        }
        return ResponseEntity.ok(ApiResponse.ok("Institutional clubs", clubService.getAllClubs()));
    }

    @PostMapping("/clubs")
    public ResponseEntity<ApiResponse<Club>> createClub(@RequestBody Map<String, Object> req, HttpSession session) {
        User dean = getDeanUser(session);
        if (dean == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Dean privileges required."));
        }
        try {
            String name = (String) req.get("clubName");
            String desc = (String) req.get("description");
            Long deptId = req.get("departmentId") != null && !req.get("departmentId").toString().isBlank()
                    ? Long.valueOf(req.get("departmentId").toString()) : null;
            Long leaderId = req.get("leaderId") != null && !req.get("leaderId").toString().isBlank()
                    ? Long.valueOf(req.get("leaderId").toString()) : null;

            Club club = clubService.createClub(name, desc, deptId, leaderId);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Club created successfully by Dean!", club));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create club: " + e.getMessage()));
        }
    }

    @PostMapping("/clubs/{id}/appoint-leader")
    public ResponseEntity<ApiResponse<Club>> appointClubLeader(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req,
            HttpSession session) {
        User dean = getDeanUser(session);
        if (dean == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Dean privileges required."));
        }
        try {
            Long studentId = Long.valueOf(req.get("studentId").toString());
            Club club = clubService.appointClubLeader(id, studentId, dean);
            return ResponseEntity.ok(ApiResponse.ok("Club leader appointed successfully by Dean of Academics!", club));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Leader appointment failed: " + e.getMessage()));
        }
    }

    @PostMapping("/clubs/{id}/remove-leader")
    public ResponseEntity<ApiResponse<Club>> removeClubLeader(@PathVariable Long id, HttpSession session) {
        User dean = getDeanUser(session);
        if (dean == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Dean privileges required."));
        }
        try {
            Club club = clubService.removeClubLeader(id);
            return ResponseEntity.ok(ApiResponse.ok("Club leader removed successfully.", club));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to remove leader: " + e.getMessage()));
        }
    }

    @GetMapping("/faculty-directory")
    public ResponseEntity<ApiResponse<List<User>>> getFacultyDirectory(HttpSession session) {
        User dean = getDeanUser(session);
        if (dean == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Dean privileges required."));
        }
        return ResponseEntity.ok(ApiResponse.ok("Faculty & HOD directory", userService.getAllFaculty()));
    }
}
