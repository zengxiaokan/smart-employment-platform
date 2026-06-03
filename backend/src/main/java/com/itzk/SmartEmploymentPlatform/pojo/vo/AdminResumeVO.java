package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;

@Data
public class AdminResumeVO {
    private Long id;
    private Long userId;
    private String username;
    private String resumeName;
    private String name;
    private String phone;
    private String email;
    private Integer age;
    private Integer gender;
    private String skills;
    private String selfDescription;
    private String jobIntention;
    private String city;
    private Integer salaryMin;
    private Integer salaryMax;
    private String characterAvatar;
    private Integer isDefault;
    private Integer jobType;
    private String industry;
    private String availableFrom;
    private String graduationSchool;
    private Integer totalWorkYears;
    private Integer maxEducation;
    private String createdAt;
    private String updatedAt;
}
