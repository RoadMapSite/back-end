package com.roadmap.backend.consultation.entity;

import com.roadmap.backend.domain.Grade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "consultation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consultation_id")
    private Long consultationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Branch branch;

    @Column(name = "consultation_date", nullable = false)
    private LocalDate consultationDate;

    @Column(name = "consultation_time", length = 20)
    private String consultationTime;

    @Column(name = "student_name", nullable = false, length = 100)
    private String studentName;

    /** N수관만 값이 있고, Hi-end는 미수집이므로 null 허용 */
    @Column(name = "student_age", nullable = true)
    private Integer studentAge;

    @Column(name = "student_school", length = 100)
    private String studentSchool;

    @Enumerated(EnumType.STRING)
    @Column(name = "student_grade", length = 20)
    private Grade studentGrade;

    @Column(name = "phone_number", nullable = false, length = 50)
    private String phoneNumber;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;
}
