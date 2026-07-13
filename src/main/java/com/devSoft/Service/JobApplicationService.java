package com.devSoft.Service;

import com.devSoft.Model.JobApplication;

import java.util.List;

public interface JobApplicationService {

    void apply(JobApplication application);

    JobApplication getApplicationById(Long id);

    void updateStatus(Long id, String status);

    void updateStatusWithRemark(Long id, String status, String remark);

    List<JobApplication> getApplicationsByJob(Long jobId);

    List<JobApplication> getApplicationsByStudent(Long studentId);

    boolean hasApplied(Long jobId, Long studentId);

    List<JobApplication> getAllApplications();
}
