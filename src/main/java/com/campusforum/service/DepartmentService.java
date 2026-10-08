package com.campusforum.service;

import com.campusforum.model.Department;
import com.campusforum.model.Role;
import com.campusforum.model.User;
import com.campusforum.repository.DepartmentRepository;
import com.campusforum.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public DepartmentService(DepartmentRepository departmentRepository, UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + id));
    }

    public Department createDepartment(String name, String code, String description, Long hodId, Long managerId) {
        if (departmentRepository.existsByCode(code.trim().toUpperCase())) {
            throw new IllegalArgumentException("Department code already exists: " + code);
        }

        Department dept = new Department();
        dept.setName(name.trim());
        dept.setCode(code.trim().toUpperCase());
        dept.setDescription(description != null ? description.trim() : "");

        if (hodId != null) {
            userRepository.findById(hodId).ifPresent(user -> {
                user.setDesignation("HOD");
                dept.setHod(user);
            });
        }
        if (managerId != null) {
            userRepository.findById(managerId).ifPresent(user -> {
                user.setDesignation("DEPARTMENT_MANAGER");
                dept.setManager(user);
            });
        }

        return departmentRepository.save(dept);
    }

    public Department updateDepartment(Long id, String name, String code, String description, Long hodId, Long managerId) {
        Department dept = getDepartmentById(id);
        dept.setName(name.trim());
        dept.setCode(code.trim().toUpperCase());
        dept.setDescription(description != null ? description.trim() : "");

        if (hodId != null) {
            userRepository.findById(hodId).ifPresent(user -> {
                user.setDesignation("HOD");
                dept.setHod(user);
            });
        } else {
            dept.setHod(null);
        }

        if (managerId != null) {
            userRepository.findById(managerId).ifPresent(user -> {
                user.setDesignation("DEPARTMENT_MANAGER");
                dept.setManager(user);
            });
        } else {
            dept.setManager(null);
        }

        return departmentRepository.save(dept);
    }

    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Department not found with ID: " + id);
        }
        departmentRepository.deleteById(id);
    }

    public long countDepartments() {
        return departmentRepository.count();
    }
}
