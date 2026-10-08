package com.campusforum.repository;

import com.campusforum.model.ForumComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {
    List<ForumComment> findByTopicIdOrderByCreatedAtAsc(Long topicId);

    List<ForumComment> findByAuthorId(Long authorId);

    @Transactional
    @Modifying
    @Query("DELETE FROM ForumComment c WHERE c.topic.id = :topicId")
    void deleteByTopicId(Long topicId);

    @Transactional
    @Modifying
    @Query("DELETE FROM ForumComment c WHERE c.author.id = :authorId")
    void deleteByAuthorId(@org.springframework.data.repository.query.Param("authorId") Long authorId);

    long countByTopicId(Long topicId);
}
