package com.devSoft.Repository;

import com.devSoft.Model.JobAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobAlertRepository extends JpaRepository<JobAlert, Long> {
    List<JobAlert> findByUserId(Long userId);
    List<JobAlert> findByActiveTrue();
}
