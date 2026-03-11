package com.roadmap.backend.consultation.config;

import java.util.List;
import lombok.Getter;
import org.springframework.context.annotation.Configuration;

/**
 * 상담 영업시간 및 예약 정책 설정.
 * <ul>
 *   <li>상담 가능 시간: 10:00 ~ 18:00</li>
 *   <li>마지막 예약 가능 타임: 17:30</li>
 *   <li>예약 단위: 30분 간격</li>
 * </ul>
 */
@Configuration
@Getter
public class ConsultationConfig {

    /** 영업 시작 시간 */
    public static final String OPERATING_START = "10:00";

    /** 영업 종료 시간 */
    public static final String OPERATING_END = "18:00";

    /** 예약 간격 (분) */
    public static final int INTERVAL_MINUTES = 30;

    /** 유효한 예약 가능 시간 슬롯 (10:00 ~ 17:30, 30분 단위) */
    public static final List<String> VALID_TIME_SLOTS = List.of(
            "10:00", "10:30", "11:00", "11:30", "12:00", "12:30",
            "13:00", "13:30", "14:00", "14:30", "15:00", "15:30",
            "16:00", "16:30", "17:00", "17:30"
    );
}
