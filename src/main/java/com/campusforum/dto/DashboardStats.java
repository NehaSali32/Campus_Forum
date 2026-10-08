package com.campusforum.dto;

public class DashboardStats {

    private long totalStudents;
    private long totalFaculty;
    private long totalDepartments;
    private long totalNotices;
    private long urgentNotices;
    private long pinnedNotices;
    private long totalTopics;
    private long totalComments;
    private long upcomingEvents;
    private long pendingEvents;
    private long totalClubs;
    private long pendingClubRequests;
    private long pendingReports;

    public DashboardStats() {
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getTotalFaculty() {
        return totalFaculty;
    }

    public void setTotalFaculty(long totalFaculty) {
        this.totalFaculty = totalFaculty;
    }

    public long getTotalDepartments() {
        return totalDepartments;
    }

    public void setTotalDepartments(long totalDepartments) {
        this.totalDepartments = totalDepartments;
    }

    public long getTotalNotices() {
        return totalNotices;
    }

    public void setTotalNotices(long totalNotices) {
        this.totalNotices = totalNotices;
    }

    public long getUrgentNotices() {
        return urgentNotices;
    }

    public void setUrgentNotices(long urgentNotices) {
        this.urgentNotices = urgentNotices;
    }

    public long getPinnedNotices() {
        return pinnedNotices;
    }

    public void setPinnedNotices(long pinnedNotices) {
        this.pinnedNotices = pinnedNotices;
    }

    public long getTotalTopics() {
        return totalTopics;
    }

    public void setTotalTopics(long totalTopics) {
        this.totalTopics = totalTopics;
    }

    public long getTotalComments() {
        return totalComments;
    }

    public void setTotalComments(long totalComments) {
        this.totalComments = totalComments;
    }

    public long getUpcomingEvents() {
        return upcomingEvents;
    }

    public void setUpcomingEvents(long upcomingEvents) {
        this.upcomingEvents = upcomingEvents;
    }

    public long getPendingEvents() {
        return pendingEvents;
    }

    public void setPendingEvents(long pendingEvents) {
        this.pendingEvents = pendingEvents;
    }

    public long getTotalClubs() {
        return totalClubs;
    }

    public void setTotalClubs(long totalClubs) {
        this.totalClubs = totalClubs;
    }

    public long getPendingClubRequests() {
        return pendingClubRequests;
    }

    public void setPendingClubRequests(long pendingClubRequests) {
        this.pendingClubRequests = pendingClubRequests;
    }

    private long totalHods;
    private long totalDeans;

    public long getTotalHods() {
        return totalHods;
    }

    public void setTotalHods(long totalHods) {
        this.totalHods = totalHods;
    }

    public long getTotalDeans() {
        return totalDeans;
    }

    public void setTotalDeans(long totalDeans) {
        this.totalDeans = totalDeans;
    }

    public long getPendingReports() {
        return pendingReports;
    }

    public void setPendingReports(long pendingReports) {
        this.pendingReports = pendingReports;
    }
}

