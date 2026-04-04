package com.cpt202.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_request")
public class ProjectRequest {
    public enum RequestStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        WITHDRAWN
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "student_id")
    private StudentProfile student;

    @ManyToOne
    @JoinColumn(name = "reviewed_by", referencedColumnName = "teacher_id")
    private TeacherProfile reviewedBy;

    @Column(name = "preference_rank")
    private int preferenceRank;

    @Column(name = "notes")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status")
    private RequestStatus requestStatus;

    @Column(name = "decision_comment")
    private String decisionComment;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ProjectRequest() {
    }

    public ProjectRequest(Project project, StudentProfile student, TeacherProfile reviewedBy,
                          int preferenceRank, String notes, RequestStatus requestStatus,
                          String decisionComment, LocalDateTime submittedAt,
                          LocalDateTime reviewedAt, LocalDateTime withdrawnAt,
                          LocalDateTime updatedAt) {
        this.project = project;
        this.student = student;
        this.reviewedBy = reviewedBy;
        this.preferenceRank = preferenceRank;
        this.notes = notes;
        this.requestStatus = requestStatus;
        this.decisionComment = decisionComment;
        this.submittedAt = submittedAt;
        this.reviewedAt = reviewedAt;
        this.withdrawnAt = withdrawnAt;
        this.updatedAt = updatedAt;
    }

    public Long getRequestId() {
        return requestId;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public StudentProfile getStudent() {
        return student;
    }

    public void setStudent(StudentProfile student) {
        this.student = student;
    }

    public TeacherProfile getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(TeacherProfile reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public int getPreferenceRank() {
        return preferenceRank;
    }

    public void setPreferenceRank(int preferenceRank) {
        this.preferenceRank = preferenceRank;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getDecisionComment() {
        return decisionComment;
    }

    public void setDecisionComment(String decisionComment) {
        this.decisionComment = decisionComment;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public LocalDateTime getWithdrawnAt() {
        return withdrawnAt;
    }

    public void setWithdrawnAt(LocalDateTime withdrawnAt) {
        this.withdrawnAt = withdrawnAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}