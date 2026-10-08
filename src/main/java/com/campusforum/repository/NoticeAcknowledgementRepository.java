package com.campusforum.repository;

import com.campusforum.model.NoticeAcknowledgement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeAcknowledgementRepository extends JpaRepository<NoticeAcknowledgement, Long> {
    List<NoticeAcknowledgement> findByNoticeId(Long noticeId);
    List<NoticeAcknowledgement> findByUserId(Long userId);
    Optional<NoticeAcknowledgement> findByNoticeIdAndUserId(Long noticeId, Long userId);
    boolean existsByNoticeIdAndUserId(Long noticeId, Long userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM NoticeAcknowledgement a WHERE a.notice.id = :noticeId")
    void deleteByNoticeId(Long noticeId);

    @Transactional
    @Modifying
    @Query("DELETE FROM NoticeAcknowledgement a WHERE a.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    long countByNoticeId(Long noticeId);
}
