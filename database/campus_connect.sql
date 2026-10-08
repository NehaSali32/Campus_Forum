-- =========================================================================
-- CAMPUS CONNECT PLATFORM - COMPLETE DATABASE SCHEMA & SAMPLE DATA
-- Database: campus_connect_db
-- Compatibility: MySQL 8.0+
-- =========================================================================

CREATE DATABASE IF NOT EXISTS `campus_connect_db` 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `campus_connect_db`;

SET FOREIGN_KEY_CHECKS = 0;

-- -------------------------------------------------------------------------
-- 1. Table: departments
-- -------------------------------------------------------------------------
DROP TABLE IF EXISTS `departments`;
CREATE TABLE `departments` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL UNIQUE,
    `code` VARCHAR(50) NOT NULL UNIQUE,
    `description` TEXT,
    `hod_id` BIGINT,
    `manager_id` BIGINT,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -------------------------------------------------------------------------
-- 2. Table: app_users
-- -------------------------------------------------------------------------
DROP TABLE IF EXISTS `app_users`;
CREATE TABLE `app_users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `full_name` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `prn_roll_no` VARCHAR(100) UNIQUE,
    `employee_id` VARCHAR(100) UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `role` VARCHAR(50) NOT NULL DEFAULT 'STUDENT',
    `designation` VARCHAR(100) DEFAULT 'STUDENT',
    `department_id` BIGINT,
    `phone` VARCHAR(50),
    `date_of_birth` DATE,
    `address` TEXT,
    `caste_category` VARCHAR(50),
    `bio` TEXT,
    `profile_image` VARCHAR(255),
    `linkedin` VARCHAR(255),
    `github` VARCHAR(255),
    `semester` VARCHAR(50),
    `year` VARCHAR(50),
    `status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_user_dept` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Set Department HOD & Manager Foreign Keys
ALTER TABLE `departments` 
    ADD CONSTRAINT `fk_dept_hod` FOREIGN KEY (`hod_id`) REFERENCES `app_users` (`id`) ON DELETE SET NULL,
    ADD CONSTRAINT `fk_dept_manager` FOREIGN KEY (`manager_id`) REFERENCES `app_users` (`id`) ON DELETE SET NULL;

-- -------------------------------------------------------------------------
-- 3. Table: subjects
-- -------------------------------------------------------------------------
DROP TABLE IF EXISTS `subjects`;
CREATE TABLE `subjects` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `subject_name` VARCHAR(255) NOT NULL,
    `subject_code` VARCHAR(50) NOT NULL UNIQUE,
    `semester` VARCHAR(50),
    `department_id` BIGINT,
    `faculty_id` BIGINT,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_subject_dept` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_subject_faculty` FOREIGN KEY (`faculty_id`) REFERENCES `app_users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- -------------------------------------------------------------------------
-- 4. Table: categories
-- -------------------------------------------------------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL UNIQUE,
    `type` VARCHAR(50) NOT NULL DEFAULT 'BOTH',
    `description` VARCHAR(500),
    `color_code` VARCHAR(50) DEFAULT '#2563eb'
) ENGINE=InnoDB;

-- -------------------------------------------------------------------------
-- 5. Table: notices
-- -------------------------------------------------------------------------
DROP TABLE IF EXISTS `notices`;
CREATE TABLE `notices` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL,
    `content` LONGTEXT NOT NULL,
    `category` VARCHAR(100) NOT NULL DEFAULT 'Academic',
    `department_id` BIGINT,
    `priority` VARCHAR(50) NOT NULL DEFAULT 'Normal',
    `author_id` BIGINT NOT NULL,
    `is_pinned` BOOLEAN NOT NULL DEFAULT FALSE,
    `attachment_url` VARCHAR(255),
    `attachment_name` VARCHAR(255),
    `views_count` INT NOT NULL DEFAULT 0,
    `requires_acknowledgement` BOOLEAN NOT NULL DEFAULT FALSE,
    `scheduled_date` DATETIME,
    `status` VARCHAR(50) NOT NULL DEFAULT 'PUBLISHED',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_notice_dept` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_notice_author` FOREIGN KEY (`author_id`) REFERENCES `app_users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -------------------------------------------------------------------------
-- 6. Table: notice_bookmarks
-- -------------------------------------------------------------------------
DROP TABLE IF EXISTS `notice_bookmarks`;
CREATE TABLE `notice_bookmarks` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `notice_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_notice_user_bookmark` (`notice_id`, `user_id`),
    CONSTRAINT `fk_bookmark_notice` FOREIGN KEY (`notice_id`) REFERENCES `notices` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_bookmark_user` FOREIGN KEY (`user_id`) REFERENCES `app_users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -------------------------------------------------------------------------
-- 7. Table: notice_acknowledgements
-- -------------------------------------------------------------------------
DROP TABLE IF EXISTS `notice_acknowledgements`;
CREATE TABLE `notice_acknowledgements` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `notice_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `acknowledged_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_notice_user_ack` (`notice_id`, `user_id`),
    CONSTRAINT `fk_ack_notice` FOREIGN KEY (`notice_id`) REFERENCES `notices` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_ack_user` FOREIGN KEY (`user_id`) REFERENCES `app_users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -------------------------------------------------------------------------
