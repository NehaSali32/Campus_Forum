package com.campusforum.repository;

import com.campusforum.model.StudentDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentDocumentRepository extends JpaRepository<StudentDocument, Long> {
    List<StudentDocument> findByStudentIdOrderByUploadDateDesc(Long studentId);
    List<StudentDocument> findByStudentIdAndType(Long studentId, String type);
    long countByStudentId(Long studentId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("DELETE FROM StudentDocument sd WHERE sd.student.id = :studentId")
    void deleteByStudentId(@org.springframework.data.repository.query.Param("studentId") Long studentId);
}
