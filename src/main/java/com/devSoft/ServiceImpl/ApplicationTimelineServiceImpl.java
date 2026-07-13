package com.devSoft.ServiceImpl;

import com.devSoft.Model.ApplicationTimeline;
import com.devSoft.Repository.ApplicationTimelineRepository;
import com.devSoft.Service.ApplicationTimelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationTimelineServiceImpl implements ApplicationTimelineService {

    @Autowired
    private ApplicationTimelineRepository timelineRepository;

    @Override
    public void addEntry(Long applicationId, String status, String remark, String updatedBy) {
        ApplicationTimeline entry = new ApplicationTimeline();
        entry.setApplicationId(applicationId);
        entry.setStatus(status);
        entry.setRemark(remark);
        entry.setUpdatedBy(updatedBy);
        entry.setCreatedAt(LocalDateTime.now());
        timelineRepository.save(entry);
    }

    @Override
    public List<ApplicationTimeline> getTimeline(Long applicationId) {
        return timelineRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId);
    }
}
