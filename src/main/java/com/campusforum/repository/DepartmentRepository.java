package com.campusforum.repository;

import com.campusforum.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByCode(String code);
    Optional<Department> findByName(String name);
    Optional<Department> findByHodId(Long hodId);
    boolean existsByCode(String code);
    boolean existsByName(String name);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("UPDATE Department d SET d.hod = null WHERE d.hod.id = :userId")
    void unbindHod(@org.springframework.data.repository.query.Param("userId") Long userId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("UPDATE Department d SET d.manager = null WHERE d.manager.id = :userId")
    void unbindManager(@org.springframework.data.repository.query.Param("userId") Long userId);
}
