package com.devSoft.Config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import com.devSoft.Model.User;

public class PanelInterceptor implements HandlerInterceptor {

    private static final AntPathMatcher matcher = new AntPathMatcher();

    private static final String[] PUBLIC = {
        "/", "/login", "/signup", "/forgot-password",
        "/api/**", "/logo.svg", "/uploads/**",
        "/error", "/favicon.ico"
    };

    private static final String[] ADMIN = {
        "/admin/**", "/home",
        "/certificate/add", "/certificate/list", "/certificate/delete",
        "/certificate/edit", "/certificate/update", "/certificate/pdf",
        "/certificate/bulk",
        "/block/add", "/block/list", "/block/delete",
        "/block/edit", "/block/update", "/block/excel", "/block/pdf",
        "/department/**",
        "/company/list", "/company/approve", "/company/reject",
        "/job/toggle-status"
    };

    private static final String[] HR = {
        "/hr/**", "/hrHome",
        "/job/post", "/my-jobs",
        "/company/register"
    };

    private static final String[] STUDENT = {
        "/student/**", "/studentHome",
        "/jobs", "/jobs/**",
        "/my-applications",
        "/job/apply",
        "/job/save", "/job/unsave",
        "/job-alerts/**",
        "/my-interviews"
    };

    private static final String[] ADMIN_OR_HR = {
        "/block/explorer",
        "/job/applicants", "/job/applicant", "/job/application/**",
        "/job/edit/**", "/job/close", "/job/delete",
        "/verify", "/verify/**",
        "/company/edit"
    };

    private static final String[] AUTH_ONLY = {
        "/profile", "/profile/**",
        "/certificate/my",
        "/certificate/pdf/**", "/certificate/qr/**",
        "/application/timeline"
    };

    private boolean matches(String path, String[] patterns) {
        for (String p : patterns) {
            if (matcher.match(p, path)) return true;
        }
        return false;
    }

    private String[] allowedRoles(String path) {
        if (matches(path, ADMIN)) return new String[]{"admin"};
        if (matches(path, HR)) return new String[]{"HR"};
        if (matches(path, STUDENT)) return new String[]{"student"};
        if (matches(path, ADMIN_OR_HR)) return new String[]{"admin", "HR"};
        if (matches(path, AUTH_ONLY)) return new String[]{"admin", "HR", "student"};
        return null;
    }

    private String homeFor(String role) {
        return switch (role.toLowerCase()) {
            case "admin" -> "/home";
            case "hr" -> "/hrHome";
            case "student" -> "/studentHome";
            default -> "/";
        };
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        if (matches(path, PUBLIC)) return true;

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("activeuser") : null;

        if (user == null) {
            response.sendRedirect("/");
            return false;
        }

        String[] allowed = allowedRoles(path);
        if (allowed == null) return true;

        for (String role : allowed) {
            if (role.equalsIgnoreCase(user.getRole())) return true;
        }

        response.sendRedirect(homeFor(user.getRole()));
        return false;
    }
}
