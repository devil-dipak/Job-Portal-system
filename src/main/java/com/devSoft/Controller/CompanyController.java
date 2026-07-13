package com.devSoft.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.devSoft.Model.Company;
import com.devSoft.Model.User;
import com.devSoft.Service.CompanyService;
import com.devSoft.Utils.FileUploadUtil;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@Controller
@RequestMapping("/company")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    private boolean isHR(HttpSession session) {
        User user = (User) session.getAttribute("activeuser");
        return user != null && ("HR".equalsIgnoreCase(user.getRole()) || "admin".equalsIgnoreCase(user.getRole()));
    }

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("activeuser");
        return user != null && "admin".equalsIgnoreCase(user.getRole());
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model, HttpSession session) {
        if (!isHR(session)) return "LoginForm";
        User user = (User) session.getAttribute("activeuser");
        Company existing = companyService.getCompanyByHrUserId(user.getId());
        if (existing != null) {
            model.addAttribute("company", existing);
        } else {
            model.addAttribute("company", new Company());
        }
        return "company/company-form";
    }

    @PostMapping("/register")
    public String registerCompany(Company company,
                                   @RequestParam("logoFile") MultipartFile logoFile,
                                   HttpSession session) throws IOException {
        if (!isHR(session)) return "LoginForm";
        User user = (User) session.getAttribute("activeuser");

        // If updating existing, preserve HR user and status
        if (company.getId() != null) {
            Company existing = companyService.getCompanyById(company.getId());
            if (existing != null) {
                company.setHrUserId(existing.getHrUserId());
                if (company.getStatus() == null) {
                    company.setStatus(existing.getStatus());
                }
            }
        } else {
            company.setHrUserId(user.getId());
        }

        if (!logoFile.isEmpty()) {
            String fileName = FileUploadUtil.saveFile("uploads/company-logos", logoFile.getOriginalFilename(), logoFile);
            company.setLogo(fileName);
        } else if (company.getId() != null) {
            // Preserve existing logo if no new file uploaded
            Company existing = companyService.getCompanyById(company.getId());
            if (existing != null && existing.getLogo() != null) {
                company.setLogo(existing.getLogo());
            }
        }

        companyService.saveCompany(company);
        return "redirect:/hrHome";
    }

    @GetMapping("/edit")
    public String editCompany(@RequestParam("id") Long id, Model model, HttpSession session) {
        if (!isAdmin(session) && !isHR(session)) return "LoginForm";
        Company company = companyService.getCompanyById(id);
        if (company == null) return "redirect:/company/list";
        model.addAttribute("company", company);
        return "company/company-form";
    }

    @GetMapping("/list")
    public String listCompanies(Model model, HttpSession session) {
        if (!isAdmin(session)) return "LoginForm";
        model.addAttribute("companies", companyService.getAllCompanies());
        return "company/company-list";
    }

    @GetMapping("/approve")
    public String approveCompany(@RequestParam("id") Long id, HttpSession session) {
        if (!isAdmin(session)) return "LoginForm";
        Company company = companyService.getCompanyById(id);
        if (company != null) {
            company.setStatus("approved");
            companyService.updateCompany(company);
        }
        return "redirect:/company/list";
    }

    @GetMapping("/reject")
    public String rejectCompany(@RequestParam("id") Long id, HttpSession session) {
        if (!isAdmin(session)) return "LoginForm";
        Company company = companyService.getCompanyById(id);
        if (company != null) {
            company.setStatus("rejected");
            companyService.updateCompany(company);
        }
        return "redirect:/company/list";
    }
}
