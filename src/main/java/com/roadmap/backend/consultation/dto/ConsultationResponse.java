package com.roadmap.backend.consultation.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationResponse {

    private boolean success;
    private String message;
    private Long consultationId;
    private LocalDateTime registeredAt;
}
