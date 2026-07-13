package com.devSoft.ServiceImpl;

import com.devSoft.Model.JobAlert;
import com.devSoft.Repository.JobAlertRepository;
import com.devSoft.Service.JobAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobAlertServiceImpl implements JobAlertService {

    @Autowired
    private JobAlertRepository jobAlertRepository;

    @Override
    public void createAlert(JobAlert alert) {
        alert.setActive(true);
        alert.setCreatedAt(LocalDateTime.now());
        jobAlertRepository.save(alert);
    }

    @Override
    public List<JobAlert> getUserAlerts(Long userId) {
        return jobAlertRepository.findByUserId(userId);
    }

    @Override
    public void toggleAlert(Long id) {
        JobAlert alert = jobAlertRepository.findById(id).orElse(null);
        if (alert != null) {
            alert.setActive(!alert.isActive());
            jobAlertRepository.save(alert);
        }
    }

    @Override
    public void deleteAlert(Long id) {
        jobAlertRepository.deleteById(id);
    }

    @Override
    public List<JobAlert> getActiveAlerts() {
        return jobAlertRepository.findByActiveTrue();
    }
}
