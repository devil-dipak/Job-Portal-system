package com.devSoft.Controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.devSoft.Model.User;
import com.devSoft.Utils.FileUploadUtil;
import com.devSoft.Utils.FileUploadUtil.FileItem;

import jakarta.servlet.http.HttpSession;

@Controller
public class GalleryController {

    private static final String BASE = "uploads";

    private static final Map<String, String> CATEGORIES = new LinkedHashMap<>();
    static {
        CATEGORIES.put("profile-pictures", "Profile Pictures");
        CATEGORIES.put("company-logos", "Company Logos");
        CATEGORIES.put("certificates", "Certificates");
    }

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("activeuser");
        return user != null && "admin".equalsIgnoreCase(user.getRole());
    }

    @GetMapping("/admin/gallery")
    public String gallery(Model model, HttpSession session) {
        if (!isAdmin(session)) return "LoginForm";
        Map<String, Object> categories = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : CATEGORIES.entrySet()) {
            Map<String, Object> cat = new LinkedHashMap<>();
            cat.put("label", e.getValue());
            cat.put("dir", BASE + "/" + e.getKey());
            cat.put("files", FileUploadUtil.listFiles(BASE + "/" + e.getKey()));
            categories.put(e.getKey(), cat);
        }
        model.addAttribute("categories", categories);
        return "admin/gallery";
    }

    @GetMapping("/api/gallery/list")
    @ResponseBody
    public List<FileItem> listFiles(@RequestParam(defaultValue = "") String dir) {
        String path = dir.isEmpty() ? BASE : BASE + "/" + dir;
        return FileUploadUtil.listFiles(path);
    }
}
