package com.cpt202.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_status_history")
public class ProjectStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "project_id")
    private Project project;

    @Column(name = "old_status")
    private String oldStatus;

    @Column(name = "new_status")
    private String newStatus;

    @ManyToOne
    @JoinColumn(name = "changed_by", referencedColumnName = "teacher_id")
    private TeacherProfile changedBy;

    @Column(name = "remark")
    private String remark;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    public ProjectStatusHistory() {
    }

    public ProjectStatusHistory(Project project, String oldStatus, String newStatus,
                                TeacherProfile changedBy, String remark, LocalDateTime changedAt) {
        this.project = project;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.remark = remark;
        this.changedAt = changedAt;
    }

    public Long getHistoryId() {
        return historyId;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public TeacherProfile getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(TeacherProfile changedBy) {
        this.changedBy = changedBy;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}