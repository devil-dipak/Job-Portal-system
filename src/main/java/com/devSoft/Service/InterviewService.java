package com.devSoft.Service;

import com.devSoft.Model.Interview;
import java.util.List;

public interface InterviewService {
    void scheduleInterview(Interview interview);
    Interview getInterviewByApplication(Long applicationId);
    List<Interview> getStudentInterviews(Long studentId);
    List<Interview> getHrInterviews(Long hrId);
    void updateStatus(Long id, String status);
}
