package com.campusforum.controller;

import com.campusforum.dto.ApiResponse;
import com.campusforum.model.Role;
import com.campusforum.model.StudentDocument;
import com.campusforum.model.User;
import com.campusforum.service.DocumentService;
import com.campusforum.service.FileStorageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final FileStorageService fileStorageService;

    public DocumentController(DocumentService documentService, FileStorageService fileStorageService) {
        this.documentService = documentService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentDocument>>> getMyDocuments(HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Please sign in to view documents."));
        }

        return ResponseEntity.ok(ApiResponse.ok("Documents loaded", documentService.getDocumentsForStudent(currentUser.getId())));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<StudentDocument>>> getDocumentsForStudent(@PathVariable Long studentId, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }

        // Privacy RBAC: Only Admin or the student himself/herself can view these documents
        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(studentId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access denied to private student documents."));
        }

        return ResponseEntity.ok(ApiResponse.ok("Student documents", documentService.getDocumentsForStudent(studentId)));
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<StudentDocument>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("type") String type,
            @RequestParam(value = "studentId", required = false) Long studentId,
            HttpSession session) {

        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Please sign in to upload documents."));
        }

        Long targetStudentId = (studentId != null && currentUser.getRole() == Role.ADMIN) ? studentId : currentUser.getId();

        try {
            String storedPath = fileStorageService.storeFile(file, "documents");
            StudentDocument doc = documentService.uploadDocument(
                    targetStudentId,
                    title,
                    type,
                    "/" + storedPath,
                    file.getOriginalFilename(),
                    file.getSize()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Document uploaded successfully!", doc));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to upload document: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }

        StudentDocument doc = documentService.getDocumentById(id);
        if (currentUser.getRole() != Role.ADMIN && !doc.getStudent().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Permission denied to delete this document."));
        }

        documentService.deleteDocument(id);
        return ResponseEntity.ok(ApiResponse.ok("Document deleted successfully."));
    }
}
