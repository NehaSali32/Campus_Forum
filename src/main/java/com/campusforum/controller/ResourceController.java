package com.campusforum.controller;

import com.campusforum.dto.ApiResponse;
import com.campusforum.model.CollegeResource;
import com.campusforum.model.Role;
import com.campusforum.model.User;
import com.campusforum.service.FileStorageService;
import com.campusforum.service.ResourceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final FileStorageService fileStorageService;

    public ResourceController(ResourceService resourceService, FileStorageService fileStorageService) {
        this.resourceService = resourceService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CollegeResource>>> getResources(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(ApiResponse.ok("Resources loaded", resourceService.getAllResources(category, departmentId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CollegeResource>> createResource(@RequestBody Map<String, Object> req, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.FACULTY && currentUser.getRole() != Role.HOD && currentUser.getRole() != Role.DEAN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Authorization required to upload resources"));
        }

        try {
            String title = (String) req.get("title");
            String desc = (String) req.get("description");
            String category = (String) req.get("category");
            String type = (String) req.get("resourceType");
            String filePath = (String) req.get("filePath");
            String linkUrl = (String) req.get("linkUrl");
            Long deptId = req.get("departmentId") != null && !req.get("departmentId").toString().isBlank()
                    ? Long.valueOf(req.get("departmentId").toString()) : null;

            CollegeResource res = resourceService.createResource(title, desc, category, deptId, type, filePath, linkUrl);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Resource added successfully!", res));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to add resource: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteResource(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.FACULTY && currentUser.getRole() != Role.HOD && currentUser.getRole() != Role.DEAN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Authorization required"));
        }

        resourceService.deleteResource(id);
        return ResponseEntity.ok(ApiResponse.ok("Resource deleted successfully."));
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadResourceFile(@RequestParam("file") MultipartFile file, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.FACULTY && currentUser.getRole() != Role.HOD && currentUser.getRole() != Role.DEAN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Authorization required"));
        }

        try {
            String storedPath = fileStorageService.storeFile(file, "resources");
            Map<String, String> data = new HashMap<>();
            data.put("fileUrl", "/" + storedPath);
            data.put("fileName", file.getOriginalFilename());
            return ResponseEntity.ok(ApiResponse.ok("Resource file uploaded successfully", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Upload failed: " + e.getMessage()));
        }
    }
}
