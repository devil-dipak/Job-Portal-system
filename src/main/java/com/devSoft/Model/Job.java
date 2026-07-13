package com.devSoft.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "job_tbl")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long companyId;

    @NotBlank
    private String title;

    @NotBlank
    @Column(length = 500)
    private String description;

    private String requirements;

    private String salary;

    private String location;

    private String type;

    private String status;

    private LocalDate createdAt;

    private LocalDate deadline;

    private String contactEmail;

    @Transient
    private String companyName;
}
