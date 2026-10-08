package com.campusforum.service;

import com.campusforum.model.StudentDocument;
import com.campusforum.model.User;
import com.campusforum.repository.StudentDocumentRepository;
import com.campusforum.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {

    private final StudentDocumentRepository documentRepository;
    private final UserRepository userRepository;

    public DocumentService(StudentDocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    public List<StudentDocument> getDocumentsForStudent(Long studentId) {
        return documentRepository.findByStudentIdOrderByUploadDateDesc(studentId);
    }

    public StudentDocument getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + id));
    }

    public StudentDocument uploadDocument(Long studentId, String title, String type, String filePath, String fileName, Long fileSize) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        StudentDocument doc = new StudentDocument(student, title.trim(), type, filePath, fileName, fileSize);
        return documentRepository.save(doc);
    }

    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new IllegalArgumentException("Document not found with ID: " + id);
        }
        documentRepository.deleteById(id);
    }
}
