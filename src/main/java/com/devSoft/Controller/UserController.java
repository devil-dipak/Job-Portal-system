package com.devSoft.Controller;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.devSoft.Utils.FileUploadUtil;

import com.devSoft.Model.Company;
import com.devSoft.Model.Job;
import com.devSoft.Model.JobApplication;
import com.devSoft.Model.User;
import com.devSoft.Repository.BlockRepository;
import com.devSoft.Repository.CertificateRepository;
import com.devSoft.Repository.CompanyRepository;
import com.devSoft.Repository.DepartmentRepository;
import com.devSoft.Repository.JobApplicationRepository;
import com.devSoft.Repository.JobRepository;
import com.devSoft.Repository.UserRepository;
import com.devSoft.Service.CompanyService;
import com.devSoft.Service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{6,}$"
    );

    // ================= LOGIN PAGE =================

    @GetMapping("/")
    public String getLogin() {
        return "LoginForm";
    }

    // ================= LOGIN =================

    @PostMapping("/login")
    public String postLogin(@ModelAttribute User user,
                            Model model,
                            HttpSession session,
                            RedirectAttributes redirect) {

        User usr = userService.login(
                user.getEmail(),
                user.getPassword()
        );

        if (usr != null) {

            session.setAttribute("activeuser", usr);
            session.setMaxInactiveInterval(500);
            redirect.addFlashAttribute("loginSuccess", "Login successful!");

            // ================= ROLE LOGIC =================
            if ("student".equalsIgnoreCase(usr.getRole())) {
                return "redirect:/studentHome";
            } else if ("HR".equalsIgnoreCase(usr.getRole())) {
                return "redirect:/hrHome";
            } else if ("admin".equalsIgnoreCase(usr.getRole())) {
                return "redirect:/home";
            } else {
                // Unknown role - logout and show error
                session.invalidate();
                model.addAttribute("message", "Unknown user role: " + usr.getRole());
                return "LoginForm";
            }
        }

        model.addAttribute("message", "User does not exist");
        return "LoginForm";
    }

    // ================= SIGNUP PAGE =================

    @GetMapping("/signup")
    public String getSignup() {
        return "SignupForm";
    }

    // ================= SIGNUP =================

    @PostMapping("/signup")
    public String postSignup(@ModelAttribute User u,
                             @RequestParam String re_pass,
                             Model model) {

        if (userService.emailExists(u.getEmail())) {
            model.addAttribute("message", "Email already registered!");
            return "SignupForm";
        }

        if (!u.getPassword().equals(re_pass)) {
            model.addAttribute("message", "Passwords do not match!");
            return "SignupForm";
        }

        if (!PASSWORD_PATTERN.matcher(u.getPassword()).matches()) {
            model.addAttribute("message",
                "Password must be at least 6 characters with at least 1 uppercase letter, 1 digit, and 1 symbol");
            return "SignupForm";
        }

        userService.signup(u);
        model.addAttribute("signupSuccess", "Account created successfully! Please sign in.");
        return "LoginForm";
    }

    // ================= HR PROFILE =================

    @GetMapping("/hr/profile")
    public String hrProfile(Model model, HttpSession session) {
        User user = (User) session.getAttribute("activeuser");
        if (user == null) return "LoginForm";
        model.addAttribute("user", user);
        model.addAttribute("company", companyService.getCompanyByHrUserId(user.getId()));
        return "hr/profile";
    }

    @GetMapping("/hr/profile/edit")
    public String editHrProfile(Model model, HttpSession session) {
        User user = (User) session.getAttribute("activeuser");
        if (user == null) return "LoginForm";
        model.addAttribute("user", user);
        return "hr/profile-edit";
    }

    @PostMapping("/hr/profile/edit")
    public String saveHrProfile(@ModelAttribute User u,
                                 @RequestParam(required = false) String newPassword,
                                 HttpSession session,
                                 RedirectAttributes redirect) {
        User user = (User) session.getAttribute("activeuser");
        if (user == null) return "LoginForm";
        user.setFname(u.getFname());
        user.setLname(u.getLname());
        user.setEmail(u.getEmail());
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(newPassword);
        }
        userService.updateUser(user);
        session.setAttribute("activeuser", user);
        redirect.addFlashAttribute("message", "Profile updated successfully!");
        return "redirect:/hr/profile";
    }

    @PostMapping("/hr/profile/edit-logo")
    public String saveHrLogo(@RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
                              @RequestParam(value = "galleryLogo", required = false) String galleryLogo,
                              HttpSession session,
                              RedirectAttributes redirect) throws java.io.IOException {
        User user = (User) session.getAttribute("activeuser");
        if (user == null) return "LoginForm";
        Company company = companyService.getCompanyByHrUserId(user.getId());
        if (company == null) {
            redirect.addFlashAttribute("message", "No company found. Please register your company first.");
            return "redirect:/hr/profile/edit";
        }
        if (galleryLogo != null && !galleryLogo.isEmpty()) {
            company.setLogo(galleryLogo);
        } else if (logoFile != null && !logoFile.isEmpty()) {
            String fileName = FileUploadUtil.saveFile("uploads/company-logos", logoFile.getOriginalFilename(), logoFile);
            company.setLogo(fileName);
        }
        companyService.saveCompany(company);
        redirect.addFlashAttribute("message", "Logo updated successfully!");
        return "redirect:/hr/profile/edit";
    }

    // ================= LOGOUT =================

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "LoginForm";
    }

    // ================= PROFILE =================

    @GetMapping("/profile")
    public String getProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("activeuser");
        if (user == null) return "LoginForm";
        model.addAttribute("user", user);
        return "ProfileForm";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model, HttpSession session) {
        User user = (User) session.getAttribute("activeuser");
        if (user == null) return "LoginForm";
        model.addAttribute("user", user);
        return "profile/edit-user";
    }

    @PostMapping("/profile/edit")
    public String saveProfile(@ModelAttribute User u,
                              @RequestParam(required = false) String newPassword,
                              HttpSession session, RedirectAttributes redirect) {
        User user = (User) session.getAttribute("activeuser");
        if (user == null) return "LoginForm";
        user.setFname(u.getFname());
        user.setLname(u.getLname());
        user.setEmail(u.getEmail());
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(newPassword);
        }
        userService.updateUser(user);
        session.setAttribute("activeuser", user);
        redirect.addFlashAttribute("message", "Profile updated successfully!");
        return "redirect:/profile";
    }

    // ================= FORGOT PASSWORD =================

    @GetMapping("/forgot-password")
    public String showForgotPassword() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model) {
        if (!userService.emailExists(email)) {
            model.addAttribute("message", "No account found with that email address.");
            return "forgot-password";
        }
        // In a real app, send a reset link email here
        model.addAttribute("successMessage", "If an account with that email exists, a password reset link has been sent.");
        return "LoginForm";
    }

    // ================= STUDENT DASHBOARD =================

    @GetMapping("/studentHome")
    public String studentHome(Model model, HttpSession session) {
        User user = (User) session.getAttribute("activeuser");
        if (user == null) return "LoginForm";
        model.addAttribute("user", user);
        model.addAttribute("totalCertificates", certificateRepository.count());
        model.addAttribute("totalBlocks", blockRepository.count());
        model.addAttribute("totalUsers", userRepository.count());

        List<Job> recentJobs = jobRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt() != null && a.getCreatedAt() != null
                        ? b.getCreatedAt().compareTo(a.getCreatedAt())
                        : (b.getId() != null ? b.getId().compareTo(a.getId()) : 0))
                .limit(5).collect(Collectors.toList());
        for (Job j : recentJobs) {
            Company c = companyRepository.findById(j.getCompanyId()).orElse(null);
            if (c != null) j.setCompanyName(c.getName());
        }
        model.addAttribute("recentJobs", recentJobs);
        model.addAttribute("totalJobs", jobRepository.count());
        model.addAttribute("myApplications", jobApplicationRepository.findByStudentId(user.getId()).size());

        List<?> monthlyData = buildMonthlyJobData(jobRepository.findAll());
        model.addAttribute("monthlyLabels", monthlyData.get(0));
        model.addAttribute("monthlyValues", monthlyData.get(1));
        return "StudentHome";
    }

    // ================= HR DASHBOARD =================

    @GetMapping("/hrHome")
    public String hrHome(Model model, HttpSession session) {
        User user = (User) session.getAttribute("activeuser");
        if (user == null) return "LoginForm";
        model.addAttribute("user", user);
        model.addAttribute("totalCertificates", certificateRepository.count());
        model.addAttribute("totalBlocks", blockRepository.count());
        model.addAttribute("totalDepartments", departmentRepository.count());
        model.addAttribute("totalUsers", userRepository.count());

        Company hrCompany = companyService.getCompanyByHrUserId(user.getId());
        model.addAttribute("hrCompany", hrCompany);

        List<Job> recentJobs = jobRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt() != null && a.getCreatedAt() != null
                        ? b.getCreatedAt().compareTo(a.getCreatedAt())
                        : (b.getId() != null ? b.getId().compareTo(a.getId()) : 0))
                .limit(5).collect(Collectors.toList());
        for (Job j : recentJobs) {
            Company c = companyRepository.findById(j.getCompanyId()).orElse(null);
            if (c != null) j.setCompanyName(c.getName());
        }
        model.addAttribute("recentJobs", recentJobs);
        model.addAttribute("totalJobs", jobRepository.count());
        model.addAttribute("totalApplications", jobApplicationRepository.count());

        List<?> monthlyData = buildMonthlyJobData(jobRepository.findAll());
        model.addAttribute("monthlyLabels", monthlyData.get(0));
        model.addAttribute("monthlyValues", monthlyData.get(1));
        return "HR-Home";
    }

    // ================= ADMIN DASHBOARD =================

    @GetMapping("/home")
    public String home(Model model, HttpSession session) {
        User user = (User) session.getAttribute("activeuser");
        if (user == null) return "LoginForm";
        model.addAttribute("user", user);
        model.addAttribute("totalCertificates", certificateRepository.count());
        model.addAttribute("totalBlocks", blockRepository.count());
        model.addAttribute("totalDepartments", departmentRepository.count());
        model.addAttribute("totalUsers", userRepository.count());

        List<Job> allJobs = jobRepository.findAll();
        List<Job> recentJobs = allJobs.stream()
                .sorted((a, b) -> b.getCreatedAt() != null && a.getCreatedAt() != null
                        ? b.getCreatedAt().compareTo(a.getCreatedAt())
                        : (b.getId() != null ? b.getId().compareTo(a.getId()) : 0))
                .limit(5).collect(Collectors.toList());
        for (Job j : recentJobs) {
            Company c = companyRepository.findById(j.getCompanyId()).orElse(null);
            if (c != null) j.setCompanyName(c.getName());
        }
        model.addAttribute("recentJobs", recentJobs);
        model.addAttribute("totalJobs", allJobs.size());
        model.addAttribute("totalApplications", jobApplicationRepository.count());
        model.addAttribute("totalCompanies", companyRepository.count());

        long openJobs = allJobs.stream().filter(j -> "open".equalsIgnoreCase(j.getStatus())).count();
        long closedJobs = allJobs.stream().filter(j -> "closed".equalsIgnoreCase(j.getStatus())).count();
        model.addAttribute("openJobs", openJobs);
        model.addAttribute("closedJobs", closedJobs);

        List<JobApplication> allApps = jobApplicationRepository.findAll();
        long pendingApps = allApps.stream().filter(a -> "pending".equalsIgnoreCase(a.getStatus())).count();
        long shortlistedApps = allApps.stream().filter(a -> "shortlisted".equalsIgnoreCase(a.getStatus())).count();
        long acceptedApps = allApps.stream().filter(a -> "accepted".equalsIgnoreCase(a.getStatus())).count();
        long rejectedApps = allApps.stream().filter(a -> "rejected".equalsIgnoreCase(a.getStatus())).count();
        model.addAttribute("pendingApps", pendingApps);
        model.addAttribute("shortlistedApps", shortlistedApps);
        model.addAttribute("acceptedApps", acceptedApps);
        model.addAttribute("rejectedApps", rejectedApps);

        List<?> monthlyData = buildMonthlyJobData(allJobs);
        model.addAttribute("monthlyLabels", monthlyData.get(0));
        model.addAttribute("monthlyValues", monthlyData.get(1));
        return "Home";
    }

    private List<?> buildMonthlyJobData(List<Job> jobs) {
        Map<String, Long> monthCounts = new LinkedHashMap<>();
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        for (String m : months) monthCounts.put(m, 0L);
        for (Job j : jobs) {
            if (j.getCreatedAt() != null) {
                String key = months[j.getCreatedAt().getMonthValue() - 1];
                monthCounts.merge(key, 1L, Long::sum);
            }
        }
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        for (Map.Entry<String, Long> e : monthCounts.entrySet()) {
            labels.add(e.getKey());
            values.add(e.getValue());
        }
        return Arrays.asList(labels, values);
    }
}
