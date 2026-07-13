package com.devSoft.Repository;

import com.devSoft.Model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByJobId(Long jobId);

    List<JobApplication> findByStudentId(Long studentId);

    Optional<JobApplication> findByJobIdAndStudentId(Long jobId, Long studentId);
}
