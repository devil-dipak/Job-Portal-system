package com.devSoft.Service;

import com.devSoft.Model.StudentProfile;

public interface StudentProfileService {

    void saveProfile(StudentProfile profile);

    void updateProfile(StudentProfile profile);

    StudentProfile getProfileByUserId(Long userId);

    StudentProfile getProfileById(Long id);
}
