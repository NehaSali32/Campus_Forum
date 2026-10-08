package com.campusforum.service;

import com.campusforum.model.CollegeResource;
import com.campusforum.repository.CollegeResourceRepository;
import com.campusforum.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceService {

    private final CollegeResourceRepository resourceRepository;
    private final DepartmentRepository departmentRepository;

    public ResourceService(CollegeResourceRepository resourceRepository, DepartmentRepository departmentRepository) {
        this.resourceRepository = resourceRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<CollegeResource> getAllResources(String category, Long departmentId) {
        if (category != null && !category.equalsIgnoreCase("All")) {
            return resourceRepository.findByCategoryOrderByCreatedAtDesc(category.trim());
        } else if (departmentId != null && departmentId > 0) {
            return resourceRepository.findByDepartmentIdOrderByCreatedAtDesc(departmentId);
        } else {
            return resourceRepository.findAllByOrderByCreatedAtDesc();
        }
    }

    public CollegeResource getResourceById(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found with ID: " + id));
    }

    public CollegeResource createResource(String title, String description, String category, Long departmentId, String resourceType, String filePath, String linkUrl) {
        CollegeResource res = new CollegeResource();
        res.setTitle(title.trim());
        res.setDescription(description != null ? description.trim() : "");
        res.setCategory(category != null ? category.trim() : "Academic");
        res.setResourceType(resourceType != null ? resourceType.trim() : "FILE");
        res.setFilePath(filePath);
        res.setLinkUrl(linkUrl);

        if (departmentId != null) {
            departmentRepository.findById(departmentId).ifPresent(res::setDepartment);
        }

        return resourceRepository.save(res);
    }

    public CollegeResource updateResource(Long id, String title, String description, String category, Long departmentId, String resourceType, String filePath, String linkUrl) {
        CollegeResource res = getResourceById(id);
        res.setTitle(title.trim());
        res.setDescription(description != null ? description.trim() : "");
        res.setCategory(category != null ? category.trim() : "Academic");
        res.setResourceType(resourceType != null ? resourceType.trim() : "FILE");

        if (filePath != null) res.setFilePath(filePath);
        if (linkUrl != null) res.setLinkUrl(linkUrl);

        if (departmentId != null) {
            departmentRepository.findById(departmentId).ifPresent(res::setDepartment);
        } else {
            res.setDepartment(null);
        }

        return resourceRepository.save(res);
    }

    public void deleteResource(Long id) {
        if (!resourceRepository.existsById(id)) {
            throw new IllegalArgumentException("Resource not found with ID: " + id);
        }
        resourceRepository.deleteById(id);
    }
}
