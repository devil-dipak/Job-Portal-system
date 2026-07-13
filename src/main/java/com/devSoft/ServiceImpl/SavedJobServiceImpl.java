package com.devSoft.ServiceImpl;

import com.devSoft.Model.SavedJob;
import com.devSoft.Repository.SavedJobRepository;
import com.devSoft.Service.SavedJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SavedJobServiceImpl implements SavedJobService {

    @Autowired
    private SavedJobRepository savedJobRepository;

    @Override
    public void saveJob(Long studentId, Long jobId) {
        if (!savedJobRepository.existsByStudentIdAndJobId(studentId, jobId)) {
            SavedJob sj = new SavedJob();
            sj.setStudentId(studentId);
            sj.setJobId(jobId);
            sj.setSavedAt(LocalDateTime.now());
            savedJobRepository.save(sj);
        }
    }

    @Override
    public void unsaveJob(Long studentId, Long jobId) {
        savedJobRepository.deleteByStudentIdAndJobId(studentId, jobId);
    }

    @Override
    public boolean isSaved(Long studentId, Long jobId) {
        return savedJobRepository.existsByStudentIdAndJobId(studentId, jobId);
    }

    @Override
    public List<SavedJob> getSavedJobs(Long studentId) {
        return savedJobRepository.findByStudentId(studentId);
    }
}
