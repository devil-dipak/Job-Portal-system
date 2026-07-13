package com.devSoft.Service;

import com.devSoft.Model.ApplicationTimeline;
import java.util.List;

public interface ApplicationTimelineService {
    void addEntry(Long applicationId, String status, String remark, String updatedBy);
    List<ApplicationTimeline> getTimeline(Long applicationId);
}
