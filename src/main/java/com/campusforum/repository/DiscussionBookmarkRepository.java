package com.campusforum.repository;

import com.campusforum.model.DiscussionBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscussionBookmarkRepository extends JpaRepository<DiscussionBookmark, Long> {
    List<DiscussionBookmark> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<DiscussionBookmark> findByTopicIdAndUserId(Long topicId, Long userId);
    boolean existsByTopicIdAndUserId(Long topicId, Long userId);
    void deleteByTopicIdAndUserId(Long topicId, Long userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM DiscussionBookmark b WHERE b.topic.id = :topicId")
    void deleteByTopicId(Long topicId);

    @Transactional
    @Modifying
    @Query("DELETE FROM DiscussionBookmark b WHERE b.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
