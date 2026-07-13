package com.devSoft.ServiceImpl;

import com.devSoft.Model.JobApplication;
import com.devSoft.Repository.JobApplicationRepository;
import com.devSoft.Service.ApplicationTimelineService;
import com.devSoft.Service.EmailService;
import com.devSoft.Service.JobApplicationService;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class JobApplicationServiceImpl implements JobApplicationService {

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private ApplicationTimelineService timelineService;

    @Autowired
    private EmailService emailService;

    @Override
    public void apply(JobApplication application) {
        application.setStatus("pending");
        application.setAppliedAt(LocalDateTime.now());
        jobApplicationRepository.save(application);
        timelineService.addEntry(application.getId(), "pending", "Application submitted", "System");
    }

    @Override
    public JobApplication getApplicationById(Long id) {
        return jobApplicationRepository.findById(id).orElse(null);
    }

    @Override
    public void updateStatus(Long id, String status) {
        JobApplication app = jobApplicationRepository.findById(id).orElse(null);
        if (app != null) {
            app.setStatus(status);
            jobApplicationRepository.save(app);
            timelineService.addEntry(id, status, "Status changed to " + status, "HR");
        }
    }

    @Override
    public void updateStatusWithRemark(Long id, String status, String remark) {
        JobApplication app = jobApplicationRepository.findById(id).orElse(null);
        if (app != null) {
            app.setStatus(status);
            jobApplicationRepository.save(app);
            timelineService.addEntry(id, status, remark, "HR");
        }
    }

    @Override
    public List<JobApplication> getApplicationsByJob(Long jobId) {
        return jobApplicationRepository.findByJobId(jobId);
    }

    @Override
    public List<JobApplication> getApplicationsByStudent(Long studentId) {
        return jobApplicationRepository.findByStudentId(studentId);
    }

    @Override
    public boolean hasApplied(Long jobId, Long studentId) {
        return jobApplicationRepository.findByJobIdAndStudentId(jobId, studentId).isPresent();
    }

    @Override
    public List<JobApplication> getAllApplications() {
        return jobApplicationRepository.findAll();
    }
}