-- 8. Table: forum_topics
-- -------------------------------------------------------------------------
DROP TABLE IF EXISTS `forum_topics`;
CREATE TABLE `forum_topics` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL,
    `content` LONGTEXT NOT NULL,
    `category_id` BIGINT NOT NULL,
    `department_id` BIGINT,
    `tags` VARCHAR(255),
    `author_id` BIGINT NOT NULL,
    `status` VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    `views_count` INT NOT NULL DEFAULT 0,
    `likes_count` INT NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_topic_cat` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_topic_dept` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_topic_author` FOREIGN KEY (`author_id`) REFERENCES `app_users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -------------------------------------------------------------------------
-- 9. Table: discussion_upvotes
-- -------------------------------------------------------------------------
DROP TABLE IF EXISTS `discussion_upvotes`;
CREATE TABLE `discussion_upvotes` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `topic_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_topic_user_upvote` (`topic_id`, `user_id`),
    CONSTRAINT `fk_upvote_topic` FOREIGN KEY (`topic_id`) REFERENCES `forum_topics` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_upvote_user` FOREIGN KEY (`user_id`) REFERENCES `app_users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -------------------------------------------------------------------------
-- 10. Table: discussion_bookmarks
-- -------------------------------------------------------------------------
DROP TABLE IF EXISTS `discussion_bookmarks`;
CREATE TABLE `discussion_bookmarks` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `topic_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_topic_user_bookmark` (`topic_id`, `user_id`),
    CONSTRAINT `fk_topic_bookmark_topic` FOREIGN KEY (`topic_id`) REFERENCES `forum_topics` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_topic_bookmark_user` FOREIGN KEY (`user_id`) REFERENCES `app_users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -------------------------------------------------------------------------
-- 11. Table: forum_comments
-- -------------------------------------------------------------------------
DROP TABLE IF EXISTS `forum_comments`;
CREATE TABLE `forum_comments` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `topic_id` BIGINT NOT NULL,
    `author_id` BIGINT NOT NULL,
    `content` TEXT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_comment_topic` FOREIGN KEY (`topic_id`) REFERENCES `forum_topics` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_author` FOREIGN KEY (`author_id`) REFERENCES `app_users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -------------------------------------------------------------------------
-- 12. Table: clubs
-- -------------------------------------------------------------------------
DROP TABLE IF EXISTS `clubs`;
CREATE TABLE `clubs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `club_name` VARCHAR(255) NOT NULL UNIQUE,
    `description` TEXT,
    `department_id` BIGINT,
    `leader_id` BIGINT,
    `status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_club_dept` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_club_leader` FOREIGN KEY (`leader_id`) REFERENCES `app_users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- -------------------------------------------------------------------------
-- 13. Table: club_requests
-- -------------------------------------------------------------------------
DROP TABLE IF EXISTS `club_requests`;
CREATE TABLE `club_requests` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `student_id` BIGINT NOT NULL,
    `club_id` BIGINT NOT NULL,
    `reason` TEXT NOT NULL,
    `status` VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_request_student` FOREIGN KEY (`student_id`) REFERENCES `app_users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_request_club` FOREIGN KEY (`club_id`) REFERENCES `clubs` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -------------------------------------------------------------------------
-- 14. Table: events
-- -------------------------------------------------------------------------
DROP TABLE IF EXISTS `events`;
CREATE TABLE `events` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL,
    `description` LONGTEXT NOT NULL,
    `event_date` DATE NOT NULL,
    `event_time` VARCHAR(50) NOT NULL,
    `location` VARCHAR(255) NOT NULL,
    `category` VARCHAR(100) NOT NULL DEFAULT 'Technical',
    `organizer` VARCHAR(255),
    `department_id` BIGINT,
    `club_id` BIGINT,
    `image` VARCHAR(255),
    `status` VARCHAR(50) NOT NULL DEFAULT 'APPROVED',
    `created_by` BIGINT NOT NULL,
    `approved_by` BIGINT,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_event_dept` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_event_club` FOREIGN KEY (`club_id`) REFERENCES `clubs` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_event_creator` FOREIGN KEY (`created_by`) REFERENCES `app_users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_event_approver` FOREIGN KEY (`approved_by`) REFERENCES `app_users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- -------------------------------------------------------------------------
-- 15. Table: event_attendees
-- -------------------------------------------------------------------------
DROP TABLE IF EXISTS `event_attendees`;
CREATE TABLE `event_attendees` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `event_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `registered_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_event_user_attendee` (`event_id`, `user_id`),
    CONSTRAINT `fk_attendee_event` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_attendee_user` FOREIGN KEY (`user_id`) REFERENCES `app_users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -------------------------------------------------------------------------
