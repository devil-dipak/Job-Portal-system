package com.devSoft.Service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
    void sendWelcomeEmail(String to, String name);
    void sendApplicationReceived(String to, String studentName, String jobTitle, String companyName);
    void sendApplicationStatusChange(String to, String studentName, String jobTitle, String status);
    void sendInterviewScheduled(String to, String studentName, String jobTitle, String companyName, String dateTime);
    void sendCompanyApproved(String to, String companyName);
}
