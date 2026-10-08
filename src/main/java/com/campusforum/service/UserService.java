package com.campusforum.service;

import com.campusforum.dto.ProfileUpdateRequest;
import com.campusforum.dto.RegisterRequest;
import com.campusforum.model.*;
import com.campusforum.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;
    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ClubRequestRepository clubRequestRepository;
    private final EventRepository eventRepository;
    private final EventAttendeeRepository eventAttendeeRepository;
    private final StudentDocumentRepository studentDocumentRepository;
    private final NoticeRepository noticeRepository;
    private final NoticeBookmarkRepository noticeBookmarkRepository;
    private final NoticeAcknowledgementRepository noticeAcknowledgementRepository;
    private final ForumTopicRepository forumTopicRepository;
    private final ForumCommentRepository forumCommentRepository;
    private final DiscussionUpvoteRepository discussionUpvoteRepository;
    private final DiscussionBookmarkRepository discussionBookmarkRepository;
    private final ContentReportRepository contentReportRepository;
    private final NotificationRepository notificationRepository;

    public UserService(UserRepository userRepository,
                       DepartmentRepository departmentRepository,
                       SubjectRepository subjectRepository,
                       ClubRepository clubRepository,
                       ClubMemberRepository clubMemberRepository,
                       ClubRequestRepository clubRequestRepository,
                       EventRepository eventRepository,
                       EventAttendeeRepository eventAttendeeRepository,
                       StudentDocumentRepository studentDocumentRepository,
                       NoticeRepository noticeRepository,
                       NoticeBookmarkRepository noticeBookmarkRepository,
                       NoticeAcknowledgementRepository noticeAcknowledgementRepository,
                       ForumTopicRepository forumTopicRepository,
                       ForumCommentRepository forumCommentRepository,
                       DiscussionUpvoteRepository discussionUpvoteRepository,
                       DiscussionBookmarkRepository discussionBookmarkRepository,
                       ContentReportRepository contentReportRepository,
                       NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.subjectRepository = subjectRepository;
        this.clubRepository = clubRepository;
        this.clubMemberRepository = clubMemberRepository;
        this.clubRequestRepository = clubRequestRepository;
        this.eventRepository = eventRepository;
        this.eventAttendeeRepository = eventAttendeeRepository;
        this.studentDocumentRepository = studentDocumentRepository;
        this.noticeRepository = noticeRepository;
        this.noticeBookmarkRepository = noticeBookmarkRepository;
        this.noticeAcknowledgementRepository = noticeAcknowledgementRepository;
        this.forumTopicRepository = forumTopicRepository;
        this.forumCommentRepository = forumCommentRepository;
        this.discussionUpvoteRepository = discussionUpvoteRepository;
        this.discussionBookmarkRepository = discussionBookmarkRepository;
        this.contentReportRepository = contentReportRepository;
        this.notificationRepository = notificationRepository;
    }

    private static final org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder =
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(10);

    public static String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public static String legacySha256(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(("campus_connect_salt_" + password).getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public static boolean verifyPassword(String rawPassword, String storedHash) {
        if (storedHash == null || rawPassword == null) return false;
        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
            try {
                return passwordEncoder.matches(rawPassword, storedHash);
            } catch (Exception e) {
                return false;
            }
        }
        return storedHash.equals(legacySha256(rawPassword));
    }

    public User register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        if (req.getPrnRollNo() != null && !req.getPrnRollNo().isBlank() &&
                userRepository.existsByPrnRollNo(req.getPrnRollNo().trim().toUpperCase())) {
            throw new IllegalArgumentException("PRN/Roll No is already registered.");
        }

        User user = new User();
        user.setFullName(req.getFullName().trim());
        user.setEmail(req.getEmail().trim().toLowerCase());
        if (req.getPrnRollNo() != null && !req.getPrnRollNo().isBlank()) {
            user.setPrnRollNo(req.getPrnRollNo().trim().toUpperCase());
        }
        user.setPassword(hashPassword(req.getPassword()));
        user.setRole(Role.STUDENT);
        user.setDesignation("STUDENT");
        user.setStatus("ACTIVE");
        user.setSemester(req.getSemester() != null && !req.getSemester().isBlank() ? req.getSemester().trim() : "Semester 1");
        user.setYear(req.getYear() != null && !req.getYear().isBlank() ? req.getYear().trim() : "First Year");

        if (req.getDepartment() != null && !req.getDepartment().isBlank()) {
            Optional<Department> deptOpt = departmentRepository.findByName(req.getDepartment().trim());
            if (deptOpt.isEmpty()) {
                deptOpt = departmentRepository.findByCode(req.getDepartment().trim());
            }
            deptOpt.ifPresent(user::setDepartment);
        }

        return userRepository.save(user);
    }

    public User authenticate(String identifier, String rawPassword) {
        String trimmed = identifier.trim();
        // Check email
        Optional<User> userOpt = userRepository.findByEmail(trimmed.toLowerCase());
        // Check PRN
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByPrnRollNo(trimmed.toUpperCase());
        }
        // Check Employee ID
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmployeeId(trimmed.toUpperCase());
        }

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with provided Email, PRN, or Employee ID.");
        }

        User user = userOpt.get();
        if ("INACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalArgumentException("This account is currently deactivated. Please contact campus admin.");
        }

        if (!verifyPassword(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password. Please check your credentials.");
        }

        // Auto-upgrade legacy hash to BCrypt
        if (!user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$") && !user.getPassword().startsWith("$2y$")) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
        }

        return user;
    }

    public User changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!verifyPassword(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password does not match.");
        }

        user.setPassword(hashPassword(newPassword));
        return userRepository.save(user);
    }

    public User updateProfile(Long userId, ProfileUpdateRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            user.setFullName(req.getFullName().trim());
        }
        if (req.getPhone() != null) user.setPhone(req.getPhone().trim());
        if (req.getDateOfBirth() != null) user.setDateOfBirth(req.getDateOfBirth());
        if (req.getAddress() != null) user.setAddress(req.getAddress().trim());
        if (req.getCasteCategory() != null) user.setCasteCategory(req.getCasteCategory().trim());
        if (req.getBio() != null) user.setBio(req.getBio().trim());
        if (req.getProfileImage() != null) user.setProfileImage(req.getProfileImage());
        if (req.getLinkedin() != null) user.setLinkedin(req.getLinkedin().trim());
        if (req.getGithub() != null) user.setGithub(req.getGithub().trim());
        if (req.getSemester() != null) user.setSemester(req.getSemester().trim());
        if (req.getYear() != null) user.setYear(req.getYear().trim());
        if (req.getPrnRollNo() != null && !req.getPrnRollNo().isBlank()) {
            String cleanPrn = req.getPrnRollNo().trim().toUpperCase();
            if (!cleanPrn.equalsIgnoreCase(user.getPrnRollNo()) && userRepository.existsByPrnRollNo(cleanPrn)) {
                throw new IllegalArgumentException("PRN/Roll No already taken: " + cleanPrn);
            }
            user.setPrnRollNo(cleanPrn);
        }

        if (req.getDepartmentId() != null) {
            departmentRepository.findById(req.getDepartmentId()).ifPresent(user::setDepartment);
        }

        return userRepository.save(user);
    }

    public User setRoleAndDesignation(Long userId, Role role, String designation, Long departmentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRole(role);
        if (designation != null && !designation.isBlank()) {
            user.setDesignation(designation);
        }
        if (departmentId != null) {
            departmentRepository.findById(departmentId).ifPresent(user::setDepartment);
        }
        return userRepository.save(user);
    }

    public User toggleStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setStatus("ACTIVE".equalsIgnoreCase(user.getStatus()) ? "INACTIVE" : "ACTIVE");
        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public List<User> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return userRepository.findAll();
        }
        return userRepository.searchUsers(query.trim());
    }

    public List<User> getAllStudents() {
        return userRepository.findByRoleInOrderByFullNameAsc(List.of(Role.STUDENT, Role.CLUB_LEADER, Role.EVENT_LEADER));
    }

    public List<User> getAllFaculty() {
        return userRepository.findByRoleInOrderByFullNameAsc(List.of(Role.FACULTY, Role.HOD, Role.DEAN));
    }

    public List<User> getDepartmentStudents(Long deptId) {
        return userRepository.findByDepartmentIdAndRoleIn(deptId, List.of(Role.STUDENT, Role.CLUB_LEADER, Role.EVENT_LEADER));
    }

    public List<User> getDepartmentFaculty(Long deptId) {
        return userRepository.findByDepartmentIdAndRoleIn(deptId, List.of(Role.FACULTY, Role.HOD, Role.DEAN));
    }

    public User createStudent(String fullName, String email, String prn, String phone, Long deptId, String course, String year, String semester, String division, String password) {
        String cleanEmail = email.trim().toLowerCase();
        String cleanPrn = (prn != null && !prn.isBlank()) ? prn.trim().toUpperCase() : null;

        if (userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("Email already in use: " + cleanEmail);
        }
        if (cleanPrn != null && userRepository.existsByPrnRollNo(cleanPrn)) {
            throw new IllegalArgumentException("PRN/Roll No already in use: " + cleanPrn);
        }

        User student = new User();
        student.setFullName(fullName.trim());
        student.setEmail(cleanEmail);
        student.setPrnRollNo(cleanPrn);
        student.setPhone(phone != null ? phone.trim() : null);
        student.setCourse(course != null && !course.isBlank() ? course.trim() : "B.Tech");
        student.setYear(year != null && !year.isBlank() ? year.trim() : "Third Year");
        student.setSemester(semester != null && !semester.isBlank() ? semester.trim() : "Semester 5");
        student.setDivision(division != null && !division.isBlank() ? division.trim() : "A");
        student.setRole(Role.STUDENT);
        student.setDesignation("STUDENT");
        student.setStatus("ACTIVE");

        String rawPass = (password != null && !password.isBlank()) ? password : "Student@123";
        student.setPassword(hashPassword(rawPass));

        if (deptId != null) {
            departmentRepository.findById(deptId).ifPresent(student::setDepartment);
        }

        return userRepository.save(student);
    }

    public User createStudent(String fullName, String email, String prn, String phone, Long deptId, String course, String year, String division, String password) {
        return createStudent(fullName, email, prn, phone, deptId, course, year, "Semester 5", division, password);
    }

    public User updateStudent(Long id, String fullName, String email, String prn, String phone, Long deptId, String course, String year, String semester, String division) {
        User student = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + id));

        String cleanEmail = email.trim().toLowerCase();
        String cleanPrn = (prn != null && !prn.isBlank()) ? prn.trim().toUpperCase() : null;

        if (!student.getEmail().equalsIgnoreCase(cleanEmail) && userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("Email already taken: " + cleanEmail);
        }
        if (cleanPrn != null && !cleanPrn.equalsIgnoreCase(student.getPrnRollNo()) && userRepository.existsByPrnRollNo(cleanPrn)) {
            throw new IllegalArgumentException("PRN/Roll No already taken: " + cleanPrn);
        }

        student.setFullName(fullName.trim());
        student.setEmail(cleanEmail);
        student.setPrnRollNo(cleanPrn);
        if (phone != null) student.setPhone(phone.trim());
        if (course != null && !course.isBlank()) student.setCourse(course.trim());
        if (year != null && !year.isBlank()) student.setYear(year.trim());
        if (semester != null && !semester.isBlank()) student.setSemester(semester.trim());
        if (division != null && !division.isBlank()) student.setDivision(division.trim());

        if (deptId != null) {
            departmentRepository.findById(deptId).ifPresent(student::setDepartment);
        }

        return userRepository.save(student);
    }

    public User updateStudent(Long id, String fullName, String email, String prn, String phone, Long deptId, String course, String year, String division) {
        return updateStudent(id, fullName, email, prn, phone, deptId, course, year, null, division);
    }

    public User createFaculty(String fullName, String email, String empId, String phone, Long deptId, String designation, String password) {
        String cleanEmail = email.trim().toLowerCase();
        String cleanEmpId = (empId != null && !empId.isBlank()) ? empId.trim().toUpperCase() : null;

        if (userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("Email already in use: " + cleanEmail);
        }
        if (cleanEmpId != null && userRepository.existsByEmployeeId(cleanEmpId)) {
            throw new IllegalArgumentException("Employee ID already in use: " + cleanEmpId);
        }

        User faculty = new User();
        faculty.setFullName(fullName.trim());
        faculty.setEmail(cleanEmail);
        faculty.setEmployeeId(cleanEmpId);
        faculty.setPhone(phone != null ? phone.trim() : null);
        faculty.setDesignation((designation != null && !designation.isBlank()) ? designation.trim() : "Assistant Professor");
        faculty.setRole(Role.FACULTY);
        faculty.setStatus("ACTIVE");

        String rawPass = (password != null && !password.isBlank()) ? password : "Faculty@123";
        faculty.setPassword(hashPassword(rawPass));

        if (deptId != null) {
            departmentRepository.findById(deptId).ifPresent(faculty::setDepartment);
        }

        return userRepository.save(faculty);
    }

    public User updateFaculty(Long id, String fullName, String email, String empId, String phone, Long deptId, String designation) {
        User faculty = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Faculty member not found with ID: " + id));

        String cleanEmail = email.trim().toLowerCase();
        String cleanEmpId = (empId != null && !empId.isBlank()) ? empId.trim().toUpperCase() : null;

        if (!faculty.getEmail().equalsIgnoreCase(cleanEmail) && userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("Email already taken: " + cleanEmail);
        }
        if (cleanEmpId != null && !cleanEmpId.equalsIgnoreCase(faculty.getEmployeeId()) && userRepository.existsByEmployeeId(cleanEmpId)) {
            throw new IllegalArgumentException("Employee ID already taken: " + cleanEmpId);
        }

        faculty.setFullName(fullName.trim());
        faculty.setEmail(cleanEmail);
        faculty.setEmployeeId(cleanEmpId);
        if (phone != null) faculty.setPhone(phone.trim());
        if (designation != null && !designation.isBlank()) faculty.setDesignation(designation.trim());

        if (deptId != null) {
            departmentRepository.findById(deptId).ifPresent(faculty::setDepartment);
        }

        return userRepository.save(faculty);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        // 1. Unbind HOD and Manager in departments
        departmentRepository.unbindHod(id);
        departmentRepository.unbindManager(id);

        // 2. Unbind faculty in subjects
        subjectRepository.unbindFaculty(id);

        // 3. Unbind club leader
        clubRepository.unbindLeader(id);

        // 4. Club member & request records
        clubMemberRepository.deleteByStudentId(id);
        clubMemberRepository.unbindReviewedBy(id);
        clubRequestRepository.deleteByStudentId(id);
        clubRequestRepository.unbindReviewedBy(id);

        // 5. Event attendees & reviewer/approver unbind
        eventAttendeeRepository.deleteByUserId(id);
        eventRepository.unbindApprovedBy(id);

        // 6. Delete events authored by this user (first cascading event attendees)
        List<Event> userEvents = eventRepository.findByCreatedByIdOrderByCreatedAtDesc(id);
        for (Event event : userEvents) {
            eventAttendeeRepository.deleteByEventId(event.getId());
            eventRepository.delete(event);
        }

        // 7. Student uploaded documents
        studentDocumentRepository.deleteByStudentId(id);

        // 8. Notice bookmarks and acknowledgements
        noticeBookmarkRepository.deleteByUserId(id);
        noticeAcknowledgementRepository.deleteByUserId(id);

        // 9. Notices authored by this user (first cascading bookmarks and acknowledgements)
        List<Notice> userNotices = noticeRepository.findByAuthorIdOrderByCreatedAtDesc(id);
        for (Notice notice : userNotices) {
            noticeBookmarkRepository.deleteByNoticeId(notice.getId());
            noticeAcknowledgementRepository.deleteByNoticeId(notice.getId());
            noticeRepository.delete(notice);
        }

        // 10. Discussion upvotes and bookmarks
        discussionUpvoteRepository.deleteByUserId(id);
        discussionBookmarkRepository.deleteByUserId(id);

        // 11. Forum comments written by this user
        forumCommentRepository.deleteByAuthorId(id);

        // 12. Forum topics authored by this user (first cascading upvotes, bookmarks, comments)
        List<ForumTopic> userTopics = forumTopicRepository.findByAuthorIdOrderByCreatedAtDesc(id);
        for (ForumTopic topic : userTopics) {
            discussionUpvoteRepository.deleteByTopicId(topic.getId());
            discussionBookmarkRepository.deleteByTopicId(topic.getId());
            forumCommentRepository.deleteByTopicId(topic.getId());
            forumTopicRepository.delete(topic);
        }

        // 13. Content reports submitted by this user
        contentReportRepository.deleteByReporterId(id);

        // 14. Notifications for this user
        notificationRepository.deleteByUserId(id);

        // 15. Finally, delete the user record
        userRepository.delete(user);
    }

    public User assignHod(Long facultyId, Long deptId) {
        User faculty = userRepository.findById(facultyId)
                .orElseThrow(() -> new IllegalArgumentException("Faculty member not found with ID: " + facultyId));
        Department department = departmentRepository.findById(deptId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + deptId));

        // If another faculty is already HOD of this department, demote that faculty to FACULTY
        if (department.getHod() != null && !department.getHod().getId().equals(faculty.getId())) {
            User oldHod = department.getHod();
            oldHod.setRole(Role.FACULTY);
            oldHod.setDesignation("Professor");
            userRepository.save(oldHod);
        }

        // Set faculty as HOD
        faculty.setRole(Role.HOD);
        faculty.setDesignation("Head of Department");
        faculty.setDepartment(department);
        User saved = userRepository.save(faculty);

        department.setHod(saved);
        departmentRepository.save(department);

        return saved;
    }

    public User removeHod(Long facultyId) {
        User faculty = userRepository.findById(facultyId)
                .orElseThrow(() -> new IllegalArgumentException("Faculty member not found with ID: " + facultyId));

        departmentRepository.findByHodId(faculty.getId()).ifPresent(dept -> {
            dept.setHod(null);
            departmentRepository.save(dept);
        });

        faculty.setRole(Role.FACULTY);
        faculty.setDesignation("Professor");
        return userRepository.save(faculty);
    }

    public User assignDean(Long facultyId, String deanTitle) {
        User faculty = userRepository.findById(facultyId)
                .orElseThrow(() -> new IllegalArgumentException("Faculty member not found with ID: " + facultyId));

        faculty.setRole(Role.DEAN);
        faculty.setDesignation((deanTitle != null && !deanTitle.isBlank()) ? deanTitle.trim() : "Dean of Academics");
        return userRepository.save(faculty);
    }

    public User removeDean(Long facultyId) {
        User faculty = userRepository.findById(facultyId)
                .orElseThrow(() -> new IllegalArgumentException("Faculty member not found with ID: " + facultyId));

        faculty.setRole(Role.FACULTY);
        faculty.setDesignation("Professor");
        return userRepository.save(faculty);
    }

    public long countStudents() {
        return userRepository.countByRoleIn(List.of(Role.STUDENT, Role.CLUB_LEADER, Role.EVENT_LEADER));
    }

    public long countFaculty() {
        return userRepository.countByRoleIn(List.of(Role.FACULTY, Role.HOD, Role.DEAN));
    }

    public long countHods() {
        return userRepository.countByRole(Role.HOD);
    }

    public long countDeans() {
        return userRepository.countByRole(Role.DEAN);
    }

    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    public long countUsers() {
        return userRepository.count();
    }
}

