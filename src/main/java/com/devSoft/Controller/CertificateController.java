package com.devSoft.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.devSoft.Model.Certificate;
import com.devSoft.Model.User;
import com.devSoft.Service.CertificateService;
import com.devSoft.Service.DepartmentService;
import com.devSoft.Utils.CertificatePdfView;
import com.devSoft.Utils.QRCodeGenerator;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/certificate")
public class CertificateController {

	@Autowired
	private CertificateService certService;

	@Autowired
	private DepartmentService deptService;

	private boolean isAdmin(HttpSession session) {
		User user = (User) session.getAttribute("activeuser");
		return user != null && !user.getRole().equalsIgnoreCase("student") && !user.getRole().equalsIgnoreCase("HR");
	}

	private boolean isLoggedIn(HttpSession session) {
		return session.getAttribute("activeuser") != null;
	}

	@GetMapping("/add")
	public String getCert(Model model, HttpSession session) {
		if (!isAdmin(session)) return "LoginForm";
		model.addAttribute("departments", deptService.getAllDepts());
		return "CertificateForm";
	}

	@PostMapping("/add")
	public String postCert(@ModelAttribute Certificate cert, Model model, HttpSession session) {
		if (!isAdmin(session)) return "LoginForm";
		certService.addCert(cert);
		model.addAttribute("success", "Certificate issued successfully!");
		model.addAttribute("hash", cert.getCertificateHash());
		model.addAttribute("certId", cert.getId());
		model.addAttribute("departments", deptService.getAllDepts());
		return "CertificateForm";
	}

	@GetMapping("/list")
	public String getCertList(Model model, HttpSession session) {
		if (!isAdmin(session)) return "LoginForm";
		model.addAttribute("certList", certService.getAllCerts());
		return "CertificateListForm";
	}

	@GetMapping("/my")
	public String myCertificates(Model model, HttpSession session) {
		User user = (User) session.getAttribute("activeuser");
		if (user == null) return "LoginForm";
		model.addAttribute("user", user);
		model.addAttribute("certList", certService.getCertsByStudent(user.getFname() + " " + user.getLname()));
		if ("student".equalsIgnoreCase(user.getRole())) {
			return "StudentCertificateList";
		}
		return "CertificateListForm";
	}

	@GetMapping("/delete")
	public String deleteCert(@RequestParam("id") Long id, HttpSession session) {
		if (!isAdmin(session)) return "LoginForm";
		certService.deleteCert(id);
		return "redirect:list";
	}

	@GetMapping("/edit")
	public String edit(@RequestParam("id") Long id, Model model, HttpSession session) {
		if (!isAdmin(session)) return "LoginForm";
		model.addAttribute("certObject", certService.getCertById(id));
		model.addAttribute("departments", deptService.getAllDepts());
		return "EditCertificateForm";
	}

	@PostMapping("/update")
	public String update(@ModelAttribute Certificate cert, HttpSession session) {
		if (!isAdmin(session)) return "LoginForm";
		certService.updateCert(cert);
		return "redirect:list";
	}

	@GetMapping("/pdf")
	public ModelAndView pdf(HttpSession session) {
		if (!isAdmin(session)) return null;
		ModelAndView mv = new ModelAndView();
		mv.addObject("cList", certService.getAllCerts());
		mv.setView(new CertificatePdfView());
		return mv;
	}

	@GetMapping("/pdf/{id}")
	public ModelAndView pdfSingle(@PathVariable Long id, HttpSession session) {
		if (!isLoggedIn(session)) return null;
		ModelAndView mv = new ModelAndView();
		mv.addObject("cList", java.util.List.of(certService.getCertById(id)));
		mv.setView(new CertificatePdfView());
		return mv;
	}

	@GetMapping(path = "/qr/{id}", produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<byte[]> getQr(@PathVariable Long id, HttpSession session) {
		if (!isLoggedIn(session)) return ResponseEntity.status(401).build();
		try {
			Certificate cert = certService.getCertById(id);
			if (cert == null) return ResponseEntity.notFound().build();
			String data = "CERT-ID:" + cert.getId() + "|HASH:" + cert.getCertificateHash();
			byte[] qr = QRCodeGenerator.generateQRCode(data);
			return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qr);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().build();
		}
	}

}
