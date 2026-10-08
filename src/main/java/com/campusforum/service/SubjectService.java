package com.campusforum.service;

import com.campusforum.model.Department;
import com.campusforum.model.Subject;
import com.campusforum.model.User;
import com.campusforum.repository.DepartmentRepository;
import com.campusforum.repository.SubjectRepository;
import com.campusforum.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public SubjectService(SubjectRepository subjectRepository,
                          DepartmentRepository departmentRepository,
                          UserRepository userRepository) {
        this.subjectRepository = subjectRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public List<Subject> getSubjectsByDepartment(Long departmentId) {
        return subjectRepository.findByDepartmentId(departmentId);
    }

    public List<Subject> getSubjectsByFaculty(Long facultyId) {
        return subjectRepository.findByFacultyId(facultyId);
    }

    public Subject getSubjectById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + id));
    }

    public Subject createSubject(String name, String code, String semester, Long departmentId, Long facultyId) {
        if (subjectRepository.existsBySubjectCode(code.trim().toUpperCase())) {
            throw new IllegalArgumentException("Subject code already exists: " + code);
        }

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        User faculty = null;
        if (facultyId != null) {
            faculty = userRepository.findById(facultyId).orElse(null);
        }

        Subject subject = new Subject(name.trim(), code.trim().toUpperCase(), semester, department, faculty);
        return subjectRepository.save(subject);
    }

    public Subject updateSubject(Long id, String name, String code, String semester, Long departmentId, Long facultyId) {
        Subject subject = getSubjectById(id);
        subject.setSubjectName(name.trim());
        subject.setSubjectCode(code.trim().toUpperCase());
        subject.setSemester(semester);

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        subject.setDepartment(department);

        if (facultyId != null) {
            subject.setFaculty(userRepository.findById(facultyId).orElse(null));
        } else {
            subject.setFaculty(null);
        }

        return subjectRepository.save(subject);
    }

    public void deleteSubject(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new IllegalArgumentException("Subject not found with ID: " + id);
        }
        subjectRepository.deleteById(id);
    }
}
