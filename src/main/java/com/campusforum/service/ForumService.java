package com.campusforum.service;

import com.campusforum.dto.CommentRequest;
import com.campusforum.dto.TopicRequest;
import com.campusforum.model.*;
import com.campusforum.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ForumService {

    private final ForumTopicRepository topicRepository;
    private final ForumCommentRepository commentRepository;
    private final CategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;
    private final DiscussionUpvoteRepository upvoteRepository;
    private final DiscussionBookmarkRepository bookmarkRepository;
    private final NotificationRepository notificationRepository;
    private final ContentReportRepository contentReportRepository;

    public ForumService(ForumTopicRepository topicRepository,
                        ForumCommentRepository commentRepository,
                        CategoryRepository categoryRepository,
                        DepartmentRepository departmentRepository,
                        DiscussionUpvoteRepository upvoteRepository,
                        DiscussionBookmarkRepository bookmarkRepository,
                        NotificationRepository notificationRepository,
                        ContentReportRepository contentReportRepository) {
        this.topicRepository = topicRepository;
        this.commentRepository = commentRepository;
        this.categoryRepository = categoryRepository;
        this.departmentRepository = departmentRepository;
        this.upvoteRepository = upvoteRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.notificationRepository = notificationRepository;
        this.contentReportRepository = contentReportRepository;
    }

    public List<ForumTopic> getTopics(Long categoryId, String query) {
        return getTopics(categoryId, query, "all", null);
    }

    public List<ForumTopic> getTopics(Long categoryId, String query, String filter, Long currentUserId) {
        List<ForumTopic> list;
        String f = (filter != null) ? filter.trim().toLowerCase() : "all";

        switch (f) {
            case "solved":
                list = topicRepository.findBySolvedTrueOrderByCreatedAtDesc();
                break;
            case "unanswered":
                list = topicRepository.findUnansweredTopics();
                break;
            case "upvoted":
                list = topicRepository.findAllByOrderByLikesCountDescCreatedAtDesc();
                break;
            case "discussed":
                list = topicRepository.findMostDiscussedTopics();
                break;
            case "my":
                if (currentUserId != null) {
                    list = topicRepository.findByAuthorIdOrderByCreatedAtDesc(currentUserId);
                } else {
                    list = topicRepository.findAllByOrderByCreatedAtDesc();
                }
                break;
            case "saved":
            case "bookmarked":
                if (currentUserId != null) {
                    list = getUserSavedTopics(currentUserId);
                } else {
                    list = topicRepository.findAllByOrderByCreatedAtDesc();
                }
                break;
            case "recent":
            case "all":
            default:
                boolean hasCategory = (categoryId != null && categoryId > 0);
                boolean hasQuery = (query != null && !query.trim().isEmpty());

                if (hasCategory && hasQuery) {
                    list = topicRepository.searchTopicsByCategory(categoryId, query.trim());
                } else if (hasCategory) {
                    list = topicRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId);
                } else if (hasQuery) {
                    list = topicRepository.searchTopics(query.trim());
                } else {
                    list = topicRepository.findAllByOrderByCreatedAtDesc();
                }
                return list;
        }

        // Apply secondary filtering (category or query) if applied on top of status filter
        if (categoryId != null && categoryId > 0) {
            list = list.stream().filter(t -> t.getCategory() != null && t.getCategory().getId().equals(categoryId)).collect(Collectors.toList());
        }
        if (query != null && !query.trim().isEmpty()) {
            String q = query.trim().toLowerCase();
            list = list.stream().filter(t ->
                    (t.getTitle() != null && t.getTitle().toLowerCase().contains(q)) ||
                    (t.getContent() != null && t.getContent().toLowerCase().contains(q)) ||
                    (t.getTags() != null && t.getTags().toLowerCase().contains(q))
            ).collect(Collectors.toList());
        }

        return list;
    }

    public List<ForumTopic> getDepartmentTopics(Long departmentId) {
        if (departmentId == null) {
            return topicRepository.findAllByOrderByCreatedAtDesc();
        }
        return topicRepository.findByDepartmentIdOrderByCreatedAtDesc(departmentId);
    }

    @Transactional
    public ForumTopic getTopicAndIncrementViews(Long id) {
        ForumTopic topic = topicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discussion topic not found with ID: " + id));
        topic.setViewsCount(topic.getViewsCount() + 1);
        return topicRepository.save(topic);
    }

    public ForumTopic getTopicById(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discussion topic not found with ID: " + id));
    }

    public ForumTopic createTopic(TopicRequest req, User author) {
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));

        ForumTopic topic = new ForumTopic();
        topic.setTitle(req.getTitle().trim());
        topic.setContent(req.getContent().trim());
        topic.setCategory(category);
        topic.setAuthor(author);
        topic.setStatus("OPEN");
        topic.setTags(req.getTags() != null ? req.getTags().trim() : "");

        if (req.getDepartmentId() != null) {
            departmentRepository.findById(req.getDepartmentId()).ifPresent(topic::setDepartment);
        }

        return topicRepository.save(topic);
    }

    @Transactional
    public ForumComment addComment(Long topicId, CommentRequest req, User author) {
        ForumTopic topic = getTopicById(topicId);
        ForumComment comment = new ForumComment(topic, author, req.getContent().trim());
        ForumComment savedComment = commentRepository.save(comment);

        if (topic.getComments() != null) {
            topic.getComments().add(savedComment);
        }

        // Notify topic author if someone else replies
        if (!topic.getAuthor().getId().equals(author.getId())) {
            String roleBadge = (author.getRole() == Role.FACULTY || author.getRole() == Role.HOD || author.getRole() == Role.DEAN) ? " [Verified Academic]" : "";
            notificationRepository.save(new Notification(
                    topic.getAuthor().getId(),
                    "New Reply on: " + topic.getTitle(),
                    author.getFullName() + roleBadge + " replied to your discussion thread.",
                    "FORUM",
                    topic.getId()
            ));
        }

        return savedComment;
    }

    @Transactional
    public Map<String, Object> toggleUpvote(Long topicId, User user) {
        ForumTopic topic = getTopicById(topicId);
        Optional<DiscussionUpvote> existing = upvoteRepository.findByTopicIdAndUserId(topicId, user.getId());

        boolean upvoted;
        if (existing.isPresent()) {
            upvoteRepository.delete(existing.get());
            topic.setLikesCount(Math.max(0, topic.getLikesCount() - 1));
            upvoted = false;
        } else {
            upvoteRepository.save(new DiscussionUpvote(topic, user));
            topic.setLikesCount(topic.getLikesCount() + 1);
            upvoted = true;
        }

        topicRepository.save(topic);

        Map<String, Object> res = new HashMap<>();
        res.put("upvoted", upvoted);
        res.put("likesCount", topic.getLikesCount());
        return res;
    }

    public boolean hasUpvoted(Long topicId, Long userId) {
        return upvoteRepository.existsByTopicIdAndUserId(topicId, userId);
    }

    @Transactional
    public boolean toggleBookmark(Long topicId, User user) {
        ForumTopic topic = getTopicById(topicId);
        Optional<DiscussionBookmark> existing = bookmarkRepository.findByTopicIdAndUserId(topicId, user.getId());

        if (existing.isPresent()) {
            bookmarkRepository.delete(existing.get());
            return false;
        } else {
            bookmarkRepository.save(new DiscussionBookmark(topic, user));
            return true;
        }
    }

    public boolean isBookmarked(Long topicId, Long userId) {
        return bookmarkRepository.existsByTopicIdAndUserId(topicId, userId);
    }

    public List<ForumTopic> getUserSavedTopics(Long userId) {
        return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(DiscussionBookmark::getTopic)
                .collect(Collectors.toList());
    }

    public List<ForumTopic> getUserTopics(Long userId) {
        return topicRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
    }

    public List<ForumTopic> getTrendingTopics() {
        return topicRepository.findTrendingTopics();
    }

    public ForumTopic updateTopicStatus(Long topicId, String status) {
        ForumTopic topic = getTopicById(topicId);
        topic.setStatus(status.toUpperCase());
        return topicRepository.save(topic);
    }

    @Transactional
    public void deleteTopic(Long topicId) {
        if (!topicRepository.existsById(topicId)) {
            throw new IllegalArgumentException("Topic not found with ID: " + topicId);
        }
        upvoteRepository.deleteByTopicId(topicId);
        bookmarkRepository.deleteByTopicId(topicId);
        commentRepository.deleteByTopicId(topicId);
        topicRepository.deleteById(topicId);
    }

    public ForumComment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found with ID: " + commentId));
    }

    @Transactional
    public void deleteComment(Long commentId) {
        ForumComment comment = getCommentById(commentId);
        if (comment.getTopic() != null && comment.getTopic().getComments() != null) {
            comment.getTopic().getComments().remove(comment);
        }
        commentRepository.delete(comment);
    }

    public List<ForumComment> getCommentsForTopic(Long topicId) {
        return commentRepository.findByTopicIdOrderByCreatedAtAsc(topicId);
    }

    public List<ForumTopic> getRecentTopics() {
        return topicRepository.findTop5ByOrderByCreatedAtDesc();
    }

    public List<Category> getForumCategories() {
        return categoryRepository.findByTypeIn(List.of("FORUM", "BOTH"));
    }

    public long countTopics() {
        return topicRepository.count();
    }

    public long countComments() {
        return commentRepository.count();
    }

    @Transactional
    public ForumTopic toggleTopicSolved(Long topicId, User user) {
        ForumTopic topic = getTopicById(topicId);
        boolean canManage = (user.getId().equals(topic.getAuthor().getId()))
                || user.getRole() == Role.ADMIN
                || user.getRole() == Role.FACULTY
                || user.getRole() == Role.HOD
                || user.getRole() == Role.DEAN;

        if (!canManage) {
            throw new IllegalArgumentException("Only the discussion author or faculty/admin can mark a discussion as solved.");
        }

        topic.setSolved(!topic.isSolved());
        if (topic.isSolved()) {
            topic.setStatus("SOLVED");
        } else {
            topic.setStatus("OPEN");
            topic.setAcceptedCommentId(null);
        }
        return topicRepository.save(topic);
    }

    @Transactional
    public ForumComment markCommentAccepted(Long topicId, Long commentId, User user) {
        ForumTopic topic = getTopicById(topicId);
        boolean canManage = (user.getId().equals(topic.getAuthor().getId()))
                || user.getRole() == Role.ADMIN
                || user.getRole() == Role.FACULTY
                || user.getRole() == Role.HOD
                || user.getRole() == Role.DEAN;

        if (!canManage) {
            throw new IllegalArgumentException("Only the discussion author or faculty/admin can accept an answer.");
        }

        ForumComment comment = getCommentById(commentId);
        if (!comment.getTopic().getId().equals(topicId)) {
            throw new IllegalArgumentException("Comment does not belong to this discussion topic.");
        }

        // Clear existing accepted comments on this topic
        List<ForumComment> comments = commentRepository.findByTopicIdOrderByCreatedAtAsc(topicId);
        for (ForumComment c : comments) {
            if (c.isAcceptedAnswer()) {
                c.setAcceptedAnswer(false);
                commentRepository.save(c);
            }
        }

        comment.setAcceptedAnswer(true);
        ForumComment updated = commentRepository.save(comment);

        topic.setAcceptedCommentId(commentId);
        topic.setSolved(true);
        topic.setStatus("SOLVED");
        topicRepository.save(topic);

        // Notify comment author that their answer was accepted
        if (!comment.getAuthor().getId().equals(user.getId())) {
            notificationRepository.save(new Notification(
                    comment.getAuthor().getId(),
                    "Answer Accepted!",
                    "Your answer on '" + topic.getTitle() + "' was marked as the accepted solution.",
                    "FORUM",
                    topic.getId()
            ));
        }

        return updated;
    }

    public ContentReport createReport(User reporter, String targetType, Long targetId, String reason, String details) {
        String title = "Reported Content #" + targetId;
        if ("TOPIC".equalsIgnoreCase(targetType)) {
            try {
                ForumTopic t = getTopicById(targetId);
                title = t.getTitle();
            } catch (Exception ignored) {}
        } else if ("COMMENT".equalsIgnoreCase(targetType)) {
            try {
                ForumComment c = getCommentById(targetId);
                title = "Reply on: " + c.getTopic().getTitle();
            } catch (Exception ignored) {}
        }

        ContentReport report = new ContentReport(reporter, targetType.toUpperCase(), targetId, title, reason, details);
        return contentReportRepository.save(report);
    }

    public List<ContentReport> getReports(String status) {
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            return contentReportRepository.findByStatusOrderByCreatedAtDesc(status.toUpperCase());
        }
        return contentReportRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public ContentReport resolveReport(Long reportId) {
        ContentReport r = contentReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with ID: " + reportId));
        r.setStatus("RESOLVED");
        return contentReportRepository.save(r);
    }

    @Transactional
    public ContentReport dismissReport(Long reportId) {
        ContentReport r = contentReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with ID: " + reportId));
        r.setStatus("DISMISSED");
        return contentReportRepository.save(r);
    }

    public long countPendingReports() {
        return contentReportRepository.countByStatus("PENDING");
    }
}
