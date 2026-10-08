package com.campusforum.controller;

import com.campusforum.model.Role;
import com.campusforum.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    private boolean isAuthenticated(HttpSession session) {
        return session.getAttribute(AuthController.SESSION_USER_KEY) != null;
    }

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        if (isAuthenticated(session)) {
            return "redirect:/";
        }
        return "forward:/login.html";
    }

    @GetMapping("/notices")
    public String notices(HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        return "forward:/notices.html";
    }

    @GetMapping("/forum")
    public String forum(HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        return "forward:/forum.html";
    }

    @GetMapping("/topic-detail")
    public String topicDetail(HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        return "forward:/topic-detail.html";
    }

    @GetMapping("/events")
    public String events(HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        return "forward:/events.html";
    }

    @GetMapping("/student")
    public String student(HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        return "forward:/student.html";
    }

    @GetMapping("/faculty")
    public String faculty(HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        return "forward:/faculty.html";
    }

    @GetMapping("/admin")
    public String admin(HttpSession session) {
        User user = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (user == null) {
            return "redirect:/login";
        }
        if (user.getRole() != Role.ADMIN) {
            return "redirect:/";
        }
        return "forward:/admin.html";
    }

    @GetMapping("/hod")
    public String hod(HttpSession session) {
        User user = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (user == null) {
            return "redirect:/login";
        }
        return "redirect:/faculty?view=hod";
    }

    @GetMapping("/dean")
    public String dean(HttpSession session) {
        User user = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (user == null) {
            return "redirect:/login";
        }
        return "redirect:/faculty?view=dean";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        return "forward:/profile.html";
    }

    @GetMapping("/resources")
    public String resources(HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        return "forward:/resources.html";
    }

    @GetMapping("/documents")
    public String documents(HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        return "forward:/documents.html";
    }

    @GetMapping("/about")
    public String about() {
        return "forward:/about.html";
    }
}
