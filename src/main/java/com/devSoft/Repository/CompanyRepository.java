package com.devSoft.Repository;

import com.devSoft.Model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByHrUserId(Long hrUserId);

    List<Company> findByStatus(String status);
}
