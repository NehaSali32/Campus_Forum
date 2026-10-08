package com.campusforum.repository;

import com.campusforum.model.NoticeBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeBookmarkRepository extends JpaRepository<NoticeBookmark, Long> {
    List<NoticeBookmark> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<NoticeBookmark> findByNoticeIdAndUserId(Long noticeId, Long userId);
    boolean existsByNoticeIdAndUserId(Long noticeId, Long userId);
    void deleteByNoticeIdAndUserId(Long noticeId, Long userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM NoticeBookmark b WHERE b.notice.id = :noticeId")
    void deleteByNoticeId(Long noticeId);

    @Transactional
    @Modifying
    @Query("DELETE FROM NoticeBookmark b WHERE b.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
