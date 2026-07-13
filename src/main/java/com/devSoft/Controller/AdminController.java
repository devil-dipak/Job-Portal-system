package com.devSoft.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.devSoft.Model.Certificate;
import com.devSoft.Model.Company;
import com.devSoft.Model.Job;
import com.devSoft.Model.JobApplication;
import com.devSoft.Model.StudentProfile;
import com.devSoft.Model.User;
import com.devSoft.Repository.CertificateRepository;
import com.devSoft.Repository.CompanyRepository;
import com.devSoft.Repository.UserRepository;
import com.devSoft.Service.CertificateService;
import com.devSoft.Service.CompanyService;
import com.devSoft.Service.JobApplicationService;
import com.devSoft.Service.JobService;
import com.devSoft.Service.StudentProfileService;
import jakarta.servlet.http.HttpSession;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private JobService jobService;

    @Autowired
    private JobApplicationService jobApplicationService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private StudentProfileService studentProfileService;

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("activeuser");
        return user != null && "admin".equalsIgnoreCase(user.getRole());
    }

    @GetMapping("/admin/activities")
    public String activities(Model model, HttpSession session) {
        if (!isAdmin(session)) return "LoginForm";
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalJobs", jobService.getAllJobs().size());
        model.addAttribute("totalApplications", jobApplicationService.getAllApplications().size());
        model.addAttribute("totalCertificates", certificateRepository.count());

        List<Company> allCompanies = companyService.getAllCompanies();
        model.addAttribute("totalCompanies", allCompanies.size());
        model.addAttribute("companies", allCompanies);

        List<Company> pendingCompanies = allCompanies.stream()
                .filter(c -> c.getStatus() == null || "pending".equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.toList());
        model.addAttribute("pendingCompanies", pendingCompanies);

        model.addAttribute("jobs", jobService.getAllJobs());
        return "admin/activities";
    }

    @GetMapping("/admin/users")
    public String users(Model model, HttpSession session) {
        if (!isAdmin(session)) return "LoginForm";
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @GetMapping("/admin/companies")
    public String companies(Model model, HttpSession session) {
        if (!isAdmin(session)) return "LoginForm";
        List<Company> companyList = companyService.getAllCompanies();
        java.util.Map<Long, String> hrNames = new java.util.HashMap<>();
        java.util.Map<Long, String> hrEmails = new java.util.HashMap<>();
        for (Company c : companyList) {
            if (c.getHrUserId() != null) {
                User hr = userRepository.findById(c.getHrUserId()).orElse(null);
                if (hr != null) {
                    hrNames.put(c.getId(), hr.getFname() + " " + hr.getLname());
                    hrEmails.put(c.getId(), hr.getEmail());
                }
            }
        }
        model.addAttribute("companies", companyList);
        model.addAttribute("hrNames", hrNames);
        model.addAttribute("hrEmails", hrEmails);
        return "admin/companies";
    }

    @GetMapping("/admin/students")
    public String students(Model model, HttpSession session) {
        if (!isAdmin(session)) return "LoginForm";
        List<User> studentList = userRepository.findByRole("student");
        java.util.Map<Long, StudentProfile> profiles = new java.util.HashMap<>();
        for (User s : studentList) {
            StudentProfile sp = studentProfileService.getProfileByUserId(s.getId());
            if (sp != null) profiles.put(s.getId(), sp);
        }
        model.addAttribute("students", studentList);
        model.addAttribute("profiles", profiles);
        return "admin/students";
    }

    @GetMapping("/admin/student/{id}")
    public String studentDetail(@PathVariable Long id, Model model, HttpSession session) {
        if (!isAdmin(session)) return "LoginForm";
        User student = userRepository.findById(id).orElse(null);
        if (student == null || !"student".equalsIgnoreCase(student.getRole())) {
            return "redirect:/admin/students";
        }
        StudentProfile profile = studentProfileService.getProfileByUserId(id);
        model.addAttribute("student", student);
        model.addAttribute("profile", profile);
        return "admin/student-detail";
    }

    @GetMapping("/admin/company/{id}")
    public String companyDetail(@PathVariable Long id, Model model, HttpSession session) {
        if (!isAdmin(session)) return "LoginForm";
        Company company = companyService.getCompanyById(id);
        if (company == null) {
            return "redirect:/admin/companies";
        }
        User hr = null;
        if (company.getHrUserId() != null) {
            hr = userRepository.findById(company.getHrUserId()).orElse(null);
        }
        List<Job> jobs = jobService.getJobsByCompany(id);
        model.addAttribute("company", company);
        model.addAttribute("hr", hr);
        model.addAttribute("jobs", jobs);
        return "admin/company-detail";
    }

    @GetMapping("/admin/jobs")
    public String jobs(Model model, HttpSession session) {
        if (!isAdmin(session)) return "LoginForm";
        List<Job> jobList = jobService.getAllJobs();
        for (Job job : jobList) {
            Company company = companyService.getCompanyById(job.getCompanyId());
            if (company != null) {
                job.setCompanyName(company.getName());
            }
        }
        model.addAttribute("jobs", jobList);
        return "admin/jobs";
    }

    @GetMapping("/admin/applications")
    public String applications(Model model, HttpSession session) {
        if (!isAdmin(session)) return "LoginForm";
        List<JobApplication> appList = jobApplicationService.getAllApplications();
        for (JobApplication app : appList) {
            Job job = jobService.getJobById(app.getJobId());
            if (job != null) {
                app.setJobTitle(job.getTitle());
                Company company = companyService.getCompanyById(job.getCompanyId());
                if (company != null) {
                    app.setCompanyName(company.getName());
                }
            }
            User student = userRepository.findById(app.getStudentId()).orElse(null);
            if (student != null) {
                app.setStudentName(student.getFname() + " " + student.getLname());
            }
        }
        model.addAttribute("applications", appList);
        return "admin/applications";
    }

    @GetMapping("/admin/reports")
    public String reports(Model model, HttpSession session) {
        if (!isAdmin(session)) return "LoginForm";
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalJobs", jobService.getAllJobs().size());
        model.addAttribute("totalApplications", jobApplicationService.getAllApplications().size());
        model.addAttribute("totalCertificates", certificateRepository.count());
        model.addAttribute("totalCompanies", companyRepository.count());
        model.addAttribute("jobs", jobService.getAllJobs());
        model.addAttribute("applications", jobApplicationService.getAllApplications());
        model.addAttribute("certificates", certificateService.getAllCerts());
        return "admin/reports";
    }

    @GetMapping("/certificate/bulk")
    public String showBulkForm(HttpSession session) {
        if (!isAdmin(session)) return "LoginForm";
        return "certificate-bulk";
    }

    @PostMapping("/certificate/bulk")
    public String bulkIssue(@RequestParam("csvFile") MultipartFile csvFile, Model model, HttpSession session) {
        if (!isAdmin(session)) return "LoginForm";
        int success = 0, failed = 0;
        try {
            String content = new String(csvFile.getBytes());
            String[] lines = content.split("\n");
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isBlank()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    try {
                        Certificate cert = new Certificate();
                        cert.setStudentName(parts[0].trim());
                        cert.setCourseName(parts[1].trim());
                        cert.setDepartment(parts[2].trim());
                        cert.setIssueDate(parts[3].trim());
                        cert.setIssuer(parts.length >= 5 ? parts[4].trim() : "Admin");
                        certificateService.addCert(cert);
                        success++;
                    } catch (Exception e) { failed++; }
                } else { failed++; }
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
            return "certificate-bulk";
        }
        model.addAttribute("success", success + " certificates issued" + (failed > 0 ? ", " + failed + " failed" : ""));
        return "certificate-bulk";
    }
}
