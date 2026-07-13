package com.devSoft.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.devSoft.Model.Company;
import com.devSoft.Model.Job;
import com.devSoft.Model.User;
import com.devSoft.Service.CompanyService;
import com.devSoft.Service.JobApplicationService;
import com.devSoft.Service.JobService;
import com.devSoft.Service.SavedJobService;

import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private JobApplicationService jobApplicationService;

    @Autowired
    private SavedJobService savedJobService;

    private boolean isHR(HttpSession session) {
        User user = (User) session.getAttribute("activeuser");
        return user != null && ("HR".equalsIgnoreCase(user.getRole()) || "admin".equalsIgnoreCase(user.getRole()));
    }

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("activeuser");
        return user != null && "admin".equalsIgnoreCase(user.getRole());
    }

    private boolean isStudent(HttpSession session) {
        User user = (User) session.getAttribute("activeuser");
        return user != null && "student".equalsIgnoreCase(user.getRole());
    }

    @GetMapping("/jobs")
    public String listJobs(@RequestParam(value = "keyword", required = false) String keyword,
                           Model model, HttpSession session) {
        User user = (User) session.getAttribute("activeuser");
        if (user == null) return "LoginForm";

        List<Job> jobs;
        if (keyword != null && !keyword.isBlank()) {
            jobs = jobService.searchJobs(keyword);
        } else {
            jobs = jobService.getOpenJobs();
        }

        for (Job job : jobs) {
            Company company = companyService.getCompanyById(job.getCompanyId());
            if (company != null) {
                job.setCompanyName(company.getName());
            }
        }

        model.addAttribute("jobs", jobs);
        model.addAttribute("keyword", keyword);
        return "jobs/job-list";
    }

    @GetMapping("/jobs/{id}")
    public String viewJob(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("activeuser");
        if (user == null) return "LoginForm";

        Job job = jobService.getJobById(id);
        if (job == null) return "redirect:/jobs";

        Company company = companyService.getCompanyById(job.getCompanyId());
        if (company != null) {
            job.setCompanyName(company.getName());
            model.addAttribute("company", company);
        }

        if (isStudent(session)) {
            boolean hasApplied = jobApplicationService.hasApplied(id, user.getId());
            model.addAttribute("hasApplied", hasApplied);
            boolean isSaved = savedJobService.isSaved(user.getId(), id);
            model.addAttribute("isSaved", isSaved);
        }

        model.addAttribute("job", job);
        return "jobs/job-detail";
    }

    @GetMapping("/job/post")
    public String showPostJobForm(Model model, HttpSession session, RedirectAttributes redirect) {
        if (!isHR(session)) return "LoginForm";
        User user = (User) session.getAttribute("activeuser");
        Company company = companyService.getCompanyByHrUserId(user.getId());
        if (company == null) {
            redirect.addFlashAttribute("errorMsg", "You must register a company before posting a job.");
            return "redirect:/hrHome";
        }
        if (!"approved".equalsIgnoreCase(company.getStatus())) {
            redirect.addFlashAttribute("errorMsg", "Your company is not yet approved. Please wait for admin approval.");
            return "redirect:/hrHome";
        }
        model.addAttribute("company", company);
        model.addAttribute("job", new Job());
        return "jobs/post-job";
    }

    @PostMapping("/job/post")
    public String postJob(Job job, HttpSession session, RedirectAttributes redirect) {
        if (!isHR(session)) return "LoginForm";
        User user = (User) session.getAttribute("activeuser");
        Company company = companyService.getCompanyByHrUserId(user.getId());
        if (company == null) {
            redirect.addFlashAttribute("errorMsg", "You must register a company before posting a job.");
            return "redirect:/hrHome";
        }
        if (!"approved".equalsIgnoreCase(company.getStatus())) {
            redirect.addFlashAttribute("errorMsg", "Your company is not yet approved. Please wait for admin approval.");
            return "redirect:/hrHome";
        }
        job.setCompanyId(company.getId());
        jobService.saveJob(job);
        return "redirect:/my-jobs";
    }

    @GetMapping("/my-jobs")
    public String myJobs(Model model, HttpSession session) {
        if (!isHR(session)) return "LoginForm";
        User user = (User) session.getAttribute("activeuser");
        Company company = companyService.getCompanyByHrUserId(user.getId());
        if (company == null) {
            model.addAttribute("jobs", List.of());
            return "jobs/my-jobs";
        }
        List<Job> jobs = jobService.getJobsByCompany(company.getId());
        for (Job job : jobs) {
            job.setCompanyName(company.getName());
        }
        model.addAttribute("jobs", jobs);
        model.addAttribute("company", company);
        return "jobs/my-jobs";
    }

    @GetMapping("/job/close")
    public String closeJob(@RequestParam("id") Long id, HttpSession session) {
        if (!isHR(session) && !isAdmin(session)) return "LoginForm";
        Job job = jobService.getJobById(id);
        if (job != null) {
            job.setStatus("closed");
            jobService.updateJob(job);
        }
        return "redirect:/my-jobs";
    }

    @GetMapping("/job/delete")
    public String deleteJob(@RequestParam("id") Long id, HttpSession session) {
        if (!isHR(session) && !isAdmin(session)) return "LoginForm";
        jobService.deleteJob(id);
        return "redirect:/my-jobs";
    }

    @GetMapping("/job/edit/{id}")
    public String showEditJobForm(@PathVariable Long id, Model model, HttpSession session) {
        if (!isHR(session) && !isAdmin(session)) return "LoginForm";
        Job job = jobService.getJobById(id);
        if (job == null) return "redirect:/my-jobs";
        model.addAttribute("job", job);
        return "jobs/edit-job";
    }

    @PostMapping("/job/edit/{id}")
    public String updateJob(@PathVariable Long id, Job updated, HttpSession session) {
        if (!isHR(session) && !isAdmin(session)) return "LoginForm";
        Job job = jobService.getJobById(id);
        if (job == null) return "redirect:/my-jobs";
        job.setTitle(updated.getTitle());
        job.setDescription(updated.getDescription());
        job.setRequirements(updated.getRequirements());
        job.setSalary(updated.getSalary());
        job.setLocation(updated.getLocation());
        job.setType(updated.getType());
        job.setDeadline(updated.getDeadline());
        job.setContactEmail(updated.getContactEmail());
        jobService.updateJob(job);
        return "redirect:/my-jobs";
    }

    @GetMapping("/job/toggle-status")
    public String toggleStatus(@RequestParam("id") Long id, HttpSession session) {
        if (!isAdmin(session)) return "LoginForm";
        Job job = jobService.getJobById(id);
        if (job != null) {
            if ("open".equalsIgnoreCase(job.getStatus())) {
                job.setStatus("closed");
            } else {
                job.setStatus("open");
            }
            jobService.updateJob(job);
        }
        return "redirect:/admin/jobs";
    }
}