-- 16. Table: student_documents
-- -------------------------------------------------------------------------
DROP TABLE IF EXISTS `student_documents`;
CREATE TABLE `student_documents` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `student_id` BIGINT NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `type` VARCHAR(100) NOT NULL DEFAULT 'Marksheet',
    `file_path` VARCHAR(255) NOT NULL,
    `file_name` VARCHAR(255),
    `file_size` BIGINT,
    `upload_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_doc_student` FOREIGN KEY (`student_id`) REFERENCES `app_users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -------------------------------------------------------------------------
-- 17. Table: college_resources
-- -------------------------------------------------------------------------
DROP TABLE IF EXISTS `college_resources`;
CREATE TABLE `college_resources` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL,
    `description` TEXT,
    `category` VARCHAR(100) NOT NULL DEFAULT 'Academic',
    `department_id` BIGINT,
    `resource_type` VARCHAR(50) NOT NULL DEFAULT 'FILE',
    `file_path` VARCHAR(255),
    `link_url` VARCHAR(255),
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_res_dept` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- -------------------------------------------------------------------------
-- 18. Table: notifications
-- -------------------------------------------------------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `message` TEXT NOT NULL,
    `type` VARCHAR(50) DEFAULT 'NOTICE',
    `reference_id` BIGINT,
    `is_read` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -------------------------------------------------------------------------
-- 19. Table: team_members
-- -------------------------------------------------------------------------
DROP TABLE IF EXISTS `team_members`;
CREATE TABLE `team_members` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL,
    `role` VARCHAR(255) NOT NULL,
    `bio` TEXT,
    `image` VARCHAR(255),
    `email` VARCHAR(255),
    `linkedin` VARCHAR(255),
    `github` VARCHAR(255),
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================================
-- SAMPLE DATA INSERTIONS
-- =========================================================================

-- Departments
INSERT INTO `departments` (`id`, `name`, `code`, `description`) VALUES
(1, 'Information Technology', 'IT', 'Department of Information Technology, focusing on software engineering, cloud computing, and AI systems.'),
(2, 'Computer Engineering', 'CE', 'Department of Computer Engineering, focusing on computing systems and network security.'),
(3, 'Electronics & Telecommunication', 'EXTC', 'Department of EXTC, focusing on embedded systems, IoT, and signal processing.'),
(4, 'Mechanical Engineering', 'MECH', 'Department of Mechanical Engineering, focusing on robotics, thermal engineering, and CAD/CAM.');

-- Categories
INSERT INTO `categories` (`id`, `name`, `type`, `description`, `color_code`) VALUES
(1, 'Academics', 'BOTH', 'Course schedules, academic calendars, syllabus updates, and lectures', '#2563eb'),
(2, 'Programming', 'FORUM', 'Coding, algorithm challenges, debugging, and software design', '#7c3aed'),
(3, 'Projects', 'BOTH', 'Mini-projects, final-year capstone, repository guidance, and team collaboration', '#059669'),
(4, 'Career', 'BOTH', 'Higher studies, competitive exams (GATE, GRE, CAT), and career paths', '#d97706'),
(5, 'Placements', 'BOTH', 'Campus recruitment drives, mock interviews, and company preparation', '#dc2626'),
(6, 'Campus Life', 'BOTH', 'Hostel, canteen, college fests, cultural events, and student clubs', '#0891b2'),
(7, 'Hostel', 'BOTH', 'Hostel accommodation, mess food, rules, and general facilities', '#ea580c'),
(8, 'Clubs', 'BOTH', 'Technical, cultural, sports clubs, and extracurricular societies', '#4f46e5'),
(9, 'Events', 'BOTH', 'Symposiums, hackathons, workshops, and sports tournaments', '#16a34a'),
(10, 'Lost & Found', 'FORUM', 'Report or reclaim lost items across campus facilities', '#6b7280'),
(11, 'General', 'BOTH', 'General student doubts and campus announcements', '#475569');

