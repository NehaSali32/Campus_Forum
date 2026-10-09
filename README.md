# Campus Forum — Academic Discussion & Collegiate Communication Platform

A secure, modern, enterprise-ready academic discussion forum and official communication web application engineered for colleges and universities.

---

## 🏛️ Platform Identity & Architecture

**Campus Forum** unifies institutional communication into a structured, role-guarded, accessible digital portal. It eliminates chaotic third-party messaging groups and physical notice boards in favor of verified institutional interaction.

* **Backend:** Java 25 LTS, Spring Boot 3.5.16 (Spring Web MVC, Spring Data JPA, Hibernate ORM, Jakarta Validation)
* **Security & Crypto:** BCrypt password hashing (`BCryptPasswordEncoder(10)`) with automatic, transparent legacy SHA-256 password upgrade on login, role-based access control (RBAC), HttpOnly session security
* **Database:** MySQL 8.0 relational database (`campus_connect_db` or configurable via `DB_URL`) with transactional referential integrity, indexes, and automated schema migration
* **Frontend:** Semantic HTML5, CSS3 Custom Properties with full Light & Dark collegiate theming, accessible components, Vanilla JavaScript (ES6+ Asynchronous Fetch API, zero heavy framework overhead)
* **Target Roles:** `STUDENT`, `CLUB_LEADER`, `FACULTY`, `HOD` (Head of Department), `DEAN` (Dean of Academics), and `ADMIN` (Institutional Administrator)

---

## 🔑 Initial Administrator Credentials

On first startup, the system initializes the foundational categories, academic departments, and an administrator account. Set `ADMIN_EMAIL` and a unique, strong `ADMIN_PASSWORD` before starting a production deployment. The production profile requires this password and will not start without it. Never publish deployment credentials in source code or documentation.

---

## 🌟 Core Functional Modules

### 1. 💬 Academic Discussion Forum (`/forum`, `/topic-detail`)
- **Advanced Filtering Tabs:**
  - `All` — Complete institutional discussion stream
  - `Recent` — Chronological publication feed
  - `Most Discussed` — Active threads ranked by comment volume
  - `Most Upvoted` — Highly rated questions and solutions
  - `Unanswered` — Questions awaiting academic assistance
  - `Solved` — Inquiries with confirmed solutions
  - `My Discussions` — Threads authored by the current member
  - `Bookmarked` — Saved references
- **Solved Discussion Workflow:** Authors and faculty members can mark questions as `Solved` or `Unsolved`.
- **Accepted Answer:** Topic creators can designate a specific reply as the **Accepted Solution**.
- **Verified Faculty Badge:** Answers from college professors, HODs, and Deans carry official verified credentials.
- **Content Reporting & Moderation:** Members can flag inappropriate or non-academic content; administrators review and resolve reports directly from the moderation console.
- **Author Thread Deletion:** Question authors and academic staff have one-click deletion privileges.

### 2. 📢 Official Institutional Notices (`/notices`)
- **Priority Tiering:** `URGENT` (prominent high-contrast alert ticker), `IMPORTANT`, `NORMAL`.
- **Audience Targeting:** College-wide broadcasts or department-specific circulars.
- **Mandatory Student Acknowledgment:** Tracks student reads with live confirmation tally.
- **Document Attachments:** Downloadable PDFs, circulars, and official letters.

### 3. 📅 Campus Events & Workshops (`/events`)
- **Multi-Level Approval Pipeline:** Events proposed by students or clubs require faculty endorsement and HOD/Dean/Admin sanction before public publishing.
- **Direct Organizing Privileges:** HODs, Deans, and Administrators can publish events without extra permission steps.
- **Student Attendance Registration:** Real-time seat tally and attendee logging.

### 4. 📚 Academic Resources Repository (`/resources`)
- Centralized course repository for syllabus documents, laboratory manuals, past exam papers, and technical links.
- Filterable by department and academic category.

### 5. 📁 Private Student Document Locker (`/documents`)
- Confidential locker for students to store marksheets, bonafide certificates, hall tickets, and fee receipts.

### 6. ⚙️ Campus Administration Console (`/admin`)
- Real-time institutional KPIs (Students, Faculty, HODs, Deans, Departments, Notices, Active Discussions).
- Moderation Queue: Audit flagged discussions and resolve community reports.
- User Access Controls: Activate, deactivate, or enroll students and faculty.
- Department & Curriculum Governance: Assign HODs and allocate semester subjects.

---

## 🚀 Environment Configuration

The application uses environment-driven configuration. Production secrets must be set explicitly:

