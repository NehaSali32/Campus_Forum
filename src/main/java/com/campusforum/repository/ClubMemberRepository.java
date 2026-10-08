package com.campusforum.repository;

import com.campusforum.model.ClubMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {

    List<ClubMember> findByClubIdAndStatus(Long clubId, String status);

    List<ClubMember> findByClubIdOrderByCreatedAtDesc(Long clubId);

    List<ClubMember> findByStudentId(Long studentId);

    List<ClubMember> findByStudentIdAndStatus(Long studentId, String status);

    Optional<ClubMember> findByClubIdAndStudentId(Long clubId, Long studentId);

    boolean existsByClubIdAndStudentIdAndStatus(Long clubId, Long studentId, String status);

    @Query("SELECT cm FROM ClubMember cm WHERE cm.club.leader.id = :leaderId AND cm.status = 'PENDING' ORDER BY cm.createdAt DESC")
    List<ClubMember> findPendingRequestsByLeaderId(@Param("leaderId") Long leaderId);

    long countByClubIdAndStatus(Long clubId, String status);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("DELETE FROM ClubMember cm WHERE cm.student.id = :studentId")
    void deleteByStudentId(@Param("studentId") Long studentId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("UPDATE ClubMember cm SET cm.reviewedBy = null WHERE cm.reviewedBy.id = :userId")
    void unbindReviewedBy(@Param("userId") Long userId);
}