-- Users (passwords hashed using SHA-256 with 'campus_connect_salt_')
-- admin123 -> a4e46049a45e998782a932cce6c321d266e745674d8123284ff35f3b7617b070
-- faculty123 -> 5c786a34586412128e4e9f3b90b1ad07dfc9d2f2b3f1b4c9103e913a89e02377
-- student123 -> 8df6225916ca118ff5c2fb754fc4f27be30fc2f1fa50259b15d2a93946394bf3

INSERT INTO `app_users` (`id`, `full_name`, `email`, `prn_roll_no`, `employee_id`, `password`, `role`, `designation`, `department_id`, `status`) VALUES
(1, 'Prof. IT Administrator', 'admin@college.ac.in', 'ADMIN001', 'ADMIN001', 'a4e46049a45e998782a932cce6c321d266e745674d8123284ff35f3b7617b070', 'ADMIN', 'CAMPUS_ADMINISTRATOR', 1, 'ACTIVE'),
(2, 'Prof. Raj Gopal Patil', 'faculty@college.ac.in', NULL, 'EMP001', '5c786a34586412128e4e9f3b90b1ad07dfc9d2f2b3f1b4c9103e913a89e02377', 'FACULTY', 'HOD', 1, 'ACTIVE'),
(3, 'Nandini D. Suryawanshi', 'student@college.ac.in', '251109047', NULL, '8df6225916ca118ff5c2fb754fc4f27be30fc2f1fa50259b15d2a93946394bf3', 'STUDENT', 'STUDENT', 1, 'ACTIVE'),
(4, 'Vaibhavi D. Patil', 'vaibhavi@college.ac.in', '251109067', NULL, '8df6225916ca118ff5c2fb754fc4f27be30fc2f1fa50259b15d2a93946394bf3', 'STUDENT', 'STUDENT', 1, 'ACTIVE'),
(5, 'Dimple P. Patil', 'dimple@college.ac.in', '251109090', NULL, '8df6225916ca118ff5c2fb754fc4f27be30fc2f1fa50259b15d2a93946394bf3', 'STUDENT', 'STUDENT', 1, 'ACTIVE'),
(6, 'Neha J. Sali', 'neha@college.ac.in', '251109053', NULL, '8df6225916ca118ff5c2fb754fc4f27be30fc2f1fa50259b15d2a93946394bf3', 'STUDENT', 'STUDENT', 1, 'ACTIVE');

-- Link HOD to Department
UPDATE `departments` SET `hod_id` = 2 WHERE `id` = 1;

-- Subjects
INSERT INTO `subjects` (`id`, `subject_name`, `subject_code`, `semester`, `department_id`, `faculty_id`) VALUES
(1, 'Data Structures & Algorithms', 'IT201', 'Semester III', 1, 2),
(2, 'Database Management Systems', 'IT202', 'Semester III', 1, 2),
(3, 'Web Technologies & Frameworks', 'IT203', 'Semester III', 1, 2),
(4, 'Object Oriented Programming with Java', 'IT204', 'Semester III', 1, 2);

-- Team Members
INSERT INTO `team_members` (`id`, `name`, `role`, `bio`, `image`, `email`, `linkedin`, `github`) VALUES
(1, 'Nandini Dipaksing Suryawanshi', 'Full-Stack Developer & Team Lead', 'Roll No: IT036 | PRN: 251109047. Leads Spring Boot backend architecture, REST APIs, and security engineering.', '/images/team-nandini.svg', 'nandini@rcpit.ac.in', 'https://linkedin.com', 'https://github.com'),
(2, 'Vaibhavi Dhananjay Patil', 'Backend & Database Developer', 'Roll No: IT051 | PRN: 251109067. Leads database schema design, JPA repository queries, and MySQL transaction optimization.', '/images/team-vaibhavi.svg', 'vaibhavi@rcpit.ac.in', 'https://linkedin.com', 'https://github.com'),
(3, 'Dimple Pandurang Patil', 'UI/UX & Frontend Developer', 'Roll No: IT073 | PRN: 251109090. Designs modern accessible responsive layouts, CSS custom property theming, and Bootstrap 5 components.', '/images/team-dimple.svg', 'dimple@rcpit.ac.in', 'https://linkedin.com', 'https://github.com'),
(4, 'Neha Jagdish Sali', 'System Testing & Integration Specialist', 'Roll No: IT040 | PRN: 251109053. Responsible for API integration testing, client-side validation, cross-browser compatibility, and quality assurance.', '/images/team-neha.svg', 'neha@rcpit.ac.in', 'https://linkedin.com', 'https://github.com');
