package com.devSoft.Service;

import com.devSoft.Model.SavedJob;
import java.util.List;

public interface SavedJobService {
    void saveJob(Long studentId, Long jobId);
    void unsaveJob(Long studentId, Long jobId);
    boolean isSaved(Long studentId, Long jobId);
    List<SavedJob> getSavedJobs(Long studentId);
}
