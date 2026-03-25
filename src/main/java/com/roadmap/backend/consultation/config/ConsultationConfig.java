package com.roadmap.backend.consultation.config;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.springframework.context.annotation.Configuration;

/**
 * 상담 영업시간 및 예약 정책 설정.
 * <ul>
 *   <li>상담 가능 시간: 10:00 ~ 22:00 (마지막 예약 시작 21:30)</li>
 *   <li>예약 단위: 30분 간격</li>
 * </ul>
 */
@Configuration
@Getter
public class ConsultationConfig {

    private static final DateTimeFormatter SLOT_FMT = DateTimeFormatter.ofPattern("HH:mm");

    /** 영업 시작 시간 */
    public static final String OPERATING_START = "10:00";

    /** 영업 종료 시각 (해당 시각까지 상담 종료, 마지막 시작 슬롯은 30분 전) */
    public static final String OPERATING_END = "22:00";

    /** 예약 간격 (분) */
    public static final int INTERVAL_MINUTES = 30;

    /** 마지막 예약 가능 시작 시각 (OPERATING_END 직전 슬롯) */
    public static final String LAST_BOOKABLE_START;

    /** 유효한 예약 가능 시간 슬롯 (30분 단위, OPERATING_START ~ LAST_BOOKABLE_START) */
    public static final List<String> VALID_TIME_SLOTS;

    static {
        LocalTime end = LocalTime.parse(OPERATING_END);
        LocalTime lastStart = end.minusMinutes(INTERVAL_MINUTES);
        LAST_BOOKABLE_START = SLOT_FMT.format(lastStart);

        List<String> slots = new ArrayList<>();
        for (LocalTime t = LocalTime.parse(OPERATING_START); !t.isAfter(lastStart); t = t.plusMinutes(INTERVAL_MINUTES)) {
            slots.add(SLOT_FMT.format(t));
        }
        VALID_TIME_SLOTS = List.copyOf(slots);
    }
}
