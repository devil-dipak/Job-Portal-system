package com.devSoft.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "job_alerts_tbl")
public class JobAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String keyword;
    private String location;
    private String jobType;
    private String frequency;
    private boolean active = true;
    private LocalDateTime createdAt = LocalDateTime.now();
}
