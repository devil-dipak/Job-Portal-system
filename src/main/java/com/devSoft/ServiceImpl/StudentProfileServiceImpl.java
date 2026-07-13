package com.devSoft.ServiceImpl;

import com.devSoft.Model.StudentProfile;
import com.devSoft.Repository.StudentProfileRepository;
import com.devSoft.Service.StudentProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StudentProfileServiceImpl implements StudentProfileService {

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Override
    public void saveProfile(StudentProfile profile) {
        studentProfileRepository.save(profile);
    }

    @Override
    public void updateProfile(StudentProfile profile) {
        studentProfileRepository.save(profile);
    }

    @Override
    public StudentProfile getProfileByUserId(Long userId) {
        return studentProfileRepository.findByUserId(userId);
    }

    @Override
    public StudentProfile getProfileById(Long id) {
        return studentProfileRepository.findById(id).orElse(null);
    }
}
