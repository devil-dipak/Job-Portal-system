package com.devSoft.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.devSoft.Model.Certificate;
import com.devSoft.Model.Company;
import com.devSoft.Model.Interview;
import com.devSoft.Model.Job;
import com.devSoft.Model.JobApplication;
import com.devSoft.Model.StudentProfile;
import com.devSoft.Model.User;
import com.devSoft.Repository.CertificateRepository;
import com.devSoft.Repository.UserRepository;
import com.devSoft.Service.ApplicationTimelineService;
import com.devSoft.Service.CertificateService;
import com.devSoft.Service.CompanyService;
import com.devSoft.Service.EmailService;
import com.devSoft.Service.InterviewService;
import com.devSoft.Service.JobApplicationService;
import com.devSoft.Service.JobService;
import com.devSoft.Service.StudentProfileService;
import com.devSoft.Utils.FileUploadUtil;

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ApplicationController {

    @Autowired private JobApplicationService jobApplicationService;
    @Autowired private JobService jobService;
    @Autowired private CompanyService companyService;
    @Autowired private UserRepository userRepository;
    @Autowired private CertificateRepository certificateRepository;
    @Autowired private CertificateService certificateService;
    @Autowired private StudentProfileService studentProfileService;
    @Autowired private ApplicationTimelineService timelineService;
    @Autowired private InterviewService interviewService;
    @Autowired private EmailService emailService;

    private static final long MAX_CV_SIZE = 5 * 1024 * 1024;

    private boolean isStudent(HttpSession session) {
        User user = (User) session.getAttribute("activeuser");
        return user != null && ("student".equalsIgnoreCase(user.getRole()) || "admin".equalsIgnoreCase(user.getRole()));
    }

    private boolean isHRorAdmin(HttpSession session) {
        User user = (User) session.getAttribute("activeuser");
        if (user == null) return false;
        return "HR".equalsIgnoreCase(user.getRole()) || "admin".equalsIgnoreCase(user.getRole());
    }

    @GetMapping("/job/apply")
    public String showApplyForm(@RequestParam("jobId") Long jobId,
                                 Model model, HttpSession session) {
        if (!isStudent(session)) return "LoginForm";
        User user = (User) session.getAttribute("activeuser");

        if (jobApplicationService.hasApplied(jobId, user.getId())) {
            return "redirect:/my-applications";
        }

        Job job = jobService.getJobById(jobId);
        if (job == null) return "redirect:/jobs";

        Company company = companyService.getCompanyById(job.getCompanyId());
        List<Certificate> certs = certificateRepository.findByStudentName(user.getFname() + " " + user.getLname());

        model.addAttribute("job", job);
        model.addAttribute("company", company);
        model.addAttribute("certificates", certs);
        model.addAttribute("application", new JobApplication());
        return "jobs/apply-form";
    }

    @PostMapping("/job/apply")
    public String submitApplication(JobApplication application,
                                     @RequestParam("jobId") Long jobId,
                                     @RequestParam(value = "cvFile", required = false) MultipartFile cvFile,
                                     @RequestParam(value = "extraDocFile", required = false) MultipartFile extraDocFile,
                                     @RequestParam(value = "certIds", required = false) String certIds,
                                     HttpSession session) {
        if (!isStudent(session)) return "LoginForm";
        User user = (User) session.getAttribute("activeuser");

        if (jobApplicationService.hasApplied(jobId, user.getId())) {
            return "redirect:/my-applications";
        }

        if (cvFile != null && !cvFile.isEmpty() && cvFile.getSize() > MAX_CV_SIZE) {
            return "redirect:/job/apply?jobId=" + jobId + "&error=fileTooLarge";
        }

        application.setJobId(jobId);
        application.setStudentId(user.getId());

        if (cvFile != null && !cvFile.isEmpty()) {
            try {
                String fileName = FileUploadUtil.saveFile("uploads/cvs", cvFile.getOriginalFilename(), cvFile);
                application.setCvPath(fileName);
            } catch (Exception e) {
                return "redirect:/job/apply?jobId=" + jobId;
            }
        }

        if (extraDocFile != null && !extraDocFile.isEmpty()) {
            try {
                String fileName = FileUploadUtil.saveFile("uploads/extra-docs", extraDocFile.getOriginalFilename(), extraDocFile);
                application.setExtraDocPath(fileName);
            } catch (Exception e) {
                return "redirect:/job/apply?jobId=" + jobId;
            }
        }

        if (certIds != null && !certIds.isBlank()) {
            application.setCertificateIds(certIds);
        }

        jobApplicationService.apply(application);

        Job job = jobService.getJobById(jobId);
        if (job != null) {
            Company company = companyService.getCompanyById(job.getCompanyId());
            emailService.sendApplicationReceived(user.getEmail(), user.getFname(),
                    job.getTitle(), company != null ? company.getName() : "");
        }

        return "redirect:/my-applications";
    }

    @GetMapping("/my-applications")
    public String myApplications(Model model, HttpSession session) {
        if (!isStudent(session)) return "LoginForm";
        User user = (User) session.getAttribute("activeuser");
        List<JobApplication> apps = jobApplicationService.getApplicationsByStudent(user.getId());

        for (JobApplication app : apps) {
            Job job = jobService.getJobById(app.getJobId());
            if (job != null) {
                app.setJobTitle(job.getTitle());
                Company company = companyService.getCompanyById(job.getCompanyId());
                if (company != null) {
                    app.setCompanyName(company.getName());
                }
            }
            if (app.getCertificateIds() != null && !app.getCertificateIds().isBlank()) {
                List<Certificate> certs = new ArrayList<>();
                for (String idStr : app.getCertificateIds().split(",")) {
                    try { certs.add(certificateService.getCertById(Long.parseLong(idStr.trim()))); } catch (Exception ignored) {}
                }
                app.setCertificates(certs);
            }
        }

        model.addAttribute("applications", apps);
        return "jobs/my-applications";
    }

    @GetMapping("/hr/applicants")
    public String hrApplicants(Model model, HttpSession session) {
        if (!isHRorAdmin(session)) return "LoginForm";
        User user = (User) session.getAttribute("activeuser");
        Company hrCompany = companyService.getCompanyByHrUserId(user.getId());
        if (hrCompany == null) {
            model.addAttribute("applications", List.of());
            return "hr/applicants";
        }
        List<Job> jobs = jobService.getJobsByCompany(hrCompany.getId());
        List<JobApplication> allApps = new ArrayList<>();
        for (Job job : jobs) {
            List<JobApplication> apps = jobApplicationService.getApplicationsByJob(job.getId());
            for (JobApplication app : apps) {
                app.setJobTitle(job.getTitle());
                User student = userRepository.findById(app.getStudentId()).orElse(null);
                if (student != null) {
                    app.setStudentName(student.getFname() + " " + student.getLname());
                }
                allApps.add(app);
            }
        }
        model.addAttribute("applications", allApps);
        model.addAttribute("hrCompany", hrCompany);
        return "hr/applicants";
    }

    @GetMapping("/job/applicants")
    public String viewApplicants(@RequestParam("jobId") Long jobId,
                                  Model model, HttpSession session) {
        if (!isHRorAdmin(session)) return "LoginForm";
        User user = (User) session.getAttribute("activeuser");
        Job job = jobService.getJobById(jobId);
        if (job == null) return "redirect:/hrHome";

        if (!"admin".equalsIgnoreCase(user.getRole())) {
            Company hrCompany = companyService.getCompanyByHrUserId(user.getId());
            if (hrCompany == null || !hrCompany.getId().equals(job.getCompanyId())) {
                return "redirect:/hrHome";
            }
        }

        List<JobApplication> apps = jobApplicationService.getApplicationsByJob(jobId);
        for (JobApplication app : apps) {
            User student = userRepository.findById(app.getStudentId()).orElse(null);
            if (student != null) {
                app.setStudentName(student.getFname() + " " + student.getLname());
                StudentProfile sp = studentProfileService.getProfileByUserId(student.getId());
                if (sp != null && job != null) {
                    app.setMatchScore(calculateMatchScore(sp, job));
                }
            }
            app.setJobTitle(job.getTitle());
            Company company = companyService.getCompanyById(job.getCompanyId());
            if (company != null) {
                app.setCompanyName(company.getName());
            }
        }

        model.addAttribute("job", job);
        model.addAttribute("applications", apps);
        return "jobs/applicants";
    }

    @GetMapping("/job/applicant")
    public String viewApplicant(@RequestParam("applicationId") Long applicationId,
                                 Model model, HttpSession session) {
        if (!isHRorAdmin(session)) return "LoginForm";
        User user = (User) session.getAttribute("activeuser");
        JobApplication app = jobApplicationService.getApplicationById(applicationId);
        if (app == null) return "redirect:/hrHome";

        Job job = jobService.getJobById(app.getJobId());

        if (!"admin".equalsIgnoreCase(user.getRole())) {
            Company hrCompany = companyService.getCompanyByHrUserId(user.getId());
            if (hrCompany == null || job == null || !hrCompany.getId().equals(job.getCompanyId())) {
                return "redirect:/hrHome";
            }
        }
        Company company = null;
        if (job != null) {
            app.setJobTitle(job.getTitle());
            company = companyService.getCompanyById(job.getCompanyId());
            if (company != null) app.setCompanyName(company.getName());
        }

        User student = userRepository.findById(app.getStudentId()).orElse(null);
        if (student != null) {
            app.setStudentName(student.getFname() + " " + student.getLname());
        }

        List<Certificate> certificates = new ArrayList<>();
        if (app.getCertificateIds() != null && !app.getCertificateIds().isBlank()) {
            for (String idStr : app.getCertificateIds().split(",")) {
                try { certificates.add(certificateService.getCertById(Long.parseLong(idStr.trim()))); } catch (Exception ignored) {}
            }
        }
        if (certificates.isEmpty() && student != null) {
            certificates = certificateRepository.findByStudentName(student.getFname() + " " + student.getLname());
        }
        app.setCertificates(certificates);

        if (student != null) {
            StudentProfile sp = studentProfileService.getProfileByUserId(student.getId());
            model.addAttribute("profile", sp);
            if (sp != null && job != null) {
                app.setMatchScore(calculateMatchScore(sp, job));
            }
        }

        model.addAttribute("job", job);
        model.addAttribute("company", company);
        model.addAttribute("application", app);
        model.addAttribute("certificates", certificates);
        model.addAttribute("timeline", timelineService.getTimeline(applicationId));
        model.addAttribute("interview", interviewService.getInterviewByApplication(applicationId));
        return "jobs/applicant-detail";
    }

    @PostMapping("/job/application/update")
    public String updateApplicationStatus(@RequestParam("id") Long id,
                                           @RequestParam("status") String status,
                                           @RequestParam(value = "remark", required = false) String remark,
                                           HttpSession session) {
        if (!isHRorAdmin(session)) return "LoginForm";
        User user = (User) session.getAttribute("activeuser");
        JobApplication app = jobApplicationService.getApplicationById(id);

        if (app != null && !"admin".equalsIgnoreCase(user.getRole())) {
            Job job = jobService.getJobById(app.getJobId());
            Company hrCompany = companyService.getCompanyByHrUserId(user.getId());
            if (hrCompany == null || job == null || !hrCompany.getId().equals(job.getCompanyId())) {
                return "redirect:/hrHome";
            }
        }

        if (app != null) {
            String r = (remark != null && !remark.isBlank()) ? remark : "Status changed to " + status;
            jobApplicationService.updateStatusWithRemark(id, status, r);
            timelineService.addEntry(id, status, r, user.getFname());
            User student = userRepository.findById(app.getStudentId()).orElse(null);
            if (student != null) {
                Job job = jobService.getJobById(app.getJobId());
                emailService.sendApplicationStatusChange(student.getEmail(), student.getFname(),
                        job != null ? job.getTitle() : "", status);
            }
        }
        return "redirect:/job/applicants?jobId=" + (app != null ? app.getJobId() : "");
    }

    private int calculateMatchScore(StudentProfile profile, Job job) {
        if (profile.getSkills() == null || job.getRequirements() == null) return 0;
        int score = 0;
        String[] skills = profile.getSkills().toLowerCase().split("[,\\s]+");
        String[] reqs = job.getRequirements().toLowerCase().split("[,\\s]+");
        int matched = 0;
        for (String req : reqs) {
            if (req.trim().isEmpty()) continue;
            for (String skill : skills) {
                if (skill.trim().equals(req.trim()) || skill.trim().contains(req.trim()) || req.trim().contains(skill.trim())) {
                    matched++;
                    break;
                }
            }
        }
        if (reqs.length > 0) {
            score = (matched * 100) / reqs.length;
        }
        return Math.min(score, 100);
    }
}
