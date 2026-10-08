package com.campusforum.repository;

import com.campusforum.model.CollegeResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollegeResourceRepository extends JpaRepository<CollegeResource, Long> {
    List<CollegeResource> findByCategoryOrderByCreatedAtDesc(String category);
    List<CollegeResource> findByDepartmentIdOrderByCreatedAtDesc(Long departmentId);
    List<CollegeResource> findAllByOrderByCreatedAtDesc();
}
