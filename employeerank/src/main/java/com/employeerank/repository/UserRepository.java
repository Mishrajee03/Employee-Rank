package com.employeerank.repository;

import com.employeerank.entity.User;
import com.employeerank.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByRefreshToken(String refreshToken);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    List<User> findByCompanyId(Long companyId);

    @Query("SELECT u FROM User u WHERE u.isPublicProfile = true AND u.role = :role ORDER BY u.totalCredits DESC")
    Page<User> findPublicLeaderboard(@Param("role") Role role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.company.id = :companyId ORDER BY u.totalCredits DESC")
    List<User> findCompanyLeaderboard(@Param("companyId") Long companyId);

    @Query("SELECT u FROM User u WHERE u.isPublicProfile = true AND u.totalCredits >= :minCredits ORDER BY u.totalCredits DESC")
    Page<User> findByMinCredits(@Param("minCredits") Integer minCredits, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isPublicProfile = true AND (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.skills) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.jobTitle) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<User> searchPublicProfiles(@Param("query") String query, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.company.id = :companyId AND u.department = :department ORDER BY u.totalCredits DESC")
    List<User> findByCompanyAndDepartment(@Param("companyId") Long companyId, @Param("department") String department);

    @Query("SELECT COUNT(u) FROM User u WHERE u.company.id = :companyId")
    long countByCompanyId(@Param("companyId") Long companyId);
}
