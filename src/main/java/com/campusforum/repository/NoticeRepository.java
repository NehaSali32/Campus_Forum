package com.campusforum.repository;

import com.campusforum.model.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findAllByOrderByIsPinnedDescCreatedAtDesc();

    List<Notice> findByStatusOrderByIsPinnedDescCreatedAtDesc(String status);

    List<Notice> findByCategoryOrderByIsPinnedDescCreatedAtDesc(String category);

    List<Notice> findByDepartmentIdOrderByIsPinnedDescCreatedAtDesc(Long departmentId);

    List<Notice> findByDepartmentIsNullOrderByIsPinnedDescCreatedAtDesc();

    List<Notice> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    List<Notice> findTop5ByOrderByIsPinnedDescCreatedAtDesc();

    long countByPriority(String priority);

    long countByIsPinnedTrue();

    long countByStatus(String status);

    @Query("SELECT n FROM Notice n WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(n.content) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:category IS NULL OR :category = '' OR LOWER(:category) = 'all' OR LOWER(n.category) = LOWER(:category)) AND " +
           "(:departmentId IS NULL OR n.department.id = :departmentId OR n.department IS NULL) AND " +
           "(:priority IS NULL OR :priority = '' OR LOWER(:priority) = 'all' OR LOWER(n.priority) = LOWER(:priority)) " +
           "ORDER BY n.isPinned DESC, n.createdAt DESC")
    List<Notice> filterNotices(@Param("query") String query,
                               @Param("category") String category,
                               @Param("departmentId") Long departmentId,
                               @Param("priority") String priority);
}
