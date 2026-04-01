package com.roadmap.backend.consultation.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 상담 신청 가능한 지점 구분.
 * <ul>
 *   <li>N - 일반 지점</li>
 *   <li>HI_END - Hi-end 지점</li>
 * </ul>
 * JSON 입출력은 {@link JsonProperty}로 {@code N} / {@code Hi-end}와 매핑한다.
 * ({@code @JsonCreator}와 {@code @JsonProperty}를 동시에 쓰면 Jackson이 역직렬화 경로를 꼬아 Hi-end만 실패할 수 있어 Creator는 쓰지 않는다.)
 */
public enum Branch {
    @JsonProperty("N")
    N,
    @JsonProperty("Hi-end")
    HI_END;

    /** API·표기용 문자열 (N, Hi-end) */
    public String toApiValue() {
        return this == HI_END ? "Hi-end" : name();
    }

    /**
     * 쿼리 파라미터 등 문자열에서 분기할 때 사용 (JSON은 {@link JsonProperty}로 처리).
     */
    public static Branch fromString(String value) {
        if (value == null || value.isBlank()) return null;
        String v = value.trim();
        if ("N".equalsIgnoreCase(v)) return N;
        if ("HI_END".equalsIgnoreCase(v) || "Hi-end".equalsIgnoreCase(v)) return HI_END;
        throw new IllegalArgumentException("지점은 N 또는 Hi-end만 입력 가능합니다.");
    }

    /**
     * Solapi 발신 번호: N수관 / 하이엔드관 환경 변수 값과 매핑한다.
     */
    public String resolveSolapiSenderNumber(String senderNumberForN, String senderNumberForHighend) {
        return switch (this) {
            case N -> senderNumberForN;
            case HI_END -> senderNumberForHighend;
        };
    }
}
