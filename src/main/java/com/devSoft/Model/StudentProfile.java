package com.devSoft.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "student_profile_tbl")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String phone;

    @Column(length = 500)
    private String address;

    @Column(length = 1000)
    private String education;

    @Column(length = 1000)
    private String skills;

    private String resumePath;

    private String profilePicture;

    private String certificateFile;

    private String extraDocFile;
}
