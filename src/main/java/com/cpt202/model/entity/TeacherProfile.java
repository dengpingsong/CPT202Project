package com.cpt202.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_profile")
public class TeacherProfile {
    @Id
    @Column(name = "teacher_id")
    private Long teacherId;
    @Column(name = "staff_no")
    private String staffNo;
    @Column(name = "department")
    private String department;
    @Column(name = "title")
    private String title;
    @Column(name = "research_area")
    private String researchArea;
    @Column(name = "office")
    private String office;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne
    @MapsId
    @JoinColumn(name = "teacher_id", referencedColumnName = "user_id")
    private User user;

    public TeacherProfile() {
    }

    public TeacherProfile(String staffNo, String department, String title, String researchArea, String office, LocalDateTime updatedAt, User user) {
        this.staffNo = staffNo;
        this.department = department;
        this.title = title;
        this.researchArea = researchArea;
        this.office = office;
        this.updatedAt = updatedAt;
        this.user = user;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public String getStaffNo() {
        return staffNo;
    }

    public void setStaffNo(String staffNo) {
        this.staffNo = staffNo;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getResearchArea() {
        return researchArea;
    }

    public void setResearchArea(String researchArea) {
        this.researchArea = researchArea;
    }

    public String getOffice() {
        return office;
    }

    public void setOffice(String office) {
        this.office = office;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
