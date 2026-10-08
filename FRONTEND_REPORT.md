# FRONTEND TECHNICAL REPORT & FACULTY PRESENTATION GUIDE
## Community Notice & Discussion Forum (Campus Forum)
**Department of Information Technology**  
**Academic Semester Project-I (Semester III, 2025–26)**  

---

### Project Team Members
* **Nandini Dipaksing Suryawanshi** (Roll No: IT036 | PRN: 251109047)
* **Vaibhavi Dhananjay Patil** (Roll No: IT051 | PRN: 251109067)
* **Dimple Pandurang Patil** (Roll No: IT073 | PRN: 251109090)
* **Neha Jagdish Sali** (Roll No: IT040 | PRN: 251109053)

---

## 1. Executive Summary

This report provides an in-depth technical documentation of the **Frontend Architecture, User Interface (UI), and User Experience (UX)** developed for the **Community Notice & Discussion Forum** web application.

The frontend is built using standard, robust web technologies:
* **HTML5**: Semantic document structuring and accessible form controls.
* **CSS3**: Modern styling with CSS Custom Properties (Variables), Flexbox, CSS Grid, and dual **Light & Dark Themes**.
* **Vanilla JavaScript (ES6+)**: Asynchronous client-server communication using the `Fetch API`, DOM manipulation, and state management without relying on third-party heavy frontend frameworks.

---

## 2. Frontend Architecture & Design Philosophy

```mermaid
graph TD
    User([Browser Client]) -->|Open Website| Guard{Authentication Guard}
    Guard -->|Not Logged In| LoginPage[login.html]
    Guard -->|Authenticated| MainApp[Campus Portal]

    MainApp --> Nav[Dynamic Navigation & Theme Engine]
    Nav --> ThemeState[LocalStorage Theme: Light / Dark]

    MainApp --> HomePage[index.html - Dashboard & Team]
    MainApp --> NoticeBoard[notices.html - Digital Notice Board]
    MainApp --> ForumPage[forum.html - Discussion Threads]
    MainApp --> ThreadDetail[topic-detail.html - Thread & Responses]
    MainApp --> AdminConsole[admin.html - Restricted Console]

    NoticeBoard -->|Async fetch| RestNotices[/api/notices]
    ForumPage -->|Async fetch| RestForum[/api/forum/topics]
    ThreadDetail -->|Async fetch| RestComments[/api/forum/topics/id/comments]
    AdminConsole -->|Async fetch| RestAdmin[/api/admin/*]
```

### Why Native Technologies (HTML5 / CSS3 / Vanilla JS)?
1. **Zero Client-Side Overhead**: Instant page loads with no bloated bundle sizes (unlike React or Angular bundles that can be several megabytes).
2. **Standard Alignment**: Exact adherence to the technologies listed in the project syllabus (*HTML, CSS, JavaScript, Java, MySQL*).
3. **High Maintainability**: Readable, clean code that any evaluator or developer can inspect directly in browser DevTools without needing transpilers (Babel, Webpack).
4. **Native Browser Security**: Clean DOM manipulation with HTML entity escaping to eliminate Cross-Site Scripting (XSS).

---

## 3. Theming Engine (Dual Light & Dark Theme)

A major feature of the application is native, system-wide **Light & Dark Theme switching** that works smoothly across every page.

### How it Works Under the Hood:
1. **CSS Custom Properties (Variables)**:
   In `src/main/resources/static/css/main.css`, all visual tokens are defined as CSS variables:
   * **Light Theme (`:root`, `[data-theme="light"]`)**:
     * Background: `#f8fafc` (clean slate off-white)
     * Card Surface: `#ffffff`
     * Primary Accent: `#2563eb` (royal blue)
     * Text: `#0f172a` (deep slate)
     * Borders: `#e2e8f0`
   * **Dark Theme (`[data-theme="dark"]`)**:
     * Background: `#0b0f19` (obsidian dark)
     * Card Surface: `#111827` (elevated dark slate)
     * Primary Accent: `#3b82f6` (high-contrast vivid blue)
     * Text: `#f8fafc` (bright, accessible white)
     * Borders: `#1e293b`
2. **Persistent State via LocalStorage**:
   When the user clicks the **☀️ Light / 🌙 Dark** button, `App.toggleTheme()` in `app.js` runs:
   ```javascript
   setTheme(theme) {
       document.documentElement.setAttribute('data-theme', theme);
       localStorage.setItem('campus_forum_theme', theme);
       this.updateThemeButton();
   }
   ```
   This ensures the user's preferred theme is **remembered automatically** even when closing the browser or navigating between different pages.

---

