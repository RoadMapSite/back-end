package com.roadmap.backend.waitlist.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(name = "student_age", nullable = false)
    private Integer studentAge;

    @Column(name = "phone_number", nullable = false, length = 50)
    private String phoneNumber;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "WAITING";

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
