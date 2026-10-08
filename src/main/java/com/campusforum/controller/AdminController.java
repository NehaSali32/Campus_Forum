package com.campusforum.controller;

import com.campusforum.dto.ApiResponse;
import com.campusforum.dto.DashboardStats;
import com.campusforum.model.Event;
import com.campusforum.model.ForumTopic;
import com.campusforum.model.Role;
import com.campusforum.model.User;
import com.campusforum.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final NoticeService noticeService;
    private final ForumService forumService;
    private final EventService eventService;
    private final DepartmentService departmentService;
    private final ClubService clubService;

    public AdminController(UserService userService,
                           NoticeService noticeService,
                           ForumService forumService,
                           EventService eventService,
                           DepartmentService departmentService,
                           ClubService clubService) {
        this.userService = userService;
        this.noticeService = noticeService;
        this.forumService = forumService;
        this.eventService = eventService;
        this.departmentService = departmentService;
        this.clubService = clubService;
    }

    private boolean checkAdmin(HttpSession session) {
        User user = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        return user != null && user.getRole() == Role.ADMIN;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStats>> getStats(HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }

        DashboardStats stats = new DashboardStats();
        stats.setTotalStudents(userService.countStudents());
        stats.setTotalFaculty(userService.countFaculty());
        stats.setTotalHods(userService.countHods());
        stats.setTotalDeans(userService.countDeans());
        stats.setTotalDepartments(departmentService.countDepartments());
        stats.setTotalNotices(noticeService.countNotices());
        stats.setUrgentNotices(noticeService.countUrgentNotices());
        stats.setPinnedNotices(noticeService.countPinnedNotices());
        stats.setTotalTopics(forumService.countTopics());
        stats.setTotalComments(forumService.countComments());
        stats.setUpcomingEvents(eventService.countUpcomingEvents());
        stats.setPendingEvents(eventService.countPendingEvents());
        stats.setTotalClubs(clubService.countClubs());
        stats.setPendingClubRequests(clubService.countPendingClubRequests());

        return ResponseEntity.ok(ApiResponse.ok("Dashboard statistics", stats));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String query,
            HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }

        if (role != null && !role.isBlank() && !role.equalsIgnoreCase("ALL")) {
            try {
                Role r = Role.valueOf(role.toUpperCase());
                return ResponseEntity.ok(ApiResponse.ok("Users by role", userService.findByRole(r)));
            } catch (Exception ignored) {
            }
        }

        return ResponseEntity.ok(ApiResponse.ok("All users", userService.searchUsers(query)));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<User>> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req,
            HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }

        try {
            Role role = Role.valueOf(req.get("role").toString().toUpperCase());
            String designation = req.get("designation") != null ? req.get("designation").toString() : null;
            Long departmentId = req.get("departmentId") != null && !req.get("departmentId").toString().isBlank()
                    ? Long.valueOf(req.get("departmentId").toString()) : null;

            User updated = userService.setRoleAndDesignation(id, role, designation, departmentId);
            return ResponseEntity.ok(ApiResponse.ok("User role and designation updated", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Update failed: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<User>> toggleUserStatus(@PathVariable Long id, HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }

        try {
            User updated = userService.toggleStatus(id);
            return ResponseEntity.ok(ApiResponse.ok("User status updated to " + updated.getStatus(), updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id, HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }
        User current = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (current != null && current.getId().equals(id)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Cannot delete your own administrator account."));
        }
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.ok("User deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<List<Event>>> getAllEvents(HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }
        return ResponseEntity.ok(ApiResponse.ok("All events", eventService.getAllEventsForAdmin()));
    }

    @PutMapping("/events/{id}/approve")
    public ResponseEntity<ApiResponse<Event>> approveEvent(@PathVariable Long id, HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }
        User admin = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        Event approved = eventService.approveEvent(id, admin);
        return ResponseEntity.ok(ApiResponse.ok("Event approved successfully", approved));
    }

    @PutMapping("/events/{id}/reject")
    public ResponseEntity<ApiResponse<Event>> rejectEvent(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> payload,
            HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }
        User admin = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        String reason = payload != null ? payload.get("reason") : "Rejected by Administrator.";
        Event rejected = eventService.rejectEvent(id, admin, reason);
        return ResponseEntity.ok(ApiResponse.ok("Event rejected", rejected));
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id, HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }
        try {
            eventService.deleteEvent(id);
            return ResponseEntity.ok(ApiResponse.ok("Event deleted successfully by Administrator."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Event not found or already deleted."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Failed to delete event: " + e.getMessage()));
        }
    }

    // --- STUDENT MANAGEMENT ---
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<User>>> getStudents(HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }
        return ResponseEntity.ok(ApiResponse.ok("All students", userService.getAllStudents()));
    }

    @PostMapping("/students")
    public ResponseEntity<ApiResponse<User>> createStudent(@RequestBody Map<String, Object> req, HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }
        try {
            String fullName = req.get("fullName").toString();
            String email = req.get("email").toString();
            String prn = req.get("prnRollNo") != null ? req.get("prnRollNo").toString() : null;
            String phone = req.get("phone") != null ? req.get("phone").toString() : null;
            Long deptId = req.get("departmentId") != null && !req.get("departmentId").toString().isBlank()
                    ? Long.valueOf(req.get("departmentId").toString()) : null;
            String course = req.get("course") != null ? req.get("course").toString() : "B.Tech";
            String year = req.get("year") != null ? req.get("year").toString() : "Third Year";
            String semester = req.get("semester") != null ? req.get("semester").toString() : "Semester 5";
            String division = req.get("division") != null ? req.get("division").toString() : "A";
            String password = req.get("password") != null ? req.get("password").toString() : "Student@123";

            User created = userService.createStudent(fullName, email, prn, phone, deptId, course, year, semester, division, password);
            return ResponseEntity.ok(ApiResponse.ok("Student created successfully", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<ApiResponse<User>> updateStudent(@PathVariable Long id, @RequestBody Map<String, Object> req, HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }
        try {
            String fullName = req.get("fullName").toString();
            String email = req.get("email").toString();
            String prn = req.get("prnRollNo") != null ? req.get("prnRollNo").toString() : null;
            String phone = req.get("phone") != null ? req.get("phone").toString() : null;
            Long deptId = req.get("departmentId") != null && !req.get("departmentId").toString().isBlank()
                    ? Long.valueOf(req.get("departmentId").toString()) : null;
            String course = req.get("course") != null ? req.get("course").toString() : null;
            String year = req.get("year") != null ? req.get("year").toString() : null;
            String semester = req.get("semester") != null ? req.get("semester").toString() : null;
            String division = req.get("division") != null ? req.get("division").toString() : null;

            User updated = userService.updateStudent(id, fullName, email, prn, phone, deptId, course, year, semester, division);
            return ResponseEntity.ok(ApiResponse.ok("Student updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id, HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.ok("Student deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // --- FACULTY MANAGEMENT ---
    @GetMapping("/faculty")
    public ResponseEntity<ApiResponse<List<User>>> getFaculty(HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }
        return ResponseEntity.ok(ApiResponse.ok("All faculty members", userService.getAllFaculty()));
    }

    @PostMapping("/faculty")
    public ResponseEntity<ApiResponse<User>> createFaculty(@RequestBody Map<String, Object> req, HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }
        try {
            String fullName = req.get("fullName").toString();
            String email = req.get("email").toString();
            String empId = req.get("employeeId") != null ? req.get("employeeId").toString() : null;
            String phone = req.get("phone") != null ? req.get("phone").toString() : null;
            Long deptId = req.get("departmentId") != null && !req.get("departmentId").toString().isBlank()
                    ? Long.valueOf(req.get("departmentId").toString()) : null;
            String designation = req.get("designation") != null ? req.get("designation").toString() : "Assistant Professor";
            String password = req.get("password") != null ? req.get("password").toString() : "Faculty@123";

            User created = userService.createFaculty(fullName, email, empId, phone, deptId, designation, password);
            return ResponseEntity.ok(ApiResponse.ok("Faculty member created successfully", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/faculty/{id}")
    public ResponseEntity<ApiResponse<User>> updateFaculty(@PathVariable Long id, @RequestBody Map<String, Object> req, HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }
        try {
            String fullName = req.get("fullName").toString();
            String email = req.get("email").toString();
            String empId = req.get("employeeId") != null ? req.get("employeeId").toString() : null;
            String phone = req.get("phone") != null ? req.get("phone").toString() : null;
            Long deptId = req.get("departmentId") != null && !req.get("departmentId").toString().isBlank()
                    ? Long.valueOf(req.get("departmentId").toString()) : null;
            String designation = req.get("designation") != null ? req.get("designation").toString() : null;

            User updated = userService.updateFaculty(id, fullName, email, empId, phone, deptId, designation);
            return ResponseEntity.ok(ApiResponse.ok("Faculty member updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/faculty/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFaculty(@PathVariable Long id, HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.ok("Faculty member deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // --- ROLE ASSIGNMENTS (HOD & DEAN) ---
    @PostMapping("/assign-hod")
    public ResponseEntity<ApiResponse<User>> assignHod(@RequestBody Map<String, Object> req, HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }
        try {
            Long facultyId = Long.valueOf(req.get("facultyId").toString());
            Long deptId = Long.valueOf(req.get("departmentId").toString());
            User hod = userService.assignHod(facultyId, deptId);
            return ResponseEntity.ok(ApiResponse.ok("HOD appointed successfully", hod));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/remove-hod")
    public ResponseEntity<ApiResponse<User>> removeHod(@RequestBody Map<String, Object> req, HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }
        try {
            Long facultyId = Long.valueOf(req.get("facultyId").toString());
            User faculty = userService.removeHod(facultyId);
            return ResponseEntity.ok(ApiResponse.ok("HOD role removed successfully", faculty));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/assign-dean")
    public ResponseEntity<ApiResponse<User>> assignDean(@RequestBody Map<String, Object> req, HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }
        try {
            Long facultyId = Long.valueOf(req.get("facultyId").toString());
            String deanTitle = req.get("deanTitle") != null ? req.get("deanTitle").toString() : "Dean of Academics";
            User dean = userService.assignDean(facultyId, deanTitle);
            return ResponseEntity.ok(ApiResponse.ok("Dean role assigned successfully", dean));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/remove-dean")
    public ResponseEntity<ApiResponse<User>> removeDean(@RequestBody Map<String, Object> req, HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }
        try {
            Long facultyId = Long.valueOf(req.get("facultyId").toString());
            User faculty = userService.removeDean(facultyId);
            return ResponseEntity.ok(ApiResponse.ok("Dean role removed successfully", faculty));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/topics/{id}/status")
    public ResponseEntity<ApiResponse<ForumTopic>> updateTopicStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }
        String status = payload.getOrDefault("status", "OPEN");
        try {
            ForumTopic updated = forumService.updateTopicStatus(id, status);
            return ResponseEntity.ok(ApiResponse.ok("Topic status updated", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<com.campusforum.model.ContentReport>>> getReports(
            @RequestParam(required = false) String status,
            HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }
        return ResponseEntity.ok(ApiResponse.ok("Reports loaded", forumService.getReports(status)));
    }

    @PutMapping("/reports/{id}/resolve")
    public ResponseEntity<ApiResponse<com.campusforum.model.ContentReport>> resolveReport(
            @PathVariable Long id,
            HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }
        try {
            return ResponseEntity.ok(ApiResponse.ok("Report resolved", forumService.resolveReport(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/reports/{id}/dismiss")
    public ResponseEntity<ApiResponse<com.campusforum.model.ContentReport>> dismissReport(
            @PathVariable Long id,
            HttpSession session) {
        if (!checkAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required."));
        }
        try {
            return ResponseEntity.ok(ApiResponse.ok("Report dismissed", forumService.dismissReport(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
