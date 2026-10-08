package com.campusforum.controller;

import com.campusforum.dto.ApiResponse;
import com.campusforum.model.Department;
import com.campusforum.model.Role;
import com.campusforum.model.User;
import com.campusforum.service.DepartmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Department>>> getAllDepartments() {
        return ResponseEntity.ok(ApiResponse.ok("Departments loaded", departmentService.getAllDepartments()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Department>> getDepartmentById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Department found", departmentService.getDepartmentById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Department>> createDepartment(@RequestBody Map<String, Object> req, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin authorization required"));
        }

        try {
            String name = (String) req.get("name");
            String code = (String) req.get("code");
            String desc = (String) req.get("description");
            Long hodId = req.get("hodId") != null ? Long.valueOf(req.get("hodId").toString()) : null;
            Long managerId = req.get("managerId") != null ? Long.valueOf(req.get("managerId").toString()) : null;

            Department created = departmentService.createDepartment(name, code, desc, hodId, managerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Department created successfully!", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create department: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Department>> updateDepartment(@PathVariable Long id, @RequestBody Map<String, Object> req, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin authorization required"));
        }

        try {
            String name = (String) req.get("name");
            String code = (String) req.get("code");
            String desc = (String) req.get("description");
            Long hodId = req.get("hodId") != null && !req.get("hodId").toString().isBlank() ? Long.valueOf(req.get("hodId").toString()) : null;
            Long managerId = req.get("managerId") != null && !req.get("managerId").toString().isBlank() ? Long.valueOf(req.get("managerId").toString()) : null;

            Department updated = departmentService.updateDepartment(id, name, code, desc, hodId, managerId);
            return ResponseEntity.ok(ApiResponse.ok("Department updated successfully!", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update department: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin authorization required"));
        }

        try {
            departmentService.deleteDepartment(id);
            return ResponseEntity.ok(ApiResponse.ok("Department deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete department: " + e.getMessage()));
        }
    }
}
