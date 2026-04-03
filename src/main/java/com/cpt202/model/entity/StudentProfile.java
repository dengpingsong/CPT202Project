package com.cpt202.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_profile")
public class StudentProfile {
    @Id
    @Column(name = "student_id")
    private Long studentId;
    @Column(name = "student_no")
    private String studentNo;
    @Column(name = "programme")
    private String programme;
    @Column(name = "year")
    private int year;
    @Column(name = "phone")
    private String phone;
    @Column(name = "interests")
    private String interests;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne
    @MapsId
    @JoinColumn(name = "student_id", referencedColumnName = "user_id")
    private User user;

    public StudentProfile() {
    }

    public StudentProfile(String studentNo, String programme, int year, String phone, String interests, LocalDateTime updatedAt, User user) {
        this.studentNo = studentNo;
        this.programme = programme;
        this.year = year;
        this.phone = phone;
        this.interests = interests;
        this.updatedAt = updatedAt;
        this.user = user;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getProgramme() {
        return programme;
    }

    public void setProgramme(String programme) {
        this.programme = programme;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getInterests() {
        return interests;
    }

    public void setInterests(String interests) {
        this.interests = interests;
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
