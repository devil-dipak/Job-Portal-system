package com.devSoft.ServiceImpl;

import com.devSoft.Model.Company;
import com.devSoft.Model.Job;
import com.devSoft.Repository.CompanyRepository;
import com.devSoft.Repository.JobRepository;
import com.devSoft.Service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class JobServiceImpl implements JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Override
    public void saveJob(Job job) {
        job.setStatus("open");
        job.setCreatedAt(LocalDate.now());
        jobRepository.save(job);
    }

    @Override
    public void updateJob(Job job) {
        jobRepository.save(job);
    }

    @Override
    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }

    @Override
    public Job getJobById(Long id) {
        return jobRepository.findById(id).orElse(null);
    }

    @Override
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    @Override
    public List<Job> getJobsByCompany(Long companyId) {
        return jobRepository.findByCompanyId(companyId);
    }

    @Override
    public List<Job> getOpenJobs() {
        return jobRepository.findByStatus("open");
    }

    @Override
    public List<Job> searchJobs(String keyword) {
        List<Job> allOpenJobs = jobRepository.findByStatus("open");
        if (keyword == null || keyword.isBlank()) {
            return allOpenJobs;
        }
        String lower = keyword.toLowerCase();
        Set<Long> companyIds = allOpenJobs.stream()
                .map(Job::getCompanyId)
                .collect(Collectors.toSet());
        Map<Long, Company> companyMap = companyRepository.findAllById(companyIds).stream()
                .collect(Collectors.toMap(Company::getId, c -> c));
        return allOpenJobs.stream()
                .filter(job -> {
                    if ((job.getTitle() != null && job.getTitle().toLowerCase().contains(lower))
                            || (job.getDescription() != null && job.getDescription().toLowerCase().contains(lower))
                            || (job.getLocation() != null && job.getLocation().toLowerCase().contains(lower))
                            || (job.getType() != null && job.getType().toLowerCase().contains(lower))) {
                        return true;
                    }
                    Company company = companyMap.get(job.getCompanyId());
                    return company != null && company.getName() != null
                            && company.getName().toLowerCase().contains(lower);
                })
                .collect(Collectors.toList());
    }
}
