package com.cpt202.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "student_profile")
@Getter
@Setter
@NoArgsConstructor
public class StudentProfile {
    /** Primary key shared with the related user account. */
    @Id
    @Setter(AccessLevel.NONE)
    @Column(name = "student_id")
    private Long studentId;

    /** Institutional student number. */
    @Column(name = "student_no")
    private String studentNo;

    /** Academic programme of the student. */
    @Column(name = "programme")
    private String programme;

    /** Current study year of the student. */
    @Column(name = "academic_year")
    private Integer academicYear;

    /** Contact phone number of the student. */
    @Column(name = "phone")
    private String phone;

    /** Personal interests or research preferences. */
    @Column(name = "interests")
    private String interests;

    /** Timestamp when the profile was last updated. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** User account linked to this student profile. */
    @OneToOne
    @MapsId
    @JoinColumn(name = "student_id", referencedColumnName = "user_id")
    private User user;

    @Builder
    public StudentProfile(String studentNo, String programme, Integer academicYear, String phone, String interests, LocalDateTime updatedAt, User user) {
        this.studentNo = studentNo;
        this.programme = programme;
        this.academicYear = academicYear;
        this.phone = phone;
        this.interests = interests;
        this.updatedAt = updatedAt;
        this.user = user;
    }
}
