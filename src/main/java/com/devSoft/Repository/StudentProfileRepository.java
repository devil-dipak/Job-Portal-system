package com.devSoft.Repository;

import com.devSoft.Model.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    StudentProfile findByUserId(Long userId);
}
