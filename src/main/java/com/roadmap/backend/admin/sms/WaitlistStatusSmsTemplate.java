package com.roadmap.backend.admin.sms;

/**
 * 관리자 대기열 상태 변경 시 발송할 SMS 템플릿.
 * status 값에 따라 각기 다른 메시지를 생성한다.
 */
public enum WaitlistStatusSmsTemplate {

    CONTACTED(
            "[로드맵 독서실]\n"
                    + "안녕하세요 로드맵 독서실입니다.\n"
                    + "%s 학생의 %s 자리가 발생하여 연락드렸습니다. \n"
                    + "희망하는 경우 3일 내로 회신 주시면 감사하겠습니다.\n"
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