## 4. Frontend Security & Route Guard (Strict Login-First)

To prevent unauthorized public access to sensitive campus circulars and discussions:
1. **Frontend Authentication Guard**:
   In `app.js`, on every page initialization:
   ```javascript
   const user = await this.checkAuth();
   const isLoginPage = window.location.pathname.includes('login');

   if (!user && !isLoginPage) {
       window.location.replace('/login');
       return;
   }
   ```
2. **Server-Side Forwarding Protection**:
   In `PageController.java`, every route checks the `HttpSession`. If no active session exists, the server returns an HTTP 302 redirect directly to `/login`.
3. **Result**:
   **No notices, forum discussions, or user data are ever visible to unauthenticated visitors.**

---

## 5. Detailed Page-by-Page Technical Breakdown

### 1. `login.html` — Authentication & Registration Gateway
* **Dual-Tab Interface**: Single unified card offering seamless toggling between **Sign In** and **Student Registration** without navigating to separate URLs.
* **Form Validation**:
  * Mandatory college email check.
  * PRN / Roll Number verification.
  * Password strength check (minimum 6 characters).
  * Department dropdown selection.
* **Faculty Demo Helpers**: Two 1-click quick fill buttons (`🛡️ Admin` and `🎓 Student`) to demonstrate both roles effortlessly during college presentation.
* **Theme Switcher**: Standalone theme button available directly on the login screen.

---

### 2. `index.html` — Campus Portal Dashboard & Team Showcase
* **Hero Banner**: High-impact campus branding with quick-action navigation buttons.
* **Urgent Notice Ticker**: Live amber notification bar that dynamically queries and highlights critical notices pinned by the administration.
* **Live KPI Counters**: 4 interactive statistics cards displaying active notices, pinned circulars, forum discussions, and registered members.
* **Split Feed Layout**:
  * Left Column: Latest official college circulars with category tags and date stamps.
  * Right Column: Active community discussion threads with reply and view counters.
* **Team Members Section (Dedicated Showcase)**:
  * Replaces generic footer text with 4 modern, responsive profile cards.
  * Displays full names, PRN numbers, Roll Numbers, Department, and individual styled SVG avatar portraits (`team-nandini.svg`, `team-vaibhavi.svg`, `team-dimple.svg`, `team-neha.svg`).
  * CSS hover animations with dynamic border glow.

---

### 3. `notices.html` — Digital Notice Board
* **Category Pill Filter**:
  * Real-time filtering by category (*All Notices, Academic & Syllabus, Examinations, Training & Placement, Campus Life & Events*).
  * Smooth client-side re-rendering without full page reload.
* **Live Search Bar**:
  * Real-time keyword search across notice titles and descriptions.
  * Implements **debounce timing (300ms)** to optimize network requests and avoid sending requests on every single keystroke.
* **Priority Notice Highlight**:
  * Pinned notices feature a distinct red left border, light accent background, and a prominent `📌 Urgent Notice` badge.
* **Circular Document Attachments**:
  * Clean attachment download button if a PDF or circular document is attached.

---

### 4. `forum.html` — Interactive Discussion Forum
* **Thread Feed**: Displays community questions with author identity, category badge, and activity metrics.
* **Upvoting / Like System**:
  * Interactive upvote button (▲) allowing students to vote for helpful queries.
  * Asynchronous `POST /api/forum/topics/{id}/like` updates the vote count in real-time.
* **"Start New Discussion" Modal**:
  * Accessible pop-up dialog with form fields: Title, Category selection, and detailed markdown/text description.
  * Automatically validates that the user is logged in before submitting.

---

### 5. `topic-detail.html` — Thread Conversation & Response Composer
* **Topic Header**: Full question body, author's full name, PRN number, view count, and publication timestamp.
* **Discussion Responses Feed**: Displays all threaded student and faculty answers with author badges (*Student* vs *🛡️ Admin*).
* **Response Composer**:
  * Integrated multi-line text input for writing replies.
  * Instant reply posting via `POST /api/forum/topics/{id}/comments` and automatic feed refresh.
* **Content Moderation Actions**:
  * Authors and Administrators are provided with quick delete buttons for inappropriate replies.

---

### 6. `admin.html` — Restricted Administration & Moderation Console
* **Role Verification**: Script verifies `currentUser.role === 'ROLE_ADMIN'`. Normal students attempting to visit `/admin` are shown an "Access Restricted" view and redirected.
* **System Analytics**: Real-time counts of total notices, urgent notices, active discussions, and registered user accounts.
* **Tab 1: Notice Management**:
  * Table of all notices with options to Pin/Unpin, Edit, and Delete.
  * "+ Create Official Notice" modal with file attachment upload capability.
