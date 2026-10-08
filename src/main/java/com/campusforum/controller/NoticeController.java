package com.campusforum.controller;

import com.campusforum.dto.ApiResponse;
import com.campusforum.dto.NoticeRequest;
import com.campusforum.model.Notice;
import com.campusforum.model.Role;
import com.campusforum.model.User;
import com.campusforum.service.FileStorageService;
import com.campusforum.service.NoticeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;
    private final FileStorageService fileStorageService;

    public NoticeController(NoticeService noticeService, FileStorageService fileStorageService) {
        this.noticeService = noticeService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notice>>> getNotices(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String priority) {
        List<Notice> notices = noticeService.getNotices(query, category, departmentId, priority);
        return ResponseEntity.ok(ApiResponse.ok("Notices loaded", notices));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<Notice>>> getRecentNotices() {
        return ResponseEntity.ok(ApiResponse.ok("Recent notices", noticeService.getRecentNotices()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNoticeById(@PathVariable Long id, HttpSession session) {
        try {
            Notice notice = noticeService.getNoticeById(id);
            User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);

            Map<String, Object> data = new HashMap<>();
            data.put("notice", notice);
            data.put("ackCount", noticeService.getAcknowledgementCount(id));
            if (currentUser != null) {
                data.put("isBookmarked", noticeService.isBookmarked(id, currentUser.getId()));
                data.put("hasAcknowledged", noticeService.hasAcknowledged(id, currentUser.getId()));
            } else {
                data.put("isBookmarked", false);
                data.put("hasAcknowledged", false);
            }

            return ResponseEntity.ok(ApiResponse.ok("Notice retrieved", data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Notice>> createNotice(@Valid @RequestBody NoticeRequest request, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Please sign in to publish a notice."));
        }

        // RBAC: Admin, Dean, HOD, and Faculty can publish notices
        boolean isStaffOrAdmin = currentUser.getRole() == Role.ADMIN 
                || currentUser.getRole() == Role.DEAN 
                || currentUser.getRole() == Role.HOD 
                || currentUser.getRole() == Role.FACULTY;

        if (!isStaffOrAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Only faculty members, department heads, deans, and administrators can publish official notices."));
        }

        try {
            Notice created = noticeService.createNotice(request, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Notice published successfully!", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to publish notice: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Notice>> updateNotice(@PathVariable Long id, @Valid @RequestBody NoticeRequest request, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }

        Notice existing = noticeService.getNoticeById(id);
        boolean isStaffOrAdmin = currentUser.getRole() == Role.ADMIN 
                || currentUser.getRole() == Role.DEAN 
                || currentUser.getRole() == Role.HOD 
                || currentUser.getRole() == Role.FACULTY;

        if (!isStaffOrAdmin && !existing.getAuthor().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Permission denied to edit this notice."));
        }

        try {
            Notice updated = noticeService.updateNotice(id, request);
            return ResponseEntity.ok(ApiResponse.ok("Notice updated successfully!", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }

        Notice existing = noticeService.getNoticeById(id);
        boolean isAuthor = existing.getAuthor() != null && existing.getAuthor().getId().equals(currentUser.getId());
        boolean isStaffOrAdmin = currentUser.getRole() == Role.ADMIN 
                || currentUser.getRole() == Role.DEAN 
                || currentUser.getRole() == Role.HOD 
                || currentUser.getRole() == Role.FACULTY;

        if (!isAuthor && !isStaffOrAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Permission denied to delete this notice."));
        }

        try {
            noticeService.deleteNotice(id);
            return ResponseEntity.ok(ApiResponse.ok("Notice deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/bookmark")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleBookmark(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Please sign in to save notices."));
        }

        boolean bookmarked = noticeService.toggleBookmark(id, currentUser);
        Map<String, Object> res = new HashMap<>();
        res.put("bookmarked", bookmarked);
        String msg = bookmarked ? "Notice saved to bookmarks." : "Notice removed from bookmarks.";
        return ResponseEntity.ok(ApiResponse.ok(msg, res));
    }

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<ApiResponse<Map<String, Object>>> acknowledgeNotice(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Please sign in to acknowledge notice."));
        }

        boolean ack = noticeService.acknowledgeNotice(id, currentUser);
        Map<String, Object> res = new HashMap<>();
        res.put("acknowledged", true);
        res.put("totalAcks", noticeService.getAcknowledgementCount(id));
        return ResponseEntity.ok(ApiResponse.ok("Notice successfully acknowledged.", res));
    }

    @GetMapping("/saved")
    public ResponseEntity<ApiResponse<List<Notice>>> getSavedNotices(HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Saved notices", noticeService.getUserSavedNotices(currentUser.getId())));
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadAttachment(@RequestParam("file") MultipartFile file, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        boolean isStaffOrAdmin = currentUser != null && (currentUser.getRole() == Role.ADMIN 
                || currentUser.getRole() == Role.DEAN 
                || currentUser.getRole() == Role.HOD 
                || currentUser.getRole() == Role.FACULTY);

        if (!isStaffOrAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Permission denied to upload notice attachments."));
        }

        try {
            String storedPath = fileStorageService.storeFile(file, "notices");
            Map<String, String> data = new HashMap<>();
            data.put("fileUrl", "/" + storedPath);
            data.put("fileName", file.getOriginalFilename());
            return ResponseEntity.ok(ApiResponse.ok("File uploaded successfully", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Upload failed: " + e.getMessage()));
        }
    }
}
