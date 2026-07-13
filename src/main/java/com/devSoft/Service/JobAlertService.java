package com.devSoft.Service;

import com.devSoft.Model.JobAlert;
import java.util.List;

public interface JobAlertService {
    void createAlert(JobAlert alert);
    List<JobAlert> getUserAlerts(Long userId);
    void toggleAlert(Long id);
    void deleteAlert(Long id);
    List<JobAlert> getActiveAlerts();
}