* **Tab 2: Forum Moderation**:
  * Full table of all discussions with ability to switch status (*OPEN*, *CLOSED*, *FLAGGED*) or permanently delete spam topics.
* **Tab 3: User Directory & Role Promotion**:
  * Directory of all students with their PRNs and Departments.
  * "Promote to Admin" / "Demote to Student" privilege toggling.

---

## 6. Shared Utilities & State Management (`app.js`)

All pages share the modular `App` object:
1. **`App.currentUser`**: Cached session state of the logged-in user.
2. **`App.showToast(message, type)`**: Custom floating toast notification system (Success, Error, Info) that slides in smoothly and auto-dismisses after 3.5 seconds.
3. **`App.formatDate(isoString)`**: Converts ISO database timestamps into human-readable format (e.g., *"Sep 3, 2026, 11:30 PM"*).
4. **`App.escapeHtml(str)`**: Sanitizes input strings before injecting into the DOM, preventing Cross-Site Scripting (XSS) injection attacks.
5. **`App.renderNavbar()`**: Dynamically alters navbar links depending on user authentication status and role (*Admin Panel button shown only to Administrators*).

---

## 7. Faculty Presentation / Viva Q&A Guide

Prepare for your project viva with these common questions asked by evaluators:

### Q1: "Why did you build the frontend using HTML, CSS, and Vanilla JavaScript instead of React or Angular?"
> **Answer**:  
> *"Our project requirements specifically called for HTML5, CSS3, JavaScript, Java, and MySQL. Using modern Vanilla ES6+ JavaScript with the Fetch API allows us to build a lightning-fast, Single-Page-Application feel with zero build-tool bloat, zero heavy node_modules dependencies, and instant browser rendering. It also makes the code transparent and easy to evaluate."*

### Q2: "How did you implement the Light and Dark mode?"
> **Answer**:  
> *"We implemented CSS Custom Properties (Variables) on the root element. When the user clicks the theme toggle button, a JavaScript function toggles the `data-theme` attribute on the root `<html>` tag between `light` and `dark`. All colors, backgrounds, borders, and shadows dynamically update through CSS variables. Additionally, the user's choice is saved in browser `localStorage`, ensuring the theme persists across sessions."*

### Q3: "How is access control handled on the frontend?"
> **Answer**:  
> *"We implemented a two-tier protection system. On the server side, Spring Boot's `PageController` checks the HTTP Session and redirects unauthenticated requests with an HTTP 302 redirect to `/login`. On the client side, our shared `app.js` module executes `checkAuth()` before rendering any page. If the user is unauthenticated, they are immediately redirected to `/login`, ensuring no protected content is ever exposed."*

### Q4: "How does the search and filter mechanism work without page refreshes?"
> **Answer**:  
> *"We use asynchronous `fetch()` API calls to our Spring Boot REST endpoints (`/api/notices?categoryId=...&query=...`). For the search bar, we implemented a **debounce technique** (300ms delay) so that network requests are only triggered after the user stops typing, preventing unnecessary server load."*

### Q5: "How does the application prevent XSS (Cross-Site Scripting) attacks when displaying user comments?"
> **Answer**:  
> *"All dynamic text submitted by users—such as notice titles, forum posts, and comments—passes through an `App.escapeHtml()` sanitization function that converts characters like `<`, `>`, `&`, and `\"` into HTML entities before inserting them into the DOM."*

---

## 8. Summary of Frontend Files

| File Path | Lines | Key Responsibility |
| :--- | :--- | :--- |
| `src/main/resources/static/css/main.css` | ~600 | Design system, Light & Dark themes, responsive layout, cards, animations |
| `src/main/resources/static/js/app.js` | ~130 | Auth state, theme switcher, toast notifications, date formatting, XSS protection |
| `src/main/resources/static/login.html` | ~210 | Dual Sign-In/Register card, validation, demo login helpers |
| `src/main/resources/static/index.html` | ~220 | Homepage dashboard, live statistics, feeds, and Team Members section |
| `src/main/resources/static/notices.html` | ~210 | Digital Notice Board, category filters, debounced search, urgent notices |
| `src/main/resources/static/forum.html` | ~230 | Community forum feed, upvoting mechanism, "Start Discussion" modal |
| `src/main/resources/static/topic-detail.html` | ~225 | Discussion thread, replies feed, interactive response composer |
| `src/main/resources/static/admin.html` | ~280 | Administrator dashboard, notice publisher, forum moderation, user role toggling |
| `src/main/resources/static/images/*.svg` | 4 files | Profile avatar illustrations for each project team member |
