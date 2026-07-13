package com.devSoft.ServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.devSoft.Service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("Email sending failed to " + to + ": " + e.getMessage());
        }
    }

    @Override
    public void sendWelcomeEmail(String to, String name) {
        sendEmail(to, "Welcome to CertChain - Certificate Verification System",
                "Dear " + name + ",\n\nWelcome to CertChain! Your account has been created successfully.\n\n"
                + "You can now:\n- Browse and apply for jobs\n- View your blockchain-verified certificates\n"
                + "- Track your applications\n\nBest regards,\nCertChain Team");
    }

    @Override
    public void sendApplicationReceived(String to, String studentName, String jobTitle, String companyName) {
        sendEmail(to, "Application Received - " + jobTitle,
                "Dear " + studentName + ",\n\nYour application for '" + jobTitle + "' at " + companyName
                + " has been received successfully.\n\nWe will notify you when your application status changes.\n\n"
                + "Best regards,\nCertChain Team");
    }

    @Override
    public void sendApplicationStatusChange(String to, String studentName, String jobTitle, String status) {
        String statusMsg = status.equalsIgnoreCase("accepted") ? "accepted. Congratulations!"
                : status.equalsIgnoreCase("rejected") ? "not selected at this time."
                : "updated to: " + status;
        sendEmail(to, "Application Status Update - " + jobTitle,
                "Dear " + studentName + ",\n\nYour application for '" + jobTitle + "' has been " + statusMsg + "\n\n"
                + "Best regards,\nCertChain Team");
    }

    @Override
    public void sendInterviewScheduled(String to, String studentName, String jobTitle, String companyName, String dateTime) {
        sendEmail(to, "Interview Scheduled - " + jobTitle,
                "Dear " + studentName + ",\n\nAn interview has been scheduled for your application for '" + jobTitle
                + "' at " + companyName + ".\n\nDate & Time: " + dateTime + "\n\n"
                + "Please check your dashboard for details.\n\nBest regards,\nCertChain Team");
    }

    @Override
    public void sendCompanyApproved(String to, String companyName) {
        sendEmail(to, "Company Registration Approved - " + companyName,
                "Dear HR,\n\nYour company '" + companyName + "' has been approved. You can now post jobs "
                + "and review applications.\n\nBest regards,\nCertChain Team");
    }
}
