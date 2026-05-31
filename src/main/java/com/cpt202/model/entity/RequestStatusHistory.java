package com.cpt202.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "request_status_history")
@Getter
@Setter
@NoArgsConstructor
public class RequestStatusHistory {
    public enum HistoryActorType {
        STUDENT,
        TEACHER,
        SYSTEM
    }

    /** Primary key of the request status history record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "history_id")
    private Long historyId;

    /** Request whose status changed. */
    @ManyToOne
    @JoinColumn(name = "request_id", referencedColumnName = "request_id")
    private ProjectRequest request;

    /** Previous request status before the change. */
    @Column(name = "old_status")
    private String oldStatus;

    /** New request status after the change. */
    @Column(name = "new_status")
    private String newStatus;

    /** Student who triggered the recorded change. */
    @ManyToOne
    @JoinColumn(name = "changed_by", referencedColumnName = "student_id")
    private StudentProfile changedBy;

    /** Actor type responsible for the status change. */
    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type")
    private HistoryActorType actorType;

    /** Teacher who triggered the change, when the change is teacher-driven. */
    @ManyToOne
    @JoinColumn(name = "changed_by_teacher", referencedColumnName = "teacher_id")
    private TeacherProfile changedByTeacher;

    /** Optional remark explaining the status update. */
    @Column(name = "remark")
    private String remark;

    /** Time when the status change was recorded. */
    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @Builder
    public RequestStatusHistory(ProjectRequest request, String oldStatus, String newStatus,
                                StudentProfile changedBy, HistoryActorType actorType,
                                TeacherProfile changedByTeacher, String remark, LocalDateTime changedAt) {
        this.request = request;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.actorType = actorType;
        this.changedByTeacher = changedByTeacher;
        this.remark = remark;
        this.changedAt = changedAt;
    }
}
