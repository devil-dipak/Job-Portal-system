package com.devSoft.ServiceImpl;

import com.devSoft.Model.Interview;
import com.devSoft.Repository.InterviewRepository;
import com.devSoft.Service.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InterviewServiceImpl implements InterviewService {

    @Autowired
    private InterviewRepository interviewRepository;

    @Override
    public void scheduleInterview(Interview interview) {
        interview.setStatus("scheduled");
        interviewRepository.save(interview);
    }

    @Override
    public Interview getInterviewByApplication(Long applicationId) {
        return interviewRepository.findByApplicationId(applicationId).orElse(null);
    }

    @Override
    public List<Interview> getStudentInterviews(Long studentId) {
        return interviewRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Override
    public List<Interview> getHrInterviews(Long hrId) {
        return interviewRepository.findByHrIdOrderByCreatedAtDesc(hrId);
    }

    @Override
    public void updateStatus(Long id, String status) {
        Interview interview = interviewRepository.findById(id).orElse(null);
        if (interview != null) {
            interview.setStatus(status);
            interviewRepository.save(interview);
        }
    }
}
