package com.campusforum.repository;

import com.campusforum.model.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {
    List<Club> findByStatus(String status);
    Optional<Club> findByClubName(String clubName);
    boolean existsByClubName(String clubName);
    List<Club> findByLeaderId(Long leaderId);
    List<Club> findByDepartmentId(Long departmentId);
    List<Club> findByDepartmentIdAndStatus(Long departmentId, String status);
    long countByStatus(String status);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("UPDATE Club c SET c.leader = null WHERE c.leader.id = :leaderId")
    void unbindLeader(@org.springframework.data.repository.query.Param("leaderId") Long leaderId);
}
