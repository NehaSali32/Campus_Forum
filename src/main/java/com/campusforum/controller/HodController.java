package com.campusforum.controller;

import com.campusforum.dto.ApiResponse;
import com.campusforum.model.*;
import com.campusforum.repository.DepartmentRepository;
import com.campusforum.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/hod")
public class HodController {

    private final UserService userService;
    private final EventService eventService;
    private final ClubService clubService;
    private final DepartmentRepository departmentRepository;
    private final ForumService forumService;

    public HodController(UserService userService,
                         EventService eventService,
                         ClubService clubService,
                         DepartmentRepository departmentRepository,
                         ForumService forumService) {
        this.userService = userService;
        this.eventService = eventService;
        this.clubService = clubService;
        this.departmentRepository = departmentRepository;
        this.forumService = forumService;
    }

    private User getHodUser(HttpSession session) {
        User user = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (user == null) return null;
        if (user.getRole() == Role.HOD || user.getRole() == Role.ADMIN) {
            return user;
        }
        return null;
    }

    private Department getHodDepartment(User hod) {
        if (hod == null) return null;
        Optional<Department> dept = departmentRepository.findByHodId(hod.getId());
        if (dept.isPresent()) return dept.get();
        return hod.getDepartment();
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(HttpSession session) {
        User hod = getHodUser(session);
        if (hod == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("HOD privileges required."));
        }
        Department dept = getHodDepartment(hod);
        Long deptId = (dept != null) ? dept.getId() : null;

        Map<String, Object> data = new HashMap<>();
        data.put("hod", hod);
        data.put("department", dept);
        data.put("studentCount", deptId != null ? userService.getDepartmentStudents(deptId).size() : 0);
        data.put("facultyCount", deptId != null ? userService.getDepartmentFaculty(deptId).size() : 0);
        data.put("pendingEventsCount", deptId != null ? eventService.countPendingEventsForDepartment(deptId) : eventService.countPendingEvents());
        data.put("pendingClubRequestsCount", deptId != null ? clubService.countPendingClubRequestsForDepartment(deptId) : clubService.countPendingClubRequests());

        return ResponseEntity.ok(ApiResponse.ok("HOD Profile and Metrics", data));
    }

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<User>>> getStudents(HttpSession session) {
        User hod = getHodUser(session);
        if (hod == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("HOD privileges required."));
        }
        Department dept = getHodDepartment(hod);
        if (dept == null) {
            return ResponseEntity.ok(ApiResponse.ok("No department assigned", Collections.emptyList()));
        }
        return ResponseEntity.ok(ApiResponse.ok("Department students", userService.getDepartmentStudents(dept.getId())));
    }

    @GetMapping("/faculty")
    public ResponseEntity<ApiResponse<List<User>>> getFaculty(HttpSession session) {
        User hod = getHodUser(session);
        if (hod == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("HOD privileges required."));
        }
        Department dept = getHodDepartment(hod);
        if (dept == null) {
            return ResponseEntity.ok(ApiResponse.ok("No department assigned", Collections.emptyList()));
        }
        return ResponseEntity.ok(ApiResponse.ok("Department faculty", userService.getDepartmentFaculty(dept.getId())));
    }

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<List<Event>>> getEvents(
            @RequestParam(required = false, defaultValue = "PENDING") String status,
            HttpSession session) {
        User hod = getHodUser(session);
        if (hod == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("HOD privileges required."));
        }
        Department dept = getHodDepartment(hod);
        List<Event> events;
        if (dept != null) {
            events = eventService.getPendingEventsForDepartment(dept.getId());
        } else {
            events = eventService.getPendingEvents();
        }
        return ResponseEntity.ok(ApiResponse.ok("Department events for review", events));
    }

    @PutMapping("/events/{id}/approve")
    public ResponseEntity<ApiResponse<Event>> approveEvent(@PathVariable Long id, HttpSession session) {
        User hod = getHodUser(session);
        if (hod == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("HOD privileges required."));
        }
        try {
            Event approved = eventService.approveEvent(id, hod);
            return ResponseEntity.ok(ApiResponse.ok("Event approved successfully by HOD", approved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/events/{id}/reject")
    public ResponseEntity<ApiResponse<Event>> rejectEvent(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            HttpSession session) {
        User hod = getHodUser(session);
        if (hod == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("HOD privileges required."));
        }
        try {
            String reason = body != null ? body.get("reason") : "Declined by Head of Department.";
            Event rejected = eventService.rejectEvent(id, hod, reason);
            return ResponseEntity.ok(ApiResponse.ok("Event rejected by HOD", rejected));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id, HttpSession session) {
        User hod = getHodUser(session);
        if (hod == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("HOD privileges required."));
        }
        try {
            eventService.deleteEvent(id);
            return ResponseEntity.ok(ApiResponse.ok("Event proposal deleted successfully by HOD."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Event not found or already deleted."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Failed to delete event: " + e.getMessage()));
        }
    }

    @GetMapping("/clubs")
    public ResponseEntity<ApiResponse<List<Club>>> getDepartmentClubs(HttpSession session) {
        User hod = getHodUser(session);
        if (hod == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("HOD privileges required."));
        }
        Department dept = getHodDepartment(hod);
        List<Club> clubs = (dept != null) ? clubService.getClubsByDepartment(dept.getId()) : clubService.getAllClubs();
        return ResponseEntity.ok(ApiResponse.ok("Department clubs", clubs));
    }

    @PostMapping("/clubs")
    public ResponseEntity<ApiResponse<Club>> createDepartmentClub(@RequestBody Map<String, Object> req, HttpSession session) {
        User hod = getHodUser(session);
        if (hod == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("HOD privileges required."));
        }
        Department dept = getHodDepartment(hod);
        try {
            String name = (String) req.get("clubName");
            String desc = (String) req.get("description");
            Long deptId = (dept != null) ? dept.getId() : null;
            Long leaderId = req.get("leaderId") != null && !req.get("leaderId").toString().isBlank()
                    ? Long.valueOf(req.get("leaderId").toString()) : null;

            Club club = clubService.createClub(name, desc, deptId, leaderId);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Club created successfully by HOD!", club));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create club: " + e.getMessage()));
        }
    }

    @PostMapping("/clubs/{id}/appoint-leader")
    public ResponseEntity<ApiResponse<Club>> appointClubLeader(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req,
            HttpSession session) {
        User hod = getHodUser(session);
        if (hod == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("HOD privileges required."));
        }
        try {
            Long studentId = Long.valueOf(req.get("studentId").toString());
            Club club = clubService.appointClubLeader(id, studentId, hod);
            return ResponseEntity.ok(ApiResponse.ok("Club leader appointed successfully by HOD!", club));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Leader appointment failed: " + e.getMessage()));
        }
    }

    @PostMapping("/clubs/{id}/remove-leader")
    public ResponseEntity<ApiResponse<Club>> removeClubLeader(@PathVariable Long id, HttpSession session) {
        User hod = getHodUser(session);
        if (hod == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("HOD privileges required."));
        }
        try {
            Club club = clubService.removeClubLeader(id);
            return ResponseEntity.ok(ApiResponse.ok("Club leader removed successfully.", club));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to remove leader: " + e.getMessage()));
        }
    }

    @GetMapping("/discussions")
    public ResponseEntity<ApiResponse<List<ForumTopic>>> getDepartmentDiscussions(HttpSession session) {
        User hod = getHodUser(session);
        if (hod == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("HOD privileges required."));
        }
        Department dept = getHodDepartment(hod);
        Long deptId = (dept != null) ? dept.getId() : null;
        List<ForumTopic> topics = forumService.getDepartmentTopics(deptId);
        return ResponseEntity.ok(ApiResponse.ok("Department academic discussions", topics));
    }
}