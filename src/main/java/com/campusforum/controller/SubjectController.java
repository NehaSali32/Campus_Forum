package com.campusforum.controller;

import com.campusforum.dto.ApiResponse;
import com.campusforum.model.Role;
import com.campusforum.model.Subject;
import com.campusforum.model.User;
import com.campusforum.service.SubjectService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Subject>>> getAllSubjects() {
        return ResponseEntity.ok(ApiResponse.ok("Subjects loaded", subjectService.getAllSubjects()));
    }

    @GetMapping("/department/{deptId}")
    public ResponseEntity<ApiResponse<List<Subject>>> getSubjectsByDepartment(@PathVariable Long deptId) {
        return ResponseEntity.ok(ApiResponse.ok("Department subjects", subjectService.getSubjectsByDepartment(deptId)));
    }

    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<ApiResponse<List<Subject>>> getSubjectsByFaculty(@PathVariable Long facultyId) {
        return ResponseEntity.ok(ApiResponse.ok("Faculty subjects", subjectService.getSubjectsByFaculty(facultyId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Subject>> createSubject(@RequestBody Map<String, Object> req, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.FACULTY)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Authorization required"));
        }

        try {
            String name = (String) req.get("subjectName");
            String code = (String) req.get("subjectCode");
            String semester = (String) req.get("semester");
            Long deptId = Long.valueOf(req.get("departmentId").toString());
            Long facultyId = req.get("facultyId") != null && !req.get("facultyId").toString().isBlank()
                    ? Long.valueOf(req.get("facultyId").toString()) : null;

            Subject created = subjectService.createSubject(name, code, semester, deptId, facultyId);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Subject created successfully!", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create subject: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Subject>> updateSubject(@PathVariable Long id, @RequestBody Map<String, Object> req, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.FACULTY)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Authorization required"));
        }

        try {
            String name = (String) req.get("subjectName");
            String code = (String) req.get("subjectCode");
            String semester = (String) req.get("semester");
            Long deptId = Long.valueOf(req.get("departmentId").toString());
            Long facultyId = req.get("facultyId") != null && !req.get("facultyId").toString().isBlank()
                    ? Long.valueOf(req.get("facultyId").toString()) : null;

            Subject updated = subjectService.updateSubject(id, name, code, semester, deptId, facultyId);
            return ResponseEntity.ok(ApiResponse.ok("Subject updated successfully!", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update subject: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin authorization required"));
        }

        try {
            subjectService.deleteSubject(id);
            return ResponseEntity.ok(ApiResponse.ok("Subject deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
