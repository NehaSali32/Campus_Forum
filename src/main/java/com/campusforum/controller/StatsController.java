package com.campusforum.controller;

import com.campusforum.dto.ApiResponse;
import com.campusforum.dto.DashboardStats;
import com.campusforum.model.Role;
import com.campusforum.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final UserService userService;
    private final NoticeService noticeService;
    private final ForumService forumService;
    private final EventService eventService;
    private final DepartmentService departmentService;
    private final ClubService clubService;

    public StatsController(UserService userService,
                           NoticeService noticeService,
                           ForumService forumService,
                           EventService eventService,
                           DepartmentService departmentService,
                           ClubService clubService) {
        this.userService = userService;
        this.noticeService = noticeService;
        this.forumService = forumService;
        this.eventService = eventService;
        this.departmentService = departmentService;
        this.clubService = clubService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardStats>> getSummaryStats() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalStudents(userService.countByRole(Role.STUDENT) + userService.countByRole(Role.CLUB_LEADER));
        stats.setTotalFaculty(userService.countByRole(Role.FACULTY) + userService.countByRole(Role.HOD) + userService.countByRole(Role.DEAN));
        stats.setTotalDepartments(departmentService.countDepartments());
        stats.setTotalNotices(noticeService.countNotices());
        stats.setUrgentNotices(noticeService.countUrgentNotices());
        stats.setPinnedNotices(noticeService.countPinnedNotices());
        stats.setTotalTopics(forumService.countTopics());
        stats.setTotalComments(forumService.countComments());
        stats.setUpcomingEvents(eventService.countUpcomingEvents());
        stats.setTotalClubs(clubService.countClubs());
        stats.setPendingReports(forumService.countPendingReports());

        return ResponseEntity.ok(ApiResponse.ok("Campus summary statistics", stats));
    }
}
