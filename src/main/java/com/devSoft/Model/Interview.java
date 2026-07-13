package com.devSoft.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "interview_tbl")
public class Interview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long applicationId;
    private Long jobId;
    private Long studentId;
    private Long hrId;
    private String scheduledAt;
    private String meetingLink;
    private String notes;
    private String status;
    private LocalDateTime createdAt = LocalDateTime.now();

    @Transient
    private String jobTitle;
    @Transient
    private String studentName;
    @Transient
    private String companyName;
}
