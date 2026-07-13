package com.devSoft.Repository;

import com.devSoft.Model.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    Optional<Interview> findByApplicationId(Long applicationId);
    List<Interview> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<Interview> findByHrIdOrderByCreatedAtDesc(Long hrId);
}
