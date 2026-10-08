package com.campusforum.controller;

import com.campusforum.dto.ApiResponse;
import com.campusforum.dto.CommentRequest;
import com.campusforum.dto.TopicRequest;
import com.campusforum.model.ForumComment;
import com.campusforum.model.ForumTopic;
import com.campusforum.model.Role;
import com.campusforum.model.User;
import com.campusforum.service.ForumService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forum")
public class ForumController {

    private final ForumService forumService;

    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<List<ForumTopic>>> getTopics(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String filter,
            HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        Long currentUserId = (currentUser != null) ? currentUser.getId() : null;
        return ResponseEntity.ok(ApiResponse.ok("Topics loaded", forumService.getTopics(categoryId, query, filter, currentUserId)));
    }

    @GetMapping("/topics/recent")
    public ResponseEntity<ApiResponse<List<ForumTopic>>> getRecentTopics() {
        return ResponseEntity.ok(ApiResponse.ok("Recent topics", forumService.getRecentTopics()));
    }

    @GetMapping("/topics/trending")
    public ResponseEntity<ApiResponse<List<ForumTopic>>> getTrendingTopics() {
        return ResponseEntity.ok(ApiResponse.ok("Trending topics", forumService.getTrendingTopics()));
    }

    @GetMapping("/topics/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTopicById(@PathVariable Long id, HttpSession session) {
        try {
            ForumTopic topic = forumService.getTopicAndIncrementViews(id);
            User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);

            Map<String, Object> data = new HashMap<>();
            data.put("topic", topic);
            if (currentUser != null) {
                data.put("hasUpvoted", forumService.hasUpvoted(id, currentUser.getId()));
                data.put("isBookmarked", forumService.isBookmarked(id, currentUser.getId()));
            } else {
                data.put("hasUpvoted", false);
                data.put("isBookmarked", false);
            }

            return ResponseEntity.ok(ApiResponse.ok("Topic retrieved", data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/topics")
    public ResponseEntity<ApiResponse<ForumTopic>> createTopic(@Valid @RequestBody TopicRequest request, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Please sign in to start a discussion."));
        }

        try {
            ForumTopic created = forumService.createTopic(request, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Discussion posted successfully!", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to post discussion: " + e.getMessage()));
        }
    }

    @DeleteMapping("/topics/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTopic(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }

        ForumTopic topic = forumService.getTopicById(id);
        boolean isAuthor = topic.getAuthor() != null && topic.getAuthor().getId().equals(currentUser.getId());
        boolean isStaffOrAdmin = currentUser.getRole() == Role.ADMIN 
                || currentUser.getRole() == Role.DEAN 
                || currentUser.getRole() == Role.HOD 
                || currentUser.getRole() == Role.FACULTY;

        if (!isAuthor && !isStaffOrAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Permission denied to delete this discussion."));
        }

        try {
            forumService.deleteTopic(id);
            return ResponseEntity.ok(ApiResponse.ok("Discussion deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/topics/{id}/like")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleUpvote(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Please sign in to upvote."));
        }

        Map<String, Object> result = forumService.toggleUpvote(id, currentUser);
        boolean upvoted = (boolean) result.get("upvoted");
        return ResponseEntity.ok(ApiResponse.ok(upvoted ? "Upvoted!" : "Upvote removed.", result));
    }

    @PostMapping("/topics/{id}/bookmark")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleBookmark(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Please sign in to save discussions."));
        }

        boolean bookmarked = forumService.toggleBookmark(id, currentUser);
        Map<String, Object> res = new HashMap<>();
        res.put("bookmarked", bookmarked);
        return ResponseEntity.ok(ApiResponse.ok(bookmarked ? "Discussion saved." : "Discussion unsaved.", res));
    }

    @GetMapping("/topics/{id}/comments")
    public ResponseEntity<ApiResponse<List<ForumComment>>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Comments loaded", forumService.getCommentsForTopic(id)));
    }

    @PostMapping("/topics/{id}/comments")
    public ResponseEntity<ApiResponse<ForumComment>> addComment(@PathVariable Long id, @Valid @RequestBody CommentRequest request, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Please sign in to post a reply."));
        }

        try {
            ForumComment comment = forumService.addComment(id, request, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Reply posted successfully!", comment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to post reply: " + e.getMessage()));
        }
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }

        try {
            ForumComment comment = forumService.getCommentById(id);
            boolean canDelete = currentUser.getRole() == Role.ADMIN 
                    || currentUser.getRole() == Role.DEAN 
                    || currentUser.getRole() == Role.HOD
                    || currentUser.getRole() == Role.FACULTY
                    || (comment != null && comment.getAuthor() != null && comment.getAuthor().getId().equals(currentUser.getId()));

            if (!canDelete) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Permission denied to delete this reply."));
            }

            forumService.deleteComment(id);
            return ResponseEntity.ok(ApiResponse.ok("Reply deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/saved")
    public ResponseEntity<ApiResponse<List<ForumTopic>>> getSavedTopics(HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Saved discussions", forumService.getUserSavedTopics(currentUser.getId())));
    }

    @GetMapping("/my-discussions")
    public ResponseEntity<ApiResponse<List<ForumTopic>>> getMyDiscussions(HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        return ResponseEntity.ok(ApiResponse.ok("My discussions", forumService.getUserTopics(currentUser.getId())));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<com.campusforum.model.Category>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.ok("Forum categories", forumService.getForumCategories()));
    }

    @PostMapping("/topics/{id}/solve")
    public ResponseEntity<ApiResponse<ForumTopic>> toggleSolved(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        try {
            ForumTopic topic = forumService.toggleTopicSolved(id, currentUser);
            return ResponseEntity.ok(ApiResponse.ok(topic.isSolved() ? "Discussion marked as solved!" : "Discussion reopened.", topic));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/topics/{topicId}/accept-comment/{commentId}")
    public ResponseEntity<ApiResponse<ForumComment>> acceptComment(
            @PathVariable Long topicId,
            @PathVariable Long commentId,
            HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Authentication required"));
        }
        try {
            ForumComment accepted = forumService.markCommentAccepted(topicId, commentId, currentUser);
            return ResponseEntity.ok(ApiResponse.ok("Accepted answer marked successfully!", accepted));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/reports")
    public ResponseEntity<ApiResponse<com.campusforum.model.ContentReport>> submitReport(
            @RequestBody Map<String, Object> req,
            HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Please sign in to report content."));
        }
        try {
            String targetType = (String) req.get("targetType");
            Long targetId = Long.valueOf(req.get("targetId").toString());
            String reason = (String) req.get("reason");
            String details = req.get("details") != null ? req.get("details").toString() : "";
            com.campusforum.model.ContentReport report = forumService.createReport(currentUser, targetType, targetId, reason, details);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Report submitted for administrator review.", report));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to submit report: " + e.getMessage()));
        }
    }
}
