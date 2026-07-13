package com.devSoft.Repository;

import com.devSoft.Model.ApplicationTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationTimelineRepository extends JpaRepository<ApplicationTimeline, Long> {
    List<ApplicationTimeline> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
}
