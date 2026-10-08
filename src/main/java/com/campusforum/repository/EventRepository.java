package com.campusforum.repository;

import com.campusforum.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatusOrderByEventDateAsc(String status);
    List<Event> findByCategoryAndStatusOrderByEventDateAsc(String category, String status);
    List<Event> findByDepartmentIdAndStatusOrderByEventDateAsc(Long departmentId, String status);
    List<Event> findTop5ByStatusOrderByEventDateAsc(String status);
    List<Event> findAllByOrderByCreatedAtDesc();
    List<Event> findByCreatedByIdOrderByCreatedAtDesc(Long createdById);
    List<Event> findByStatusOrderByCreatedAtDesc(String status);
    List<Event> findByDepartmentIdAndStatusOrderByCreatedAtDesc(Long departmentId, String status);
    long countByStatus(String status);
    long countByDepartmentIdAndStatus(Long departmentId, String status);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("UPDATE Event e SET e.approvedBy = null WHERE e.approvedBy.id = :userId")
    void unbindApprovedBy(@org.springframework.data.repository.query.Param("userId") Long userId);
}
