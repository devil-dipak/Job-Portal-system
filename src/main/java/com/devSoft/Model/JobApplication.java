package com.devSoft.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "job_application_tbl")
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long jobId;

    private Long studentId;

    @Column(length = 1000)
    private String coverNote;

    private String cvPath;

    private String extraDocPath;

    private String status;

    private LocalDateTime appliedAt;

    private String certificateIds;

    @Transient
    private String jobTitle;

    @Transient
    private String studentName;

    @Transient
    private String companyName;

    @Transient
    private List<Certificate> certificates;

    @Transient
    private int matchScore;
}
