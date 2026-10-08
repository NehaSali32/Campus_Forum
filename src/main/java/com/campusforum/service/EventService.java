package com.campusforum.service;

import com.campusforum.dto.EventRequest;
import com.campusforum.model.Department;
import com.campusforum.model.Event;
import com.campusforum.model.EventAttendee;
import com.campusforum.model.User;
import com.campusforum.repository.ClubRepository;
import com.campusforum.repository.DepartmentRepository;
import com.campusforum.repository.EventAttendeeRepository;
import com.campusforum.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventAttendeeRepository attendeeRepository;
    private final DepartmentRepository departmentRepository;
    private final ClubRepository clubRepository;
    private final ClubService clubService;

    public EventService(EventRepository eventRepository,
                        EventAttendeeRepository attendeeRepository,
                        DepartmentRepository departmentRepository,
                        ClubRepository clubRepository,
                        ClubService clubService) {
        this.eventRepository = eventRepository;
        this.attendeeRepository = attendeeRepository;
        this.departmentRepository = departmentRepository;
        this.clubRepository = clubRepository;
        this.clubService = clubService;
    }

    public List<Event> getApprovedEvents(String category, Long departmentId) {
        if (category != null && !category.equalsIgnoreCase("All")) {
            return eventRepository.findByCategoryAndStatusOrderByEventDateAsc(category.trim(), "APPROVED");
        } else if (departmentId != null && departmentId > 0) {
            return eventRepository.findByDepartmentIdAndStatusOrderByEventDateAsc(departmentId, "APPROVED");
        } else {
            return eventRepository.findByStatusOrderByEventDateAsc("APPROVED");
        }
    }

    public List<Event> getAllEventsForAdmin() {
        return eventRepository.findAllByOrderByCreatedAtDesc();
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + id));
    }

    public Event createEvent(EventRequest req, User creator, boolean autoApprove) {
        Event event = new Event();
        event.setTitle(req.getTitle().trim());
        event.setDescription(req.getDescription().trim());
        event.setEventDate(req.getEventDate());
        event.setEventTime(req.getEventTime().trim());
        event.setLocation(req.getLocation().trim());
        event.setCategory(req.getCategory() != null ? req.getCategory().trim() : "Technical");
        event.setOrganizer(req.getOrganizer() != null ? req.getOrganizer().trim() : creator.getFullName());
        event.setImage(req.getImage());
        event.setEndTime(req.getEndTime());
        if (req.getMaxParticipants() != null && req.getMaxParticipants() > 0) {
            event.setMaxParticipants(req.getMaxParticipants());
        }
        event.setRules(req.getRules());
        event.setContactInfo(req.getContactInfo());
        event.setCreatedBy(creator);
        event.setStatus(autoApprove ? "APPROVED" : "PENDING");
        if (autoApprove) {
            event.setApprovedBy(creator);
            event.setApprovedAt(java.time.LocalDateTime.now());
        }

        if (req.getEligibility() != null && !req.getEligibility().isBlank()) {
            event.setEligibility(req.getEligibility().trim());
        } else {
            event.setEligibility("OPEN_TO_ALL");
        }

        if (req.getDepartmentId() != null) {
            departmentRepository.findById(req.getDepartmentId()).ifPresent(event::setDepartment);
        }
        if (req.getClubId() != null) {
            clubRepository.findById(req.getClubId()).ifPresent(c -> {
                event.setClub(c);
                if (event.getDepartment() == null && c.getDepartment() != null) {
                    event.setDepartment(c.getDepartment());
                }
            });
        }
        if (event.getDepartment() == null && creator != null && creator.getDepartment() != null) {
            event.setDepartment(creator.getDepartment());
        }

        return eventRepository.save(event);
    }

    public List<Event> getPendingEvents() {
        return eventRepository.findByStatusOrderByCreatedAtDesc("PENDING");
    }

    public List<Event> getPendingEventsForDepartment(Long deptId) {
        return eventRepository.findByDepartmentIdAndStatusOrderByCreatedAtDesc(deptId, "PENDING");
    }

    public List<Event> getEventsByCreator(Long userId) {
        return eventRepository.findByCreatedByIdOrderByCreatedAtDesc(userId);
    }

    public Event approveEvent(Long id, User approver) {
        Event event = getEventById(id);
        event.setStatus("APPROVED");
        event.setApprovedBy(approver);
        event.setApprovedAt(java.time.LocalDateTime.now());
        event.setRejectionReason(null);
        return eventRepository.save(event);
    }

    public Event rejectEvent(Long id, User approver, String reason) {
        Event event = getEventById(id);
        event.setStatus("REJECTED");
        event.setApprovedBy(approver);
        event.setRejectionReason(reason != null ? reason.trim() : "Request rejected by academic administration.");
        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new IllegalArgumentException("Event not found with ID: " + id);
        }
        attendeeRepository.deleteByEventId(id);
        eventRepository.deleteById(id);
    }

    @Transactional
    public boolean toggleRegistration(Long eventId, User user) {
        Event event = getEventById(eventId);
        Optional<EventAttendee> existing = attendeeRepository.findByEventIdAndUserId(eventId, user.getId());

        if (existing.isPresent()) {
            attendeeRepository.delete(existing.get());
            return false; // Cancelled
        } else {
            // Must be approved to register
            if (!"APPROVED".equalsIgnoreCase(event.getStatus())) {
                throw new IllegalStateException("Registration is unavailable: event is currently " + event.getStatus().toLowerCase() + ".");
            }

            // Check capacity limit
            if (event.getMaxParticipants() != null && event.getMaxParticipants() > 0) {
                long currentCount = attendeeRepository.countByEventId(eventId);
                if (currentCount >= event.getMaxParticipants()) {
                    throw new IllegalStateException("Event has reached its maximum capacity of " + event.getMaxParticipants() + " attendees.");
                }
            }

            // Enforce eligibility rules
            String elig = event.getEligibility() != null ? event.getEligibility() : "OPEN_TO_ALL";
            if ("MEMBERS_ONLY".equalsIgnoreCase(elig)) {
                if (event.getClub() != null) {
                    boolean isMember = clubService.isStudentClubMember(user.getId(), event.getClub().getId());
                    if (!isMember && user.getRole() != com.campusforum.model.Role.ADMIN && user.getRole() != com.campusforum.model.Role.DEAN) {
                        throw new IllegalStateException("Restricted Event: Registration is open exclusively to approved members of " + event.getClub().getClubName() + ". Please join the club first.");
                    }
                }
            } else if ("DEPARTMENT_ONLY".equalsIgnoreCase(elig)) {
                if (event.getDepartment() != null) {
                    if (user.getDepartment() == null || !user.getDepartment().getId().equals(event.getDepartment().getId())) {
                        throw new IllegalStateException("Restricted Event: Registration is open exclusively to students belonging to the " + event.getDepartment().getName() + " department.");
                    }
                }
            }

            attendeeRepository.save(new EventAttendee(event, user));
            return true; // Registered
        }
    }

    public boolean isUserRegistered(Long eventId, Long userId) {
        return attendeeRepository.existsByEventIdAndUserId(eventId, userId);
    }

    public long getAttendeeCount(Long eventId) {
        return attendeeRepository.countByEventId(eventId);
    }

    public List<Event> getUserRegisteredEvents(Long userId) {
        return attendeeRepository.findByUserId(userId)
                .stream()
                .map(EventAttendee::getEvent)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<EventAttendee> getEventAttendees(Long eventId) {
        return attendeeRepository.findByEventId(eventId);
    }

    public List<java.util.Map<String, Object>> getEventAttendeesWithDetails(Long eventId) {
        List<EventAttendee> attendees = attendeeRepository.findByEventId(eventId);
        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        for (EventAttendee a : attendees) {
            User u = a.getUser();
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("attendeeId", a.getId());
            map.put("userId", u != null ? u.getId() : null);
            map.put("fullName", u != null ? u.getFullName() : "N/A");
            map.put("email", u != null ? u.getEmail() : "N/A");
            map.put("prnRollNo", (u != null && u.getPrnRollNo() != null) ? u.getPrnRollNo() : "N/A");
            map.put("phone", (u != null && u.getPhone() != null) ? u.getPhone() : "N/A");
            map.put("department", (u != null && u.getDepartment() != null) ? u.getDepartment().getName() : "General");
            map.put("course", (u != null && u.getCourse() != null) ? u.getCourse() : "B.Tech");
            map.put("year", (u != null && u.getYear() != null) ? u.getYear() : "N/A");
            map.put("division", (u != null && u.getDivision() != null) ? u.getDivision() : "A");
            map.put("registeredAt", a.getRegisteredAt());
            list.add(map);
        }
        return list;
    }

    public boolean canManageEventAttendees(Long eventId, User user) {
        if (user == null) return false;
        if (user.getRole() == com.campusforum.model.Role.ADMIN 
                || user.getRole() == com.campusforum.model.Role.DEAN
                || user.getRole() == com.campusforum.model.Role.HOD
                || user.getRole() == com.campusforum.model.Role.FACULTY) {
            return true;
        }
        Event event = getEventById(eventId);
        if (event.getCreatedBy() != null && event.getCreatedBy().getId().equals(user.getId())) {
            return true;
        }
        if (event.getClub() != null && event.getClub().getLeader() != null && event.getClub().getLeader().getId().equals(user.getId())) {
            return true;
        }
        return false;
    }

    public List<Event> getUpcomingEventsPreview() {
        return eventRepository.findTop5ByStatusOrderByEventDateAsc("APPROVED");
    }

    public long countUpcomingEvents() {
        return eventRepository.countByStatus("APPROVED");
    }

    public long countPendingEvents() {
        return eventRepository.countByStatus("PENDING");
    }

    public long countPendingEventsForDepartment(Long deptId) {
        return eventRepository.countByDepartmentIdAndStatus(deptId, "PENDING");
    }
}
