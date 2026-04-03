package com.roadmap.backend.admin.sms;

/**
 * 관리자 대기열 상태 변경 시 발송할 SMS 템플릿.
 * status 값에 따라 각기 다른 메시지를 생성한다.
 */
public enum WaitlistStatusSmsTemplate {

    CONTACTED(
            "%s 학생의 %s 등록을 예약해 주셔서 감사합니다.\n"
                    + "현재 이용 가능한 좌석이 발생하여 안내드립니다.\n\n"
                    + "등록을 원하시는 경우 당일 내 회신 부탁드립니다.\n"
                    + "(회신이 지연될 경우 원하시는 기간 이용이 어려울 수 있습니다.)\n\n"
                    + "장시간 회신이 없을 경우는 등록 의사가 없으신 것으로 판단하여 예약이 자동 취소될 수 있는 점 양해 부탁드립니다.\n\n"
                    + "감사합니다."
    ),
    REGISTERED(
            "[로드맵 독서실]\n"
                    + "%s 학생의 %s 등록이 완료되었습니다."
    ),
    CANCELED(
            "[로드맵 독서실]\n"
                    + "%s 학생의 %s 등록 대기가 취소되었습니다."
    );

    private final String template;

    WaitlistStatusSmsTemplate(String template) {
        this.template = template;
    }

    /**
     * 학생 이름과 시즌+관 정보로 메시지 생성.
     *
     * @param studentName   학생 이름
     * @param seasonAndBranch 시즌 및 관 표기 (예: "1학기 N수관", "겨울캠프")
     * @return 포맷된 SMS 메시지
     */
    public String format(String studentName, String seasonAndBranch) {
        return String.format(template, studentName, seasonAndBranch);
    }

    /**
     * status 문자열로 해당 템플릿 조회. WAITING은 null 반환 (SMS 미발송).
     */
    public static WaitlistStatusSmsTemplate fromStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return switch (status.toUpperCase()) {
            case "CONTACTED" -> CONTACTED;
            case "REGISTERED" -> REGISTERED;
            case "CANCELED" -> CANCELED;
            default -> null;
        };
    }
}
