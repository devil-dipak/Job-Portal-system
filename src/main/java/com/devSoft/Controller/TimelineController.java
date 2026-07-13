package com.devSoft.Controller;

import com.devSoft.Model.User;
import com.devSoft.Service.ApplicationTimelineService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TimelineController {

    @Autowired private ApplicationTimelineService timelineService;

    @GetMapping("/application/timeline")
    public String view(@RequestParam Long applicationId, Model model, HttpSession session) {
        if (session.getAttribute("activeuser") == null) return "LoginForm";
        model.addAttribute("timeline", timelineService.getTimeline(applicationId));
        model.addAttribute("applicationId", applicationId);
        return "jobs/application-timeline";
    }
}
