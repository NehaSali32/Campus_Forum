package com.campusforum.repository;

import com.campusforum.model.ClubRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubRequestRepository extends JpaRepository<ClubRequest, Long> {
    List<ClubRequest> findByStatus(String status);
    List<ClubRequest> findByStatusOrderByCreatedAtDesc(String status);
    List<ClubRequest> findAllByOrderByCreatedAtDesc();
    List<ClubRequest> findByStudentId(Long studentId);
    List<ClubRequest> findByClubDepartmentIdAndStatusOrderByCreatedAtDesc(Long departmentId, String status);
    long countByStatus(String status);
    long countByClubDepartmentIdAndStatus(Long departmentId, String status);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("DELETE FROM ClubRequest cr WHERE cr.student.id = :studentId")
    void deleteByStudentId(@org.springframework.data.repository.query.Param("studentId") Long studentId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("UPDATE ClubRequest cr SET cr.reviewedBy = null WHERE cr.reviewedBy.id = :userId")
    void unbindReviewedBy(@org.springframework.data.repository.query.Param("userId") Long userId);
}
