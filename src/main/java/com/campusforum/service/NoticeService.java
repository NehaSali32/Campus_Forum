package com.campusforum.service;

import com.campusforum.dto.NoticeRequest;
import com.campusforum.model.*;
import com.campusforum.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final DepartmentRepository departmentRepository;
    private final NoticeBookmarkRepository bookmarkRepository;
    private final NoticeAcknowledgementRepository acknowledgementRepository;
    private final NotificationRepository notificationRepository;

    public NoticeService(NoticeRepository noticeRepository,
                         DepartmentRepository departmentRepository,
                         NoticeBookmarkRepository bookmarkRepository,
                         NoticeAcknowledgementRepository acknowledgementRepository,
                         NotificationRepository notificationRepository) {
        this.noticeRepository = noticeRepository;
        this.departmentRepository = departmentRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.acknowledgementRepository = acknowledgementRepository;
        this.notificationRepository = notificationRepository;
    }

    public List<Notice> getNotices(String query, String category, Long departmentId, String priority) {
        return noticeRepository.filterNotices(
                (query != null && !query.isBlank()) ? query.trim() : null,
                (category != null && !category.equalsIgnoreCase("All")) ? category.trim() : null,
                departmentId,
                (priority != null && !priority.equalsIgnoreCase("All")) ? priority.trim() : null
        );
    }

    public Notice getNoticeById(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notice not found with ID: " + id));
        notice.setViewsCount(notice.getViewsCount() + 1);
        return noticeRepository.save(notice);
    }

    public Notice createNotice(NoticeRequest req, User author) {
        Notice notice = new Notice();
        notice.setTitle(req.getTitle().trim());
        notice.setContent(req.getContent().trim());
        notice.setCategory(req.getCategory() != null ? req.getCategory().trim() : "Academic");
        notice.setPriority(req.getPriority() != null ? req.getPriority().trim() : "Normal");
        notice.setAuthor(author);
        notice.setPinned(req.isPinned());
        notice.setRequiresAcknowledgement(req.isRequiresAcknowledgement());
        notice.setAttachmentUrl(req.getAttachmentUrl());
        notice.setAttachmentName(req.getAttachmentName());
        notice.setStatus(req.getStatus() != null ? req.getStatus() : "PUBLISHED");
        notice.setScheduledDate(req.getScheduledDate());

        if (req.getDepartmentId() != null) {
            departmentRepository.findById(req.getDepartmentId()).ifPresent(notice::setDepartment);
        }

        Notice saved = noticeRepository.save(notice);

        // Generate campus notification for urgent notice
        if ("Urgent".equalsIgnoreCase(saved.getPriority())) {
            notificationRepository.save(new Notification(
                    0L, // 0 = broadcast to all
                    "Urgent Notice: " + saved.getTitle(),
                    "An urgent college circular has been published under " + saved.getCategory(),
                    "NOTICE",
                    saved.getId()
            ));
        }

        return saved;
    }

    public Notice updateNotice(Long id, NoticeRequest req) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notice not found with ID: " + id));

        notice.setTitle(req.getTitle().trim());
        notice.setContent(req.getContent().trim());
        if (req.getCategory() != null) notice.setCategory(req.getCategory().trim());
        if (req.getPriority() != null) notice.setPriority(req.getPriority().trim());
        notice.setPinned(req.isPinned());
        notice.setRequiresAcknowledgement(req.isRequiresAcknowledgement());

        if (req.getDepartmentId() != null) {
            departmentRepository.findById(req.getDepartmentId()).ifPresent(notice::setDepartment);
        } else {
            notice.setDepartment(null);
        }

        if (req.getAttachmentUrl() != null) {
            notice.setAttachmentUrl(req.getAttachmentUrl());
            notice.setAttachmentName(req.getAttachmentName());
        }

        if (req.getStatus() != null) notice.setStatus(req.getStatus());

        return noticeRepository.save(notice);
    }

    @Transactional
    public void deleteNotice(Long id) {
        if (!noticeRepository.existsById(id)) {
            throw new IllegalArgumentException("Notice not found with ID: " + id);
        }
        bookmarkRepository.deleteByNoticeId(id);
        acknowledgementRepository.deleteByNoticeId(id);
        noticeRepository.deleteById(id);
    }

    public Notice togglePin(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notice not found with ID: " + id));
        notice.setPinned(!notice.isPinned());
        return noticeRepository.save(notice);
    }

    @Transactional
    public boolean toggleBookmark(Long noticeId, User user) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("Notice not found"));

        Optional<NoticeBookmark> existing = bookmarkRepository.findByNoticeIdAndUserId(noticeId, user.getId());
        if (existing.isPresent()) {
            bookmarkRepository.delete(existing.get());
            return false; // unbookmarked
        } else {
            bookmarkRepository.save(new NoticeBookmark(notice, user));
            return true; // bookmarked
        }
    }

    public boolean isBookmarked(Long noticeId, Long userId) {
        return bookmarkRepository.existsByNoticeIdAndUserId(noticeId, userId);
    }

    public List<Notice> getUserSavedNotices(Long userId) {
        return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NoticeBookmark::getNotice)
                .collect(Collectors.toList());
    }

    public boolean acknowledgeNotice(Long noticeId, User user) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("Notice not found"));

        if (!acknowledgementRepository.existsByNoticeIdAndUserId(noticeId, user.getId())) {
            acknowledgementRepository.save(new NoticeAcknowledgement(notice, user));
            return true;
        }
        return false;
    }

    public boolean hasAcknowledged(Long noticeId, Long userId) {
        return acknowledgementRepository.existsByNoticeIdAndUserId(noticeId, userId);
    }

    public long getAcknowledgementCount(Long noticeId) {
        return acknowledgementRepository.countByNoticeId(noticeId);
    }

    public List<Notice> getRecentNotices() {
        return noticeRepository.findTop5ByOrderByIsPinnedDescCreatedAtDesc();
    }

    public long countNotices() {
        return noticeRepository.count();
    }

    public long countUrgentNotices() {
        return noticeRepository.countByPriority("Urgent");
    }

    public long countPinnedNotices() {
        return noticeRepository.countByIsPinnedTrue();
    }
}