```properties
# Server Port
PORT=8080

# MySQL Database Configuration
DB_URL=jdbc:mysql://localhost:3306/campus_connect_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=your_mysql_password

# Initial Admin Credentials (set a unique value for each deployment)
ADMIN_EMAIL=admin@campusforum.edu
ADMIN_PASSWORD=

# Seed Demo Data (set to false for production)
SEED_DEMO_DATA=false
```

`ADMIN_PASSWORD` is required for production and must be set to a unique, strong secret.

Templates provided:
- `.env.example` — Environment template for containerized or cloud deployments
- `application.properties` — Base configuration with env variable interpolation
- `application-dev.properties` — Local development settings (demo data enabled)
- `application-prod.properties` — Hardened production profile

---

## ☁️ Permanent Hosting on Railway

The repository includes a Dockerfile and Railway deployment settings. Railway hosts the app and MySQL independently of your laptop. A Railway account and an active plan may be required; check current pricing before deploying.

1. Push this repository to GitHub and create a Railway project from the repository.
2. Add a Railway **MySQL** service and name it `MySQL`.
3. In the app service's **Variables**, add:

   | Variable | Value |
   | :--- | :--- |
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `ADMIN_EMAIL` | Your administrator email |
   | `ADMIN_PASSWORD` | A new, strong password kept private |
   | `DB_URL` | `jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?serverTimezone=UTC` |
   | `DB_USERNAME` | `${{MySQL.MYSQLUSER}}` |
   | `DB_PASSWORD` | `${{MySQL.MYSQLPASSWORD}}` |
   | `SEED_DEMO_DATA` | `false` |
   | `UPLOAD_DIR` | `/app/uploads` |

4. Attach a Railway persistent volume to the app service at `/app/uploads` so uploaded documents survive redeploys. Keep the MySQL service's persistent storage enabled as well.
5. Deploy the app service, then use Railway's **Networking** settings to generate a public domain. The resulting Railway domain remains reachable while the Railway services and plan remain active, even when your laptop is off.

Set all secrets in Railway's Variables UI, not in this repository. Do not enable demo-data seeding on a public deployment.

---

## ▶️ Running from VS Code

### Option 1: Press F5 (VS Code Launch Configuration)
1. Open the project root in **VS Code**.
2. Press **`F5`** or select **"▶ Run Website (MySQL Mode)"** from the Run & Debug panel.
3. Open `http://localhost:8080` in your web browser.

### Option 2: VS Code Tasks
Open the Command Palette (`Ctrl+Shift+P`), select **Tasks: Run Task**, and choose:
- `▶ Run Campus Forum (MySQL Mode)`

### Option 3: Terminal / Command Prompt
```powershell
# Using the bundled Maven Wrapper
.\mvnw.cmd spring-boot:run
```

For production profile:
```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=prod"
```

---

## 🧪 Testing & Packaging

Run the full automated test suite and package a production `.jar`:

```powershell
.\mvnw.cmd clean test package
```

The resulting executable archive will be generated at:
`target/campus-forum-1.0.0.jar`

Launch in production:
```bash
java -jar target/campus-forum-1.0.0.jar --spring.profiles.active=prod
```

---

## 🌐 Core REST API Reference

| Endpoint | Method | Access | Description |
| :--- | :--- | :--- | :--- |
| `/api/auth/login` | POST | Public | Authenticate via email or PRN |
| `/api/auth/register` | POST | Public | Student enrollment |
| `/api/auth/me` | GET | Authenticated | Current user profile and permissions |
| `/api/auth/logout` | POST | Authenticated | Terminate session |
| `/api/forum/topics` | GET | Public/Auth | Filter discussions (`filter=all\|recent\|most_discussed\|most_upvoted\|unanswered\|solved\|my_discussions\|bookmarked`) |
| `/api/forum/topics` | POST | Authenticated | Create discussion thread |
| `/api/forum/topics/{id}/solve` | POST | Author/Staff | Toggle solved status on topic |
| `/api/forum/topics/{topicId}/accept-comment/{commentId}` | POST | Author/Staff | Mark comment as accepted solution |
| `/api/forum/reports` | POST | Authenticated | Submit content report |
| `/api/admin/reports` | GET | Admin | Retrieve moderation reports queue |
| `/api/admin/reports/{id}/resolve` | PUT | Admin | Mark report resolved |
| `/api/admin/reports/{id}/dismiss` | PUT | Admin | Dismiss report |
| `/api/notices` | GET | Public/Auth | Filter official circulars |
| `/api/notices` | POST | Faculty/Admin | Publish official notice |
| `/api/events` | GET | Public/Auth | List upcoming approved events |
| `/api/resources` | GET | Authenticated | Access college learning resources |

---

## 📄 License & Institutional Credits

Developed for collegiate campus communication and academic inquiry.  
Department of Information Technology &bull; **Campus Forum** Platform.
