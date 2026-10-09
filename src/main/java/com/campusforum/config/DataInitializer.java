package com.campusforum.config;

import com.campusforum.model.*;
import com.campusforum.repository.*;
import com.campusforum.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @org.springframework.beans.factory.annotation.Value("${app.initial-admin.email:admin@campusforum.edu}")
    private String initialAdminEmail;

    @org.springframework.beans.factory.annotation.Value("${app.initial-admin.password:Admin@Campus2026}")
    private String initialAdminPassword;

    @org.springframework.beans.factory.annotation.Value("${app.seed-demo-data:false}")
    private boolean seedDemoData;

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;
    private final NoticeRepository noticeRepository;
    private final ForumTopicRepository topicRepository;
    private final ForumCommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final ClubRepository clubRepository;
    private final CollegeResourceRepository resourceRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final EventAttendeeRepository attendeeRepository;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           DepartmentRepository departmentRepository,
                           SubjectRepository subjectRepository,
                           NoticeRepository noticeRepository,
                           ForumTopicRepository topicRepository,
                           ForumCommentRepository commentRepository,
                           EventRepository eventRepository,
                           ClubRepository clubRepository,
                           CollegeResourceRepository resourceRepository,
                           TeamMemberRepository teamMemberRepository,
                           EventAttendeeRepository attendeeRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.departmentRepository = departmentRepository;
        this.subjectRepository = subjectRepository;
        this.noticeRepository = noticeRepository;
        this.topicRepository = topicRepository;
        this.commentRepository = commentRepository;
        this.eventRepository = eventRepository;
        this.clubRepository = clubRepository;
        this.resourceRepository = resourceRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.attendeeRepository = attendeeRepository;
    }

    @Override
    public void run(String... args) {
        // 1. Seed Core Categories if empty
        if (categoryRepository.count() == 0) {
            categoryRepository.saveAll(List.of(
                    new Category("Academics", "BOTH", "Course schedules, academic calendars, syllabus updates, and lectures", "#2563eb"),
                    new Category("Programming", "FORUM", "Coding, algorithm challenges, debugging, and software design", "#7c3aed"),
                    new Category("Projects", "BOTH", "Mini-projects, final-year capstone, repository guidance, and team collaboration", "#059669"),
                    new Category("Career", "BOTH", "Higher studies, competitive exams (GATE, GRE, CAT), and career paths", "#d97706"),
                    new Category("Placements", "BOTH", "Campus recruitment drives, mock interviews, and company preparation", "#dc2626"),
                    new Category("Campus Life", "BOTH", "Hostel, canteen, college fests, cultural events, and student clubs", "#0891b2"),
                    new Category("Events", "BOTH", "Symposiums, hackathons, workshops, and sports tournaments", "#16a34a"),
                    new Category("General", "BOTH", "General student doubts and campus announcements", "#475569")
            ));
        }

        // 2. Seed Academic Departments if empty
        Department itDept = departmentRepository.findByCode("IT").orElse(null);
        if (itDept == null && departmentRepository.count() == 0) {
            itDept = departmentRepository.save(new Department("Information Technology", "IT", "Department of Information Technology, focusing on software engineering, cloud computing, and AI systems."));
            departmentRepository.save(new Department("Computer Engineering", "CE", "Department of Computer Engineering, focusing on computing systems, hardware architectures, and network security."));
            departmentRepository.save(new Department("Electronics & Telecommunication", "EXTC", "Department of EXTC, focusing on embedded systems, IoT, and signal processing."));
            departmentRepository.save(new Department("Mechanical Engineering", "MECH", "Department of Mechanical Engineering, focusing on robotics, thermal engineering, and CAD/CAM design."));
        } else if (itDept == null) {
            itDept = departmentRepository.findAll().stream().findFirst().orElse(null);
        }

        // 3. Ensure Primary Administrator Account Exists
        boolean hasAdmin = userRepository.findAll().stream().anyMatch(u -> u.getRole() == Role.ADMIN);
        if (!hasAdmin) {
            if (initialAdminPassword.isBlank()) {
                throw new IllegalStateException("ADMIN_PASSWORD must be set when no administrator account exists.");
            }

            User primaryAdmin = new User();
            primaryAdmin.setFullName("Campus Forum Administrator");
            primaryAdmin.setEmail(initialAdminEmail.trim().toLowerCase());
            primaryAdmin.setEmployeeId("ADMIN001");
            primaryAdmin.setPrnRollNo("ADMIN001");
            primaryAdmin.setPassword(UserService.hashPassword(initialAdminPassword));
            primaryAdmin.setRole(Role.ADMIN);
            primaryAdmin.setDesignation("SYSTEM_ADMINISTRATOR");
            primaryAdmin.setDepartment(itDept);
            primaryAdmin.setStatus("ACTIVE");
            userRepository.save(primaryAdmin);
        } else {
            if (!initialAdminPassword.isBlank()
                    && userRepository.findByEmail(initialAdminEmail.trim().toLowerCase()).isEmpty()) {
                User primaryAdmin = new User();
                primaryAdmin.setFullName("Campus Forum Administrator");
                primaryAdmin.setEmail(initialAdminEmail.trim().toLowerCase());
                primaryAdmin.setEmployeeId("ADMIN_SYS");
                primaryAdmin.setPassword(UserService.hashPassword(initialAdminPassword));
                primaryAdmin.setRole(Role.ADMIN);
                primaryAdmin.setDesignation("SYSTEM_ADMINISTRATOR");
                primaryAdmin.setDepartment(itDept);
                primaryAdmin.setStatus("ACTIVE");
                userRepository.save(primaryAdmin);
            }
        }

        // Seed / Update Dynamic Team Members with real photos and exact details
        List<TeamMember> currentTeam = teamMemberRepository.findAll();
        if (currentTeam.isEmpty()) {
            teamMemberRepository.saveAll(List.of(
                    new TeamMember("Nandini Dipaksing Suryawanshi", "Full-Stack Developer & Team Lead", "Roll No: IT036 | PRN: 251109047. Specializes in Spring Boot backend architecture, REST API design, and security engineering.", "/images/Nandini.jpeg", "nandini@rcpit.ac.in", "https://linkedin.com", "https://github.com"),
                    new TeamMember("Vaibhavi Dhananjay Patil", "Backend & Database Developer", "Roll No: IT051 | PRN: 251109067. Leads database schema design, JPA repository queries, and MySQL transaction optimization.", "/images/Vaibhavi%20Patil.jpeg", "vaibhavi@rcpit.ac.in", "https://linkedin.com", "https://github.com"),
                    new TeamMember("Dimpal Pandurang Patil", "UI/UX & Frontend Developer", "Roll No: IT073 | PRN: 251109090. Designs modern accessible responsive layouts, CSS custom property theming, and Bootstrap 5 components.", "/images/Dimpal%20Patil.jpeg", "dimpal@rcpit.ac.in", "https://linkedin.com", "https://github.com"),
                    new TeamMember("Neha Jagdish Sali", "System Testing & Integration Specialist", "Roll No: IT040 | PRN: 251109053. Responsible for API integration testing, client-side validation, cross-browser compatibility, and quality assurance.", "/images/Neha.jpeg", "neha@rcpit.ac.in", "https://linkedin.com", "https://github.com")
            ));
        } else {
            for (TeamMember m : currentTeam) {
                if (m.getEmail() != null) {
                    if (m.getEmail().contains("nandini")) {
                        m.setImage("/images/Nandini.jpeg");
                        m.setName("Nandini Dipaksing Suryawanshi");
                    } else if (m.getEmail().contains("vaibhavi")) {
                        m.setImage("/images/Vaibhavi%20Patil.jpeg");
                        m.setName("Vaibhavi Dhananjay Patil");
                    } else if (m.getEmail().contains("dimple") || m.getEmail().contains("dimpal")) {
                        m.setImage("/images/Dimpal%20Patil.jpeg");
                        m.setName("Dimpal Pandurang Patil");
                    } else if (m.getEmail().contains("neha")) {
                        m.setImage("/images/Neha.jpeg");
                        m.setName("Neha Jagdish Sali");
                    }
                    teamMemberRepository.save(m);
                }
            }
        }

        // If demo seeding is disabled (production default), stop here
        if (!seedDemoData) {
            return;
        }

        User admin = userRepository.findByEmail(initialAdminEmail)
                .orElseGet(() -> userRepository.findByEmail("admin@college.ac.in")
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> u.getRole() == Role.ADMIN)
                        .findFirst()
                        .orElse(null)));

        // Backward-compatible RCPIT admin
        if (userRepository.findByEmail("admin@rcpit.ac.in").isEmpty()) {
            User rcpitAdmin = new User();
            rcpitAdmin.setFullName("Campus Admin (RCPIT)");
            rcpitAdmin.setEmail("admin@rcpit.ac.in");
            rcpitAdmin.setEmployeeId("ADMIN002");
            rcpitAdmin.setPrnRollNo("ADMIN002");
            rcpitAdmin.setPassword(UserService.hashPassword("admin123"));
            rcpitAdmin.setRole(Role.ADMIN);
            rcpitAdmin.setDesignation("CAMPUS_ADMINISTRATOR");
            rcpitAdmin.setDepartment(itDept);
            rcpitAdmin.setStatus("ACTIVE");
            userRepository.save(rcpitAdmin);
        }

        // 4. Seed Dean
        if (userRepository.findByEmail("dean@college.ac.in").isEmpty()) {
            User dean = new User();
            dean.setFullName("Dr. Suresh Kulkarni");
            dean.setEmail("dean@college.ac.in");
            dean.setEmployeeId("DEAN001");
            dean.setPassword(UserService.hashPassword("dean123"));
            dean.setRole(Role.DEAN);
            dean.setDesignation("Dean of Academics");
            dean.setDepartment(itDept);
            dean.setBio("Dean of Academic Affairs overseeing curriculum, collegiate policy, and event sanctioning.");
            dean.setStatus("ACTIVE");
            userRepository.save(dean);
        }

        // 5. Seed HOD
        User hod = userRepository.findByEmail("hod@college.ac.in").orElse(null);
        if (hod == null) {
            hod = new User();
            hod.setFullName("Dr. Rajesh Sharma");
            hod.setEmail("hod@college.ac.in");
            hod.setEmployeeId("HOD001");
            hod.setPassword(UserService.hashPassword("hod123"));
            hod.setRole(Role.HOD);
            hod.setDesignation("Head of Department");
            hod.setDepartment(itDept);
            hod.setBio("Head of Department (Information Technology) with 18+ years in Computer Systems.");
            hod.setStatus("ACTIVE");
            hod = userRepository.save(hod);
            itDept.setHod(hod);
            departmentRepository.save(itDept);
        }

        // 6. Seed Faculty Users
        User faculty = userRepository.findByEmail("faculty@college.ac.in").orElse(null);
        if (faculty == null) {
            faculty = new User();
            faculty.setFullName("Prof. Amit Verma");
            faculty.setEmail("faculty@college.ac.in");
            faculty.setEmployeeId("FAC001");
            faculty.setPassword(UserService.hashPassword("faculty123"));
            faculty.setRole(Role.FACULTY);
            faculty.setDesignation("Assistant Professor");
            faculty.setDepartment(itDept);
            faculty.setBio("Faculty in Information Technology specializing in Database Systems and Java.");
            faculty.setStatus("ACTIVE");
            faculty = userRepository.save(faculty);
        }

        // 7. Seed Student Users
        User student = userRepository.findByEmail("student@college.ac.in").orElse(null);
        if (student == null) {
            student = new User();
            student.setFullName("Nandini D. Suryawanshi");
            student.setEmail("student@college.ac.in");
            student.setPrnRollNo("251109047");
            student.setPassword(UserService.hashPassword("student123"));
            student.setRole(Role.STUDENT);
            student.setDesignation("STUDENT");
            student.setDepartment(itDept);
            student.setSemester("Semester III");
            student.setYear("Third Year");
            student.setCourse("B.Tech");
            student.setDivision("A");
            student.setBio("Information Technology undergrad passionate about Full-Stack Web Development, Spring Boot, and Cloud Architecture.");
            student.setStatus("ACTIVE");
            student = userRepository.save(student);
        }

        // 8. Seed Club Leader
        if (userRepository.findByEmail("clubleader@college.ac.in").isEmpty()) {
            User cl = new User();
            cl.setFullName("Aarav K. Mehta");
            cl.setEmail("clubleader@college.ac.in");
            cl.setPrnRollNo("251109088");
            cl.setPassword(UserService.hashPassword("student123"));
            cl.setRole(Role.CLUB_LEADER);
            cl.setDesignation("CLUB_LEADER");
            cl.setDepartment(itDept);
            cl.setSemester("Semester III");
            cl.setYear("Third Year");
            cl.setCourse("B.Tech");
            cl.setDivision("B");
            cl.setBio("Student Lead at Coding & Developers Club.");
            cl.setStatus("ACTIVE");
            userRepository.save(cl);
        }

        // Seed other student team members
        if (userRepository.findByEmail("vaibhavi@college.ac.in").isEmpty()) {
            User s2 = new User();
            s2.setFullName("Vaibhavi D. Patil");
            s2.setEmail("vaibhavi@college.ac.in");
            s2.setPrnRollNo("251109067");
            s2.setPassword(UserService.hashPassword("student123"));
            s2.setRole(Role.STUDENT);
            s2.setDesignation("STUDENT");
            s2.setDepartment(itDept);
            s2.setSemester("Semester III");
            s2.setYear("Second Year");
            s2.setStatus("ACTIVE");
            userRepository.save(s2);
        }

        if (userRepository.findByEmail("dimple@college.ac.in").isEmpty()) {
            User s3 = new User();
            s3.setFullName("Dimple P. Patil");
            s3.setEmail("dimple@college.ac.in");
            s3.setPrnRollNo("251109090");
            s3.setPassword(UserService.hashPassword("student123"));
            s3.setRole(Role.STUDENT);
            s3.setDesignation("STUDENT");
            s3.setDepartment(itDept);
            s3.setSemester("Semester III");
            s3.setYear("Second Year");
            s3.setStatus("ACTIVE");
            userRepository.save(s3);
        }

        if (userRepository.findByEmail("neha@college.ac.in").isEmpty()) {
            User s4 = new User();
            s4.setFullName("Neha Jagdish Sali");
            s4.setEmail("neha@college.ac.in");
            s4.setPrnRollNo("251109053");
            s4.setPassword(UserService.hashPassword("student123"));
            s4.setRole(Role.STUDENT);
            s4.setDesignation("STUDENT");
            s4.setDepartment(itDept);
            s4.setSemester("Semester III");
            s4.setYear("Second Year");
            s4.setStatus("ACTIVE");
            userRepository.save(s4);
        }

        // 6. Seed Subjects if empty
        if (subjectRepository.count() == 0 && itDept != null && faculty != null) {
            subjectRepository.saveAll(List.of(
                    new Subject("Data Structures & Algorithms", "IT201", "Semester III", itDept, faculty),
                    new Subject("Database Management Systems", "IT202", "Semester III", itDept, faculty),
                    new Subject("Web Technologies & Frameworks", "IT203", "Semester III", itDept, faculty),
                    new Subject("Object Oriented Programming with Java", "IT204", "Semester III", itDept, faculty)
            ));
        }

        // 7. Seed Official Notices if empty
        if (noticeRepository.count() == 0 && admin != null) {
            Notice n1 = new Notice(
                    "Semester III In-Semester Examination (ISE-I) Schedule 2026",
                    "All S.Y. B.Tech Information Technology students are hereby informed that the In-Semester Examination (ISE-I) will commence from next Monday. The detailed slot-wise timetable has been published. Hall tickets and seating arrangements will be displayed near the Department Notice Board. Attendance is strictly compulsory.",
                    "Examination", itDept, "Urgent", admin, true, null, null, true
            );
            noticeRepository.save(n1);

            Notice n2 = new Notice(
                    "Campus Recruitment Drive 2026 - TCS & Tech Mahindra Registrations Open",
                    "The Training and Placement Cell invites eligible students for the upcoming national level campus drive. Eligibility criteria: 60% aggregate in B.Tech with no active backlogs. Interested candidates must submit their updated resume on the T&P portal before Friday 5:00 PM.",
                    "Training & Placement", null, "Important", admin, true, null, null, false
            );
            noticeRepository.save(n2);

            Notice n3 = new Notice(
                    "Submission of Semester Project-I Synopsis & Guide Allocation",
                    "All IT department student project groups (Semester III) must submit their signed project synopsis approved by their respective project guide. Evaluation will be conducted based on project feasibility, technical stack (Java, Spring Boot, MySQL), and preliminary architecture design.",
                    "Academic", itDept, "Normal", faculty, false, null, null, false
            );
            noticeRepository.save(n3);

            Notice n4 = new Notice(
                    "Annual National Level Technical Symposium 'Converge 2026' - Call for Participation",
                    "The Institute is proud to organize the annual national level symposium featuring Web Hackathons, Blind Coding, Technical Paper Presentations, and Robo-Race. Cash prizes worth Rs. 1,00,000 to be won. Visit the event desk or register via Campus Connect Events.",
                    "Campus Events", null, "Normal", admin, false, null, null, false
            );
            noticeRepository.save(n4);
        }

        // 8. Seed Forum Discussions if empty
        if (topicRepository.count() == 0 && student != null && faculty != null) {
            Category techCat = categoryRepository.findByName("Programming").orElseGet(() -> categoryRepository.findAll().get(0));
            Category careerCat = categoryRepository.findByName("Career").orElseGet(() -> categoryRepository.findAll().get(0));

            ForumTopic topic1 = new ForumTopic(
                    "Best practices for Spring Boot 3 REST APIs and MySQL integration in college projects",
                    "Hi everyone! We are currently upgrading our semester project into Campus Connect. We want to know what are the best practices for structuring controllers, repositories, and entities in Spring Boot 3. Any advice or recommended design patterns from seniors and faculty?",
                    techCat, student
            );
            topic1.setDepartment(itDept);
            topic1.setTags("#Java #SpringBoot #MySQL #Projects");
            topic1.setViewsCount(48);
            topic1.setLikesCount(14);
            topic1 = topicRepository.save(topic1);

            ForumComment c1 = new ForumComment(
                    topic1, faculty,
                    "Excellent architectural choice! Always follow the clean layered architecture: Entity -> Repository -> Service -> Controller. Ensure database transactions are handled with @Transactional and validate request DTOs with Jakarta Validation."
            );
            commentRepository.save(c1);

            ForumTopic topic2 = new ForumTopic(
                    "TCS National Qualifier Test (NQT) and campus placement preparation roadmap",
                    "Can seniors share tips on preparing for aptitude rounds and technical interviews for TCS, Infosys, and Cognizant campus placements?",
                    careerCat, student
            );
            topic2.setDepartment(itDept);
            topic2.setTags("#Placements #TCS #Career #Aptitude");
            topic2.setViewsCount(35);
            topic2.setLikesCount(9);
            topicRepository.save(topic2);
        }

        // 9. Seed Events if empty
        if (eventRepository.count() == 0 && admin != null) {
            eventRepository.save(new Event(
                    "Smart Campus Hackathon 2026",
                    "A 24-hour inter-college hackathon to build intelligent software solutions for campus management, student collaboration, and paperless administrative workflows. Free food and mentor guidance provided.",
                    LocalDate.now().plusDays(14), "09:00 AM", "Central Computing Facility (CCF)", "Hackathon", "Coding Club & IT Dept", itDept, null, null, "APPROVED", admin
            ));

            eventRepository.save(new Event(
                    "National Level Technical Symposium - Converge 2026",
                    "Annual college technical festival featuring coding sprints, web design showdown, technical paper presentation, and project exhibition with esteemed corporate judges.",
                    LocalDate.now().plusDays(25), "10:00 AM", "Main Campus Auditorium", "Technical", "Student Council", null, null, null, "APPROVED", admin
            ));

            eventRepository.save(new Event(
                    "Industry Expert Seminar: Cloud Architecture & DevOps",
                    "Interactive workshop on Docker, Kubernetes, CI/CD pipelines, and AWS cloud deployment strategies delivered by senior software architects from Pune.",
                    LocalDate.now().plusDays(7), "02:00 PM", "Seminar Hall 2", "Seminar", "T&P Cell", itDept, null, null, "APPROVED", admin
            ));
        }

        // 10. Seed Clubs if empty
        User clubLeader = userRepository.findByEmail("clubleader@college.ac.in").orElse(student);
        if (clubRepository.count() == 0 && itDept != null) {
            Club codingClub = clubRepository.save(new Club("Coding & Developers Club", "Technical club dedicated to competitive programming, open source projects, and web development hackathons.", itDept, clubLeader));
            clubRepository.save(new Club("Robotics & IoT Society", "Hands-on student club focused on Arduino, Raspberry Pi, autonomous bots, and industrial IoT solutions.", itDept, null));
            clubRepository.save(new Club("NSS (National Service Scheme)", "Social community service unit dedicated to cleanliness drives, tree plantation, blood donation, and rural education.", null, null));
            clubRepository.save(new Club("Entrepreneurship & Innovation Cell (E-Cell)", "Incubation platform fostering student startup ideas, venture capital pitch sessions, and patent guidance.", null, null));

            Event clubEvent = new Event(
                    "Algorithmic Sprint & Web Odyssey 2026",
                    "Competitive coding showdown testing dynamic programming, graphs, and responsive UI prototyping. Organized by Coding & Developers Club.",
                    LocalDate.now().plusDays(10), "03:00 PM", "Lab 402, IT Block", "Technical", "Coding & Developers Club", itDept, codingClub, null, "APPROVED", clubLeader
            );
            clubEvent.setMaxParticipants(60);
            clubEvent.setRules("Individual participation. Laptops mandatory with JDK 25 installed.");
            clubEvent.setContactInfo("codingclub@college.ac.in");
            clubEvent = eventRepository.save(clubEvent);

            if (student != null) {
                attendeeRepository.save(new EventAttendee(clubEvent, student));
            }
        }

        // 11. Seed College Resources if empty
        if (resourceRepository.count() == 0) {
            resourceRepository.save(new CollegeResource(
                    "Academic Calendar 2025-2026 (Even Semester)",
                    "Complete schedule of term commencement, in-sem exam dates, submission deadlines, and end-sem vacations.",
                    "Academic", itDept, "LINK", null, "https://college.edu/academics/calendar"
            ));
            resourceRepository.save(new CollegeResource(
                    "End-Semester Examination Rules & Ordinance Manual",
                    "Official examination ordinances, grade point calculation, revaluation policies, and hall ticket guidelines.",
                    "Examination", null, "LINK", null, "https://college.edu/examination/rules"
            ));
            resourceRepository.save(new CollegeResource(
                    "Bonafide Certificate & Library Clearance Form",
                    "Official application form for passport verification, education loan bonafide, and library card clearance.",
                    "Forms", null, "LINK", null, "https://college.edu/student-services/forms"
            ));
            resourceRepository.save(new CollegeResource(
                    "T&P Standard Resume Format and Placement Policy 2026",
                    "Standard college resume template and mandatory placement code of conduct for all campus recruiting companies.",
                    "Placement", null, "LINK", null, "https://college.edu/placement/guidelines"
            ));
        }
    }
}
