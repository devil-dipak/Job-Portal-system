package com.devSoft.Controller;

import com.devSoft.Model.*;
import com.devSoft.Repository.UserRepository;
import com.devSoft.Service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class InterviewController {

    @Autowired private InterviewService interviewService;
    @Autowired private JobService jobService;
    @Autowired private CompanyService companyService;
    @Autowired private JobApplicationService jobApplicationService;
    @Autowired private ApplicationTimelineService timelineService;
    @Autowired private EmailService emailService;
    @Autowired private UserRepository userRepository;

    private User getUser(HttpSession s) { return (User) s.getAttribute("activeuser"); }
    private boolean isHR(HttpSession s) { User u = getUser(s); return u != null && ("HR".equalsIgnoreCase(u.getRole()) || "admin".equalsIgnoreCase(u.getRole())); }
    private boolean isStudent(HttpSession s) { User u = getUser(s); return u != null && "student".equalsIgnoreCase(u.getRole()); }

    @GetMapping("/hr/interviews")
    public String hrInterviews(Model model, HttpSession session) {
        if (!isHR(session)) return "LoginForm";
        User user = getUser(session);
        model.addAttribute("interviews", enrichInterviews(interviewService.getHrInterviews(user.getId())));
        return "jobs/hr-interviews";
    }

    @GetMapping("/my-interviews")
    public String studentInterviews(Model model, HttpSession session) {
        if (!isStudent(session)) return "LoginForm";
        User user = getUser(session);
        model.addAttribute("interviews", enrichInterviews(interviewService.getStudentInterviews(user.getId())));
        return "jobs/my-interviews";
    }

    @GetMapping("/job/schedule-interview")
    public String showForm(@RequestParam Long applicationId, @RequestParam Long jobId, Model model, HttpSession session) {
        if (!isHR(session)) return "LoginForm";
        JobApplication app = jobApplicationService.getApplicationById(applicationId);
        Job job = jobService.getJobById(jobId);
        User student = app != null ? userRepository.findById(app.getStudentId()).orElse(null) : null;
        model.addAttribute("application", app);
        model.addAttribute("job", job);
        model.addAttribute("student", student);
        model.addAttribute("interview", new Interview());
        return "jobs/schedule-interview";
    }

    @PostMapping("/job/schedule-interview")
    public String schedule(Interview interview, HttpSession session) {
        if (!isHR(session)) return "LoginForm";
        User user = getUser(session);
        JobApplication app = jobApplicationService.getApplicationById(interview.getApplicationId());
        if (app == null) return "redirect:/hrHome";
        interview.setHrId(user.getId());
        interview.setStudentId(app.getStudentId());
        interview.setJobId(app.getJobId());
        interviewService.scheduleInterview(interview);
        jobApplicationService.updateStatusWithRemark(app.getId(), "interview", "Interview scheduled");
        timelineService.addEntry(app.getId(), "interview", "Scheduled for " + interview.getScheduledAt(), user.getFname());
        User student = userRepository.findById(app.getStudentId()).orElse(null);
        if (student != null) {
            Job j = jobService.getJobById(app.getJobId());
            String cn = j != null && companyService.getCompanyById(j.getCompanyId()) != null
                    ? companyService.getCompanyById(j.getCompanyId()).getName() : "";
            emailService.sendInterviewScheduled(student.getEmail(), student.getFname(),
                    j != null ? j.getTitle() : "", cn, interview.getScheduledAt());
        }
        return "redirect:/job/applicants?jobId=" + app.getJobId();
    }

    private java.util.List<Interview> enrichInterviews(java.util.List<Interview> interviews) {
        for (Interview inv : interviews) {
            Job j = jobService.getJobById(inv.getJobId());
            if (j != null) {
                inv.setJobTitle(j.getTitle());
                Company c = companyService.getCompanyById(j.getCompanyId());
                if (c != null) inv.setCompanyName(c.getName());
            }
            User s = userRepository.findById(inv.getStudentId()).orElse(null);
            if (s != null) inv.setStudentName(s.getFname() + " " + s.getLname());
        }
        return interviews;
    }
}
