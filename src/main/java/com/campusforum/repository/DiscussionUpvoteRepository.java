package com.campusforum.repository;

import com.campusforum.model.DiscussionUpvote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface DiscussionUpvoteRepository extends JpaRepository<DiscussionUpvote, Long> {
    Optional<DiscussionUpvote> findByTopicIdAndUserId(Long topicId, Long userId);
    boolean existsByTopicIdAndUserId(Long topicId, Long userId);
    void deleteByTopicIdAndUserId(Long topicId, Long userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM DiscussionUpvote u WHERE u.topic.id = :topicId")
    void deleteByTopicId(Long topicId);

    @Transactional
    @Modifying
    @Query("DELETE FROM DiscussionUpvote u WHERE u.user.id = :userId")
    void deleteByUserId(Long userId);

    long countByTopicId(Long topicId);
}
