package com.campusforum.repository;

import com.campusforum.model.ContentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentReportRepository extends JpaRepository<ContentReport, Long> {

    List<ContentReport> findAllByOrderByCreatedAtDesc();

    List<ContentReport> findByStatusOrderByCreatedAtDesc(String status);

    long countByStatus(String status);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("DELETE FROM ContentReport cr WHERE cr.reporter.id = :reporterId")
    void deleteByReporterId(@org.springframework.data.repository.query.Param("reporterId") Long reporterId);
}
