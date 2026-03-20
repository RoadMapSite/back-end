package com.roadmap.backend.sms.util;

import com.roadmap.backend.waitlist.entity.Season;

/**
 * SMS 메시지 템플릿용 공통 유틸리티.
 * 관 이름 변환 및 시즌+관 조합 로직을 분리하여 Service 클래스의 가독성을 유지한다.
 */
public final class SmsMessageUtil {

    private SmsMessageUtil() {
    }

    /**
     * branch 값을 SMS 표기용 관 이름으로 변환.
     * N -> N수관, Hi-end -> 하이엔드관
     */
    public static String formatBranchName(String branch) {
        if (branch == null || branch.isBlank()) {
            return "";
        }
        String normalized = branch.trim();
        if ("Hi-end".equalsIgnoreCase(normalized) || "HI_END".equalsIgnoreCase(normalized)) {
            return "하이엔드관";
        }
        if ("N".equalsIgnoreCase(normalized)) {
            return "N수관";
        }
        return normalized;
    }

    /**
     * Branch enum 값을 SMS 표기용 관 이름으로 변환.
     */
    public static String formatBranchName(com.roadmap.backend.consultation.entity.Branch branch) {
        if (branch == null) {
            return "";
        }
        return formatBranchName(branch.toApiValue());
    }

    /**
     * season과 branch를 조합하여 SMS 표기용 문자열 생성.
     * 예: "1학기 N수관", "2학기 하이엔드관", "여름캠프", "겨울캠프"
     * 캠프 시즌(SUMMER, WINTER)의 경우 branch 정보 없이 시즌 이름만 표기.
     */
    public static String formatSeasonAndBranch(String season, String branch) {
        if (season == null || season.isBlank()) {
            return "";
        }
        Season s;
        try {
            s = Season.valueOf(season);
        } catch (IllegalArgumentException e) {
            return season;
        }

        String seasonDisplay = switch (s) {
            case SEMESTER_1 -> "1학기";
            case SEMESTER_2 -> "2학기";
            case SUMMER -> "여름캠프";
            case WINTER -> "겨울캠프";
        };

        if (s == Season.SUMMER || s == Season.WINTER) {
            return seasonDisplay;
        }
        String branchDisplay = formatBranchName(branch);
        return branchDisplay.isEmpty() ? seasonDisplay : seasonDisplay + " " + branchDisplay;
    }
}
