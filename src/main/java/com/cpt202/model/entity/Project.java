package com.cpt202.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "project")
public class Project {
    public enum ProjectStatus {
        AVAILABLE,
        REQUESTED,
        AGREED,
        CLOSED,
        ARCHIVED
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long projectId;

    @ManyToOne
    @JoinColumn(name = "teacher_id", referencedColumnName = "teacher_id")
    private TeacherProfile teacher;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "category_id")
    private Category category;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "required_skills")
    private String requiredSkills;

    @Column(name = "topic_area")
    private String topicArea;

    @Column(name = "max_students")
    private int maxStudents;

    @Column(name = "current_agreed_count")
    private int currentAgreedCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_status")
    private ProjectStatus projectStatus;

    @Column(name = "publish_date")
    private LocalDateTime publishDate;

    @Column(name = "close_date")
    private LocalDateTime closeDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Project() {
    }

    public Project(TeacherProfile teacher, Category category, String title, String description,
                   String requiredSkills, String topicArea, int maxStudents, int currentAgreedCount,
                   ProjectStatus projectStatus, LocalDateTime publishDate, LocalDateTime closeDate,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.teacher = teacher;
        this.category = category;
        this.title = title;
        this.description = description;
        this.requiredSkills = requiredSkills;
        this.topicArea = topicArea;
        this.maxStudents = maxStudents;
        this.currentAgreedCount = currentAgreedCount;
        this.projectStatus = projectStatus;
        this.publishDate = publishDate;
        this.closeDate = closeDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getProjectId() {
        return projectId;
    }

    public TeacherProfile getTeacher() {
        return teacher;
    }

    public void setTeacher(TeacherProfile teacher) {
        this.teacher = teacher;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public String getTopicArea() {
        return topicArea;
    }

    public void setTopicArea(String topicArea) {
        this.topicArea = topicArea;
    }

    public int getMaxStudents() {
        return maxStudents;
    }

    public void setMaxStudents(int maxStudents) {
        this.maxStudents = maxStudents;
    }

    public int getCurrentAgreedCount() {
        return currentAgreedCount;
    }

    public void setCurrentAgreedCount(int currentAgreedCount) {
        this.currentAgreedCount = currentAgreedCount;
    }

    public ProjectStatus getProjectStatus() {
        return projectStatus;
    }

    public void setProjectStatus(ProjectStatus projectStatus) {
        this.projectStatus = projectStatus;
    }

    public LocalDateTime getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDateTime publishDate) {
        this.publishDate = publishDate;
    }

    public LocalDateTime getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(LocalDateTime closeDate) {
        this.closeDate = closeDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
