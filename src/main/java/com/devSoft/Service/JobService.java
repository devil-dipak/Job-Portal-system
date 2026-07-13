package com.devSoft.Service;

import com.devSoft.Model.Job;

import java.util.List;

public interface JobService {

    void saveJob(Job job);

    void updateJob(Job job);

    void deleteJob(Long id);

    Job getJobById(Long id);

    List<Job> getAllJobs();

    List<Job> getJobsByCompany(Long companyId);

    List<Job> getOpenJobs();

    List<Job> searchJobs(String keyword);
}
