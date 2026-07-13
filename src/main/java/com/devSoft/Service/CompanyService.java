package com.devSoft.Service;

import com.devSoft.Model.Company;

import java.util.List;

public interface CompanyService {

    void saveCompany(Company company);

    void updateCompany(Company company);

    void deleteCompany(Long id);

    Company getCompanyById(Long id);

    Company getCompanyByHrUserId(Long hrUserId);

    List<Company> getAllCompanies();

    List<Company> getCompaniesByStatus(String status);
}
