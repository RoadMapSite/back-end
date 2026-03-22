package com.roadmap.backend.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 학년 구분. 2학년, 3학년만 지원.
 */
public enum Grade {
    GRADE_2,
    GRADE_3;

    @JsonValue
    public String toApiValue() {
        return this == GRADE_2 ? "2학년" : "3학년";
    }

    @JsonCreator
    public static Grade fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String v = value.trim();
        if ("2학년".equals(v) || "GRADE_2".equalsIgnoreCase(v)) {
            return GRADE_2;
        }
        if ("3학년".equals(v) || "GRADE_3".equalsIgnoreCase(v)) {
            return GRADE_3;
        }
        throw new IllegalArgumentException("학년은 2학년 또는 3학년만 입력 가능합니다.");
    }
}
