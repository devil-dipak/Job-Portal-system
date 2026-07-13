package com.devSoft.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.devSoft.Model.Certificate;
import com.devSoft.Model.StudentProfile;
import com.devSoft.Model.User;
import com.devSoft.Repository.CertificateRepository;
import com.devSoft.Service.StudentProfileService;
import com.devSoft.Utils.FileUploadUtil;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@Controller
public class ProfileController {

    @Autowired
    private StudentProfileService studentProfileService;

    @Autowired
    private CertificateRepository certificateRepository;

    private boolean isStudent(HttpSession session) {
        User user = (User) session.getAttribute("activeuser");
        return user != null && ("student".equalsIgnoreCase(user.getRole()) || "admin".equalsIgnoreCase(user.getRole()));
    }

    @GetMapping("/student/profile")
    public String viewProfile(Model model, HttpSession session) {
        if (!isStudent(session)) return "LoginForm";
        User user = (User) session.getAttribute("activeuser");
        StudentProfile profile = studentProfileService.getProfileByUserId(user.getId());

        List<Certificate> certificates = certificateRepository.findByStudentName(
                user.getFname() + " " + user.getLname());

        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("certificates", certificates);
        return "profile/student-profile";
    }

    @GetMapping("/student/profile/edit")
    public String showEditForm(Model model, HttpSession session) {
        if (!isStudent(session)) return "LoginForm";
        User user = (User) session.getAttribute("activeuser");
        StudentProfile profile = studentProfileService.getProfileByUserId(user.getId());
        if (profile == null) {
            profile = new StudentProfile();
            profile.setUserId(user.getId());
        }
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        return "profile/edit-profile";
    }

    @PostMapping("/student/profile/edit")
    public String saveProfile(StudentProfile profile,
                               @RequestParam(value = "pictureFile", required = false) MultipartFile pictureFile,
                               @RequestParam(value = "resumeFile", required = false) MultipartFile resumeFile,
                               @RequestParam(value = "certificateFile", required = false) MultipartFile certificateFile,
                               @RequestParam(value = "extraDocFile", required = false) MultipartFile extraDocFile,
                               @RequestParam(value = "galleryPicture", required = false) String galleryPicture,
                               @RequestParam(value = "galleryResume", required = false) String galleryResume,
                               @RequestParam(value = "galleryCertificate", required = false) String galleryCertificate,
                               @RequestParam(value = "galleryExtraDoc", required = false) String galleryExtraDoc,
                               HttpSession session) throws IOException {
        if (!isStudent(session)) return "LoginForm";
        User user = (User) session.getAttribute("activeuser");

        StudentProfile existing = studentProfileService.getProfileByUserId(user.getId());
        if (existing != null) {
            profile.setId(existing.getId());
        }
        profile.setUserId(user.getId());

        if (galleryPicture != null && !galleryPicture.isEmpty()) {
            profile.setProfilePicture(galleryPicture);
        } else if (pictureFile != null && !pictureFile.isEmpty()) {
            String fileName = FileUploadUtil.saveFile("uploads/profile-pictures", pictureFile.getOriginalFilename(), pictureFile);
            profile.setProfilePicture(fileName);
        } else if (existing != null) {
            profile.setProfilePicture(existing.getProfilePicture());
        }

        if (galleryResume != null && !galleryResume.isEmpty()) {
            profile.setResumePath(galleryResume);
        } else if (resumeFile != null && !resumeFile.isEmpty()) {
            String fileName = FileUploadUtil.saveFile("uploads/resumes", resumeFile.getOriginalFilename(), resumeFile);
            profile.setResumePath(fileName);
        } else if (existing != null) {
            profile.setResumePath(existing.getResumePath());
        }

        if (galleryCertificate != null && !galleryCertificate.isEmpty()) {
            profile.setCertificateFile(galleryCertificate);
        } else if (certificateFile != null && !certificateFile.isEmpty()) {
            String fileName = FileUploadUtil.saveFile("uploads/certificates", certificateFile.getOriginalFilename(), certificateFile);
            profile.setCertificateFile(fileName);
        } else if (existing != null) {
            profile.setCertificateFile(existing.getCertificateFile());
        }

        if (galleryExtraDoc != null && !galleryExtraDoc.isEmpty()) {
            profile.setExtraDocFile(galleryExtraDoc);
        } else if (extraDocFile != null && !extraDocFile.isEmpty()) {
            String fileName = FileUploadUtil.saveFile("uploads/extra-docs", extraDocFile.getOriginalFilename(), extraDocFile);
            profile.setExtraDocFile(fileName);
        } else if (existing != null) {
            profile.setExtraDocFile(existing.getExtraDocFile());
        }

        if (existing != null) {
            studentProfileService.updateProfile(profile);
        } else {
            studentProfileService.saveProfile(profile);
        }

        return "redirect:/student/profile";
    }
}
