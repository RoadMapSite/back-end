package com.roadmap.backend.waitlist.entity;

import com.roadmap.backend.domain.Grade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "waitlist")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Waitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waitlist_id")
    private Long waitlistId;

    @Column(length = 50)
    private String branch;

    @Column(nullable = false, length = 50)
    private String season;

    @Column(name = "student_name", nullable = false, length = 100)
    private String studentName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Column(name = "student_age")
    private Integer studentAge;

    @Column(name = "student_school", length = 100)
    private String studentSchool;

    @Enumerated(EnumType.STRING)
    @Column(name = "student_grade", length = 20)
    private Grade studentGrade;

    @Column(name = "phone_number", nullable = false, length = 50)
    private String phoneNumber;

    @Column(name = "is_existing", nullable = false)
    @Builder.Default
    private boolean isExisting = false;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "WAITING";

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void updateStatus(String status, LocalDateTime updatedAt) {
        this.status = status;
        this.updatedAt = updatedAt;
    }
}
