package com.campusforum.controller;

import com.campusforum.dto.ApiResponse;
import com.campusforum.dto.EventRequest;
import com.campusforum.model.Event;
import com.campusforum.model.Role;
import com.campusforum.model.User;
import com.campusforum.service.EventService;
import com.campusforum.service.FileStorageService;
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
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final FileStorageService fileStorageService;

    public EventController(EventService eventService, FileStorageService fileStorageService) {
        this.eventService = eventService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Event>>> getEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(ApiResponse.ok("Events loaded", eventService.getApprovedEvents(category, departmentId)));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<Event>>> getUpcomingPreview() {
        return ResponseEntity.ok(ApiResponse.ok("Upcoming events", eventService.getUpcomingEventsPreview()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEventById(@PathVariable Long id, HttpSession session) {
        try {
            Event event = eventService.getEventById(id);
            User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);

            Map<String, Object> data = new HashMap<>();
            data.put("event", event);
            data.put("attendeeCount", eventService.getAttendeeCount(id));
            data.put("isRegistered", (currentUser != null && eventService.isUserRegistered(id, currentUser.getId())));

            return ResponseEntity.ok(ApiResponse.ok("Event retrieved", data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Event>> createEvent(@Valid @RequestBody EventRequest request, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Please sign in to submit an event."));
        }

        boolean autoApprove = (currentUser.getRole() == Role.ADMIN ||
                               currentUser.getRole() == Role.DEAN ||
                               currentUser.getRole() == Role.HOD ||
                               currentUser.getRole() == Role.FACULTY);

        try {
            Event created = eventService.createEvent(request, currentUser, autoApprove);
            String msg = autoApprove ? "Event created and published!" : "Event submitted for administrative review.";
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(msg, created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create event: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Event>> updateEvent(@PathVariable Long id, @Valid @RequestBody EventRequest request, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }

        Event existing = eventService.getEventById(id);
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.DEAN && !existing.getCreatedBy().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Permission denied to edit this event."));
        }

        existing.setTitle(request.getTitle().trim());
        existing.setDescription(request.getDescription().trim());
        existing.setEventDate(request.getEventDate());
        existing.setEventTime(request.getEventTime().trim());
        existing.setLocation(request.getLocation().trim());
        if (request.getCategory() != null) existing.setCategory(request.getCategory().trim());
        if (request.getImage() != null) existing.setImage(request.getImage());

        return ResponseEntity.ok(ApiResponse.ok("Event updated successfully.", existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }

        try {
            Event existing = eventService.getEventById(id);
            Role role = currentUser.getRole();
            boolean isPrivileged = (role == Role.ADMIN 
                    || role == Role.DEAN 
                    || role == Role.HOD 
                    || role == Role.FACULTY);
            boolean isCreator = (existing.getCreatedBy() != null && existing.getCreatedBy().getId().equals(currentUser.getId()));

            if (!isPrivileged && !isCreator) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Permission denied to delete this event."));
            }

            eventService.deleteEvent(id);
            return ResponseEntity.ok(ApiResponse.ok("Event deleted successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Event not found or already deleted."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Failed to delete event: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleRegistration(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Please sign in to register for events."));
        }

        try {
            boolean registered = eventService.toggleRegistration(id, currentUser);
            Map<String, Object> res = new HashMap<>();
            res.put("registered", registered);
            res.put("attendeeCount", eventService.getAttendeeCount(id));
            String msg = registered ? "Registered successfully for event!" : "Event registration cancelled.";
            return ResponseEntity.ok(ApiResponse.ok(msg, res));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/my-events")
    public ResponseEntity<ApiResponse<List<Event>>> getMyEvents(HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        return ResponseEntity.ok(ApiResponse.ok("My registered events", eventService.getUserRegisteredEvents(currentUser.getId())));
    }

    @GetMapping("/my-organized")
    public ResponseEntity<ApiResponse<List<Event>>> getMyOrganizedEvents(HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Events organized by me", eventService.getEventsByCreator(currentUser.getId())));
    }

    @GetMapping("/{id}/attendees")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getEventAttendees(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }

        if (!eventService.canManageEventAttendees(id, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access denied. Only the event organizer, club leader, or academic administration can view registered attendees."));
        }

        List<Map<String, Object>> attendees = eventService.getEventAttendeesWithDetails(id);
        return ResponseEntity.ok(ApiResponse.ok("Registered attendees roster", attendees));
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadEventImage(@RequestParam("file") MultipartFile file, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }

        try {
            String storedPath = fileStorageService.storeFile(file, "events");
            Map<String, String> data = new HashMap<>();
            data.put("fileUrl", "/" + storedPath);
            return ResponseEntity.ok(ApiResponse.ok("Image uploaded successfully", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Upload failed: " + e.getMessage()));
        }
    }
}
