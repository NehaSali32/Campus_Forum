package com.campusforum.repository;

import com.campusforum.model.ForumTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumTopicRepository extends JpaRepository<ForumTopic, Long> {

    List<ForumTopic> findAllByOrderByCreatedAtDesc();

    List<ForumTopic> findByCategoryIdOrderByCreatedAtDesc(Long categoryId);

    List<ForumTopic> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    List<ForumTopic> findByStatusOrderByCreatedAtDesc(String status);

    List<ForumTopic> findByDepartmentIdOrderByCreatedAtDesc(Long departmentId);

    @Query("SELECT t FROM ForumTopic t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(t.content) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(t.tags) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY t.createdAt DESC")
    List<ForumTopic> searchTopics(@Param("query") String query);

    @Query("SELECT t FROM ForumTopic t WHERE t.category.id = :categoryId AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t.content) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t.tags) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY t.createdAt DESC")
    List<ForumTopic> searchTopicsByCategory(@Param("categoryId") Long categoryId, @Param("query") String query);

    List<ForumTopic> findTop5ByOrderByCreatedAtDesc();

    List<ForumTopic> findTop5ByOrderByLikesCountDescViewsCountDesc();

    @Query("SELECT t FROM ForumTopic t WHERE t.status = 'OPEN' ORDER BY (t.viewsCount + (t.likesCount * 3)) DESC, t.createdAt DESC")
    List<ForumTopic> findTrendingTopics();

    List<ForumTopic> findBySolvedTrueOrderByCreatedAtDesc();

    List<ForumTopic> findAllByOrderByLikesCountDescCreatedAtDesc();

    @Query("SELECT t FROM ForumTopic t WHERE SIZE(t.comments) = 0 ORDER BY t.createdAt DESC")
    List<ForumTopic> findUnansweredTopics();

    @Query("SELECT t FROM ForumTopic t ORDER BY SIZE(t.comments) DESC, t.createdAt DESC")
    List<ForumTopic> findMostDiscussedTopics();

    long countByStatus(String status);
}
