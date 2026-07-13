package com.devSoft.Controller;

import com.devSoft.Model.*;
import com.devSoft.Service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class JobAlertController {

    @Autowired private JobAlertService jobAlertService;

    private User getUser(HttpSession s) { return (User) s.getAttribute("activeuser"); }
    private boolean isStudent(HttpSession s) { User u = getUser(s); return u != null && ("student".equalsIgnoreCase(u.getRole()) || "admin".equalsIgnoreCase(u.getRole())); }

    @GetMapping("/job-alerts")
    public String list(Model model, HttpSession session) {
        if (!isStudent(session)) return "LoginForm";
        model.addAttribute("alerts", jobAlertService.getUserAlerts(getUser(session).getId()));
        return "jobs/job-alerts";
    }

    @PostMapping("/job-alerts/create")
    public String create(JobAlert alert, HttpSession session) {
        if (!isStudent(session)) return "LoginForm";
        alert.setUserId(getUser(session).getId());
        jobAlertService.createAlert(alert);
        return "redirect:/job-alerts";
    }

    @GetMapping("/job-alerts/toggle")
    public String toggle(@RequestParam Long id, HttpSession session) {
        if (!isStudent(session)) return "LoginForm";
        jobAlertService.toggleAlert(id);
        return "redirect:/job-alerts";
    }

    @GetMapping("/job-alerts/delete")
    public String delete(@RequestParam Long id, HttpSession session) {
        if (!isStudent(session)) return "LoginForm";
        jobAlertService.deleteAlert(id);
        return "redirect:/job-alerts";
    }
}
