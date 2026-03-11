package com.roadmap.backend.waitlist.entity;

/**
 * 대기열 시즌 구분.
 * <ul>
 *   <li>SUMMER, WINTER - branch 무관 (null로 저장)</li>
 *   <li>SEMESTER_1, SEMESTER_2 - branch 필수 (N 또는 Hi-end)</li>
 * </ul>
 */
public enum Season {
    SUMMER,
    WINTER,
    SEMESTER_1,
    SEMESTER_2
}
