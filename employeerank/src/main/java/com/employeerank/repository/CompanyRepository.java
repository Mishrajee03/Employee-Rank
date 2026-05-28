package com.employeerank.repository;

import com.employeerank.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByName(String name);
    boolean existsByName(String name);

    @Query("SELECT c FROM Company c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.industry) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Company> searchCompanies(@Param("query") String query, Pageable pageable);

    List<Company> findByIsHiringTrue();

    Page<Company> findByIsVerifiedTrue(Pageable pageable);

    @Query("SELECT c FROM Company c WHERE c.isHiring = true AND c.minCreditThreshold <= :credits")
    List<Company> findHiringCompaniesForCredits(@Param("credits") Integer credits);
}
