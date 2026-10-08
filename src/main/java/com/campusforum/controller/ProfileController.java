package com.campusforum.controller;

import com.campusforum.dto.ApiResponse;
import com.campusforum.dto.ProfileUpdateRequest;
import com.campusforum.model.User;
import com.campusforum.service.FileStorageService;
import com.campusforum.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    public ProfileController(UserService userService, FileStorageService fileStorageService) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<User>> getProfile(HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        User refreshed = userService.findById(currentUser.getId()).orElse(currentUser);
        return ResponseEntity.ok(ApiResponse.ok("User profile", refreshed));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<User>> updateProfile(@RequestBody ProfileUpdateRequest req, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }

        try {
            User updated = userService.updateProfile(currentUser.getId(), req);
            session.setAttribute(AuthController.SESSION_USER_KEY, updated);
            return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully!", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update profile: " + e.getMessage()));
        }
    }

    @PostMapping("/upload-photo")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadProfilePhoto(@RequestParam("file") MultipartFile file, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }

        try {
            String storedPath = fileStorageService.storeFile(file, "profiles");
            String photoUrl = "/" + storedPath;

            ProfileUpdateRequest req = new ProfileUpdateRequest();
            req.setProfileImage(photoUrl);
            User updated = userService.updateProfile(currentUser.getId(), req);
            session.setAttribute(AuthController.SESSION_USER_KEY, updated);

            Map<String, String> data = new HashMap<>();
            data.put("profileImage", photoUrl);
            return ResponseEntity.ok(ApiResponse.ok("Profile photo updated!", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Photo upload failed: " + e.getMessage()));
        }
    }
}
