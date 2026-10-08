package com.campusforum.repository;

import com.campusforum.model.Role;
import com.campusforum.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPrnRollNo(String prnRollNo);
    Optional<User> findByEmployeeId(String employeeId);

    boolean existsByEmail(String email);
    boolean existsByPrnRollNo(String prnRollNo);
    boolean existsByEmployeeId(String employeeId);

    List<User> findByRole(Role role);
    List<User> findByRoleOrderByFullNameAsc(Role role);
    List<User> findByRoleIn(List<Role> roles);
    List<User> findByRoleInOrderByFullNameAsc(List<Role> roles);
    List<User> findByDepartmentId(Long departmentId);
    List<User> findByDepartmentIdAndRole(Long departmentId, Role role);
    List<User> findByDepartmentIdAndRoleIn(Long departmentId, List<Role> roles);

    long countByRole(Role role);
    long countByRoleIn(List<Role> roles);
    long countByStatus(String status);

    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(u.prnRollNo) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(u.employeeId) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<User> searchUsers(@Param("q") String q);
}
