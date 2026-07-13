package com.devSoft.Controller;

import com.devSoft.Model.*;
import com.devSoft.Service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class SavedJobController {

    @Autowired private SavedJobService savedJobService;
    @Autowired private JobService jobService;
    @Autowired private CompanyService companyService;

    private User getUser(HttpSession s) { return (User) s.getAttribute("activeuser"); }
    private boolean isStudent(HttpSession s) { User u = getUser(s); return u != null && ("student".equalsIgnoreCase(u.getRole()) || "admin".equalsIgnoreCase(u.getRole())); }

    @GetMapping("/jobs/saved")
    public String savedJobs(Model model, HttpSession session) {
        if (!isStudent(session)) return "LoginForm";
        User user = getUser(session);
        List<SavedJob> saved = savedJobService.getSavedJobs(user.getId());
        List<Job> jobs = new ArrayList<>();
        for (SavedJob sj : saved) {
            Job job = jobService.getJobById(sj.getJobId());
            if (job != null) {
                Company c = companyService.getCompanyById(job.getCompanyId());
                if (c != null) job.setCompanyName(c.getName());
                jobs.add(job);
            }
        }
        model.addAttribute("jobs", jobs);
        return "jobs/saved-jobs";
    }

    @PostMapping("/job/save")
    public String save(@RequestParam Long jobId, HttpSession session) {
        if (!isStudent(session)) return "LoginForm";
        savedJobService.saveJob(getUser(session).getId(), jobId);
        return "redirect:/jobs/" + jobId;
    }

    @PostMapping("/job/unsave")
    public String unsave(@RequestParam Long jobId, HttpSession session) {
        if (!isStudent(session)) return "LoginForm";
        savedJobService.unsaveJob(getUser(session).getId(), jobId);
        return "redirect:/jobs/" + jobId;
    }
}
