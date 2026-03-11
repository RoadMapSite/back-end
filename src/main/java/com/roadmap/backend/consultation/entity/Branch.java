package com.roadmap.backend.consultation.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 상담 신청 가능한 지점 구분.
 * <ul>
 *   <li>N - 일반 지점</li>
 *   <li>HI_END - Hi-end 지점</li>
 * </ul>
 */
public enum Branch {
    N,
    HI_END;

    /** API 입출력용 표기 (N, Hi-end) */
    @JsonValue
    public String toApiValue() {
        return this == HI_END ? "Hi-end" : name();
    }

    @JsonCreator
    public static Branch fromString(String value) {
        if (value == null || value.isBlank()) return null;
        String v = value.trim();
        if ("N".equalsIgnoreCase(v)) return N;
        if ("HI_END".equalsIgnoreCase(v) || "Hi-end".equalsIgnoreCase(v)) return HI_END;
        throw new IllegalArgumentException("지점은 N 또는 Hi-end만 입력 가능합니다.");
    }
}
