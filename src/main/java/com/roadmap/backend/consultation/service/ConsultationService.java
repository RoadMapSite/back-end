package com.roadmap.backend.consultation.service;

import com.roadmap.backend.auth.entity.PhoneVerification;
import com.roadmap.backend.auth.repository.PhoneVerificationRepository;
import com.roadmap.backend.consultation.config.ConsultationConfig;
import com.roadmap.backend.consultation.dto.ConsultationRequest;
import com.roadmap.backend.consultation.dto.ConsultationResponse;
import com.roadmap.backend.consultation.dto.OperatingHours;
import com.roadmap.backend.consultation.dto.ScheduleResponse;
import com.roadmap.backend.consultation.dto.UnavailableScheduleItem;
import com.roadmap.backend.consultation.entity.Branch;
import com.roadmap.backend.consultation.entity.Consultation;
import com.roadmap.backend.sms.service.SmsService;
import com.roadmap.backend.sms.util.SmsMessageUtil;
import com.roadmap.backend.consultation.exception.ConsultationException;
import com.roadmap.backend.consultation.repository.ConsultationRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultationService {

    private static final Pattern YEAR_MONTH_PATTERN = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])$");
    private static final DateTimeFormatter CONSULTATION_TIME_INPUT = DateTimeFormatter.ofPattern("H:mm");

    private final ConsultationRepository consultationRepository;
    private final PhoneVerificationRepository phoneVerificationRepository;
    private final SmsService smsService;

    @Value("${solapi.sender.number.n}")
    private String solapiSenderNumberN;

    @Value("${solapi.sender.number.highend}")
    private String solapiSenderNumberHighend;

    @Transactional
    public ConsultationResponse registerConsultation(ConsultationRequest request, String token) {
        requireConsultationBranchEnum(request);
        validateConsultationAppointmentRules(request);

        if (request.getBranch() == Branch.HI_END) {
            log.info(
                    "Hi-end 상담 신청 처리 시작: date={}, time={}, name={}, school={}, grade={}, phone={}",
                    request.getDate(),
                    request.getTime(),
                    request.getName(),
                    request.getSchool(),
                    request.getGrade(),
                    request.getPhoneNumber());
        } else if (request.getBranch() == Branch.N) {
            log.info(
                    "N수관 상담 신청 처리 시작: date={}, time={}, name={}, age={}, phone={}",
                    request.getDate(),
                    request.getTime(),
                    request.getName(),
                    request.getAge(),
                    request.getPhoneNumber());
        }

        // 로직 0-1: branch별 나이 vs 학교·학년 검증 (N: 나이 필수, Hi-end: 학교·학년 필수)
        validateConsultationAgeOrSchoolGrade(request);

        // 로직 1: 토큰 검증
        Optional<PhoneVerification> verificationOpt = phoneVerificationRepository
                .findFirstByVerificationTokenAndIsVerifiedTrue(token);

        if (verificationOpt.isEmpty()) {
            throw new ConsultationException("유효하지 않거나 만료된 인증 토큰입니다. 휴대폰 인증을 다시 진행해주세요.");
        }

        PhoneVerification verification = verificationOpt.get();

        if (LocalDateTime.now().isAfter(verification.getExpiresAt())) {
            throw new ConsultationException("인증 토큰이 만료되었습니다. 휴대폰 인증을 다시 진행해주세요.");
        }

        // 로직 2: 번호 일치 확인
        if (!verification.getPhoneNumber().equals(request.getPhoneNumber())) {
            throw new ConsultationException("휴대폰 번호가 인증한 번호와 일치하지 않습니다.");
        }

        // 로직 2-1: 하루 1회 제한 (동일 번호가 같은 날 이미 상담 신청한 경우)
        String phoneNumber = verification.getPhoneNumber();
        if (consultationRepository.existsByPhoneNumberAndConsultationDate(phoneNumber, request.getDate())) {
            throw new ConsultationException("하루에 상담 신청은 한 번만 가능합니다.");
        }

        // 로직 3: 중복 예약 체크 (동일 지점·날짜·시간)
        boolean exists = consultationRepository.existsByBranchAndConsultationDateAndConsultationTime(
                request.getBranch(),
                request.getDate(),
                request.getTime()
        );

        if (exists) {
            throw new ConsultationException("해당 시간은 이미 예약이 완료되었습니다.");
        }

        validateSolapiSenderConfiguredForBranch(request.getBranch());

        // 로직 4: 저장
        LocalDateTime now = LocalDateTime.now();
        Consultation.ConsultationBuilder builder = Consultation.builder()
                .branch(request.getBranch())
                .consultationDate(request.getDate())
                .consultationTime(request.getTime())
                .studentName(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .registeredAt(now);

        if (request.getBranch() == Branch.N) {
            builder.studentAge(request.getAge()).studentSchool(null).studentGrade(null);
        } else if (request.getBranch() == Branch.HI_END) {
            builder
                    .studentAge(null)
                    .studentSchool(request.getSchool())
                    .studentGrade(request.getGrade());
        } else {
            log.error("상담 저장 분기: 지원하지 않는 branch입니다. branch={}", request.getBranch());
            throw new ConsultationException("지점은 N 또는 Hi-end만 입력 가능합니다.");
        }

        Consultation consultation = builder.build();

        Consultation saved = consultationRepository.save(consultation);

        if (request.getBranch() == Branch.HI_END) {
            log.info("Hi-end 상담 신청 DB 저장 완료: consultationId={}", saved.getConsultationId());
        }

        String phoneForSms = request.getPhoneNumber();
        String smsMessage = buildConsultationSmsMessage(request);
        String senderFrom =
                request.getBranch().resolveSolapiSenderNumber(solapiSenderNumberN, solapiSenderNumberHighend);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    smsService.send(phoneForSms, smsMessage, senderFrom);
                } catch (RuntimeException e) {
                    log.error(
                            "상담 신청: 트랜잭션 커밋 후 SMS 발송 실패 (DB 저장은 이미 반영됨). consultationId={}",
                            saved.getConsultationId(),
                            e);
                    throw e;
                }
            }
        });

        return ConsultationResponse.builder()
                .success(true)
                .message("상담 예약이 완료되었습니다.")
                .consultationId(saved.getConsultationId())
                .registeredAt(saved.getRegisteredAt())
                .build();
    }

    /**
     * 상담 신청 완료 SMS 메시지 생성.
     */
    private String buildConsultationSmsMessage(ConsultationRequest request) {
        String branchDisplay = SmsMessageUtil.formatBranchName(request.getBranch());
        String dateStr = request.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return String.format(
                "[로드맵 독서실]\n"
                        + "%s 학생의 상담 신청이 완료되었습니다.\n"
                        + "예약 일정: %s %s\n"
                        + "위치: 로드맵 %s\n"
                        + "감사합니다.",
                request.getName(),
                dateStr,
                request.getTime(),
                branchDisplay
        );
    }

    /**
     * 요청의 branch가 Enum으로 확정된 N / HI_END 인지 검사한다. (문자열 비교 금지)
     */
    private void requireConsultationBranchEnum(ConsultationRequest request) {
        Branch b = request.getBranch();
        if (b == null) {
            log.error("상담 신청: branch가 null입니다.");
            throw new ConsultationException("지점(branch)은 필수입니다. N 또는 Hi-end를 입력해 주세요.");
        }
        if (b != Branch.N && b != Branch.HI_END) {
            log.error("상담 신청: 알 수 없는 branch Enum 값입니다. branch={}", b);
            throw new ConsultationException("지점은 N 또는 Hi-end만 입력 가능합니다.");
        }
    }

    /** 해당 지점 SMS 발신번호가 비어 있으면 커밋 전에 실패시킨다 (사일런트 스킵 방지). */
    private void validateSolapiSenderConfiguredForBranch(Branch branch) {
        if (branch == Branch.N && !StringUtils.hasText(solapiSenderNumberN)) {
            log.error("상담 신청: N수관 Solapi 발신번호(solapi.sender.number.n)가 비어 있습니다.");
            throw new ConsultationException("시스템 설정 오류로 문자를 보낼 수 없습니다. 관리자에게 문의해 주세요.");
        }
        if (branch == Branch.HI_END && !StringUtils.hasText(solapiSenderNumberHighend)) {
            log.error("상담 신청: Hi-end Solapi 발신번호(solapi.sender.number.highend)가 비어 있습니다.");
            throw new ConsultationException("시스템 설정 오류로 문자를 보낼 수 없습니다. 관리자에게 문의해 주세요.");
        }
    }

    /**
     * N: 나이 필수, Hi-end: 학교·학년 필수 검증.
     */
    private void validateConsultationAgeOrSchoolGrade(ConsultationRequest request) {
        if (request.getBranch() == Branch.N) {
            if (request.getAge() == null) {
                throw new ConsultationException("N수관은 나이를 필수로 입력해주세요.");
            }
            if ((request.getSchool() != null && !request.getSchool().isBlank()) || request.getGrade() != null) {
                throw new ConsultationException("N수관은 나이만 입력 가능합니다.");
            }
        } else if (request.getBranch() == Branch.HI_END) {
            boolean hasSchoolGrade = (request.getSchool() != null && !request.getSchool().isBlank())
                    && request.getGrade() != null;
            if (!hasSchoolGrade) {
                throw new ConsultationException("Hi-end는 학교와 학년을 필수로 입력해주세요.");
            }
            if (request.getAge() != null) {
                throw new ConsultationException("Hi-end는 학교·학년만 입력 가능합니다.");
            }
        } else {
            log.error("상담 검증 분기: 지원하지 않는 branch입니다. branch={}", request.getBranch());
            throw new ConsultationException("지점은 N 또는 Hi-end만 입력 가능합니다.");
        }
    }

    /**
     * 예약 일시 강제 검증 (프론트 우회 방지).
     * <ul>
     *   <li>예약일: 오늘 기준 최소 이틀 뒤({@code LocalDate.now().plusDays(2)})부터</li>
     *   <li>일요일 불가</li>
     *   <li>지점·요일별 허용 시각만 (N: 평·토 10:00·17:00 / Hi-end: 평 17:00·20:00, 토 10:00·16:00)</li>
     * </ul>
     */
    private void validateConsultationAppointmentRules(ConsultationRequest request) {
        LocalDate date = request.getDate();
        if (date == null) {
            throw new ConsultationException("상담 날짜는 필수입니다.");
        }

        LocalDate today = LocalDate.now();
        LocalDate earliestBookable = today.plusDays(2);
        if (date.isBefore(earliestBookable)) {
            throw new ConsultationException("상담 신청은 오늘 기준으로 이틀 뒤 날짜부터 가능합니다.");
        }

        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SUNDAY) {
            throw new ConsultationException("일요일은 상담을 운영하지 않습니다.");
        }

        String timeRaw = request.getTime();
        if (timeRaw == null || timeRaw.isBlank()) {
            throw new ConsultationException("상담 시간은 필수입니다.");
        }

        LocalTime requestedTime;
        try {
            requestedTime = LocalTime.parse(timeRaw.trim(), CONSULTATION_TIME_INPUT).withSecond(0).withNano(0);
        } catch (DateTimeParseException e) {
            throw new ConsultationException("상담 시간 형식이 올바르지 않습니다. (예: 10:00, 17:00)");
        }

        Branch branch = request.getBranch();
        Set<LocalTime> allowed = resolveConsultationAllowedTimes(branch, dow);
        if (!allowed.contains(requestedTime)) {
            throw new ConsultationException("선택하신 시간은 상담 가능 시간이 아닙니다.");
        }
    }

    /** N수관: 평일·토 10:00, 17:00. 하이엔드: 평일 17:00·20:00, 토 10:00·16:00. (일요일은 호출 전에 차단) */
    private Set<LocalTime> resolveConsultationAllowedTimes(Branch branch, DayOfWeek dow) {
        boolean weekday =
                dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
        boolean saturday = dow == DayOfWeek.SATURDAY;

        if (branch == Branch.N) {
            if (weekday || saturday) {
                return Set.of(LocalTime.of(10, 0), LocalTime.of(17, 0));
            }
            return Set.of();
        }
        if (branch == Branch.HI_END) {
            if (weekday) {
                return Set.of(LocalTime.of(17, 0), LocalTime.of(20, 0));
            }
            if (saturday) {
                return Set.of(LocalTime.of(10, 0), LocalTime.of(16, 0));
            }
            return Set.of();
        }
        return Set.of();
    }

    /**
     * 월별 상담 스케줄 조회 - 예약 불가능한(이미 예약된) 시간만 반환.
     * 예약이 없는 날짜는 unavailableSchedules 배열에 포함하지 않음.
     */
    public ScheduleResponse getUnavailableSchedules(String branchParam, String yearMonth) {
        Branch branch = parseBranch(branchParam);
        YearMonth ym = parseYearMonth(yearMonth);

        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<Consultation> consultations = consultationRepository.findByBranchAndConsultationDateBetween(
                branch, startDate, endDate);

        Map<LocalDate, List<String>> bookedByDate = consultations.stream()
                .collect(Collectors.groupingBy(
                        Consultation::getConsultationDate,
                        Collectors.mapping(Consultation::getConsultationTime, Collectors.toList())
                ));

        List<UnavailableScheduleItem> unavailableSchedules = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<String> bookedTimes = bookedByDate.getOrDefault(date, List.of())
                    .stream()
                    .filter(ConsultationConfig.VALID_TIME_SLOTS::contains)
                    .sorted()
                    .toList();

            if (!bookedTimes.isEmpty()) {
                unavailableSchedules.add(UnavailableScheduleItem.builder()
                        .date(date.format(dateFormatter))
                        .bookedTimes(bookedTimes)
                        .build());
            }
        }

        return ScheduleResponse.builder()
                .branch(branch.toApiValue())
                .yearMonth(yearMonth)
                .operatingHours(OperatingHours.createDefault())
                .unavailableSchedules(unavailableSchedules)
                .build();
    }

    private Branch parseBranch(String branchParam) {
        if (branchParam == null || branchParam.isBlank()) {
            throw new ConsultationException("지점(branch)은 필수입니다. N 또는 Hi-end를 입력해주세요.");
        }
        String normalized = branchParam.trim();
        if ("N".equalsIgnoreCase(normalized)) {
            return Branch.N;
        }
        if ("HI_END".equalsIgnoreCase(normalized) || "Hi-end".equalsIgnoreCase(normalized)) {
            return Branch.HI_END;
        }
        throw new ConsultationException("지점은 N 또는 Hi-end만 입력 가능합니다.");
    }

    private YearMonth parseYearMonth(String yearMonth) {
        if (yearMonth == null || yearMonth.isBlank()) {
            throw new ConsultationException("연월(yearMonth)은 필수입니다. (예: 2026-02)");
        }
        String trimmed = yearMonth.trim();
        if (!YEAR_MONTH_PATTERN.matcher(trimmed).matches()) {
            throw new ConsultationException("연월 형식이 올바르지 않습니다. yyyy-MM 형식으로 입력해주세요. (예: 2026-02)");
        }
        try {
            return YearMonth.parse(trimmed);
        } catch (DateTimeParseException e) {
            throw new ConsultationException("연월 형식이 올바르지 않습니다. (예: 2026-02)");
        }
    }
}
