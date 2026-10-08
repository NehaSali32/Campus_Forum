package com.campusforum.controller;

import com.campusforum.dto.ApiResponse;
import com.campusforum.dto.ChangePasswordRequest;
import com.campusforum.dto.LoginRequest;
import com.campusforum.dto.RegisterRequest;
import com.campusforum.model.User;
import com.campusforum.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String SESSION_USER_KEY = "LOGGED_IN_USER";

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody RegisterRequest request, HttpSession session) {
        try {
            User newUser = userService.register(request);
            session.setAttribute(SESSION_USER_KEY, newUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Registration successful! Welcome to Campus Forum.", newUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<User>> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        try {
            User user = userService.authenticate(request.getIdentifier(), request.getPassword());
            session.setAttribute(SESSION_USER_KEY, user);
            return ResponseEntity.ok(ApiResponse.ok("Login successful! Welcome back, " + user.getFullName() + ".", user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Authentication failed: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpSession session) {
        session.removeAttribute(SESSION_USER_KEY);
        session.invalidate();
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully."));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(HttpSession session) {
        User currentUser = (User) session.getAttribute(SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(org.springframework.http.HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .body(ApiResponse.error("Not logged in"));
        }
        User refreshed = userService.findById(currentUser.getId()).orElse(currentUser);
        session.setAttribute(SESSION_USER_KEY, refreshed);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(org.springframework.http.HttpHeaders.PRAGMA, "no-cache")
                .header(org.springframework.http.HttpHeaders.EXPIRES, "0")
                .body(ApiResponse.ok("Active user", refreshed));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest req, HttpSession session) {
        User currentUser = (User) session.getAttribute(SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }

        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("New passwords do not match."));
        }

        try {
            userService.changePassword(currentUser.getId(), req.getCurrentPassword(), req.getNewPassword());
            return ResponseEntity.ok(ApiResponse.ok("Password changed successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
