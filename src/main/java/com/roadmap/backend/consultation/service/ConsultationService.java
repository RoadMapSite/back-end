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
import com.roadmap.backend.consultation.exception.ConsultationException;
import com.roadmap.backend.consultation.repository.ConsultationRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultationService {

    private static final Pattern TIME_30MIN_PATTERN = Pattern.compile("^([01]?\\d|2[0-3]):(00|30)$");
    private static final Pattern YEAR_MONTH_PATTERN = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])$");

    private final ConsultationRepository consultationRepository;
    private final PhoneVerificationRepository phoneVerificationRepository;
    private final SmsService smsService;

    @Transactional
    public ConsultationResponse registerConsultation(ConsultationRequest request, String token) {
        // 로직 0: 상담 시간·날짜 검증 (30분 단위, 영업시간, 과거 시점 차단)
        validateConsultationDateTime(request.getDate(), request.getTime());

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

        // 로직 4: 저장
        LocalDateTime now = LocalDateTime.now();
        Consultation consultation = Consultation.builder()
                .branch(request.getBranch())
                .consultationDate(request.getDate())
                .consultationTime(request.getTime())
                .studentName(request.getName())
                .studentAge(request.getAge())
                .phoneNumber(request.getPhoneNumber())
                .registeredAt(now)
                .build();

        Consultation saved = consultationRepository.save(consultation);

        String phoneForSms = request.getPhoneNumber();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                smsService.send(phoneForSms, "test");
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
     * 상담 일시 유효성 검증.
     * 1) 30분 단위 검증 (분이 00 또는 30)
     * 2) 영업시간 검증 (10:00 ~ 17:30)
     * 3) 과거 날짜/시간 차단
     */
    private void validateConsultationDateTime(LocalDate date, String time) {
        if (time == null || time.isBlank()) {
            throw new ConsultationException("상담 시간은 필수이며, 30분 단위(예: 10:00, 10:30)만 입력 가능합니다.");
        }
        String trimmedTime = time.trim();
        if (!TIME_30MIN_PATTERN.matcher(trimmedTime).matches()) {
            throw new ConsultationException("상담 시간은 30분 단위(분이 00 또는 30)로만 입력 가능합니다. (예: 10:00, 17:30)");
        }
        if (!ConsultationConfig.VALID_TIME_SLOTS.contains(trimmedTime)) {
            throw new ConsultationException(
                    "상담 영업시간은 10:00 ~ 18:00입니다. 10:00 이전 또는 17:30 이후 시간은 예약할 수 없습니다.");
        }
        LocalDateTime requestedAt = date.atTime(
                Integer.parseInt(trimmedTime.substring(0, 2)),
                Integer.parseInt(trimmedTime.substring(3, 5)));
        if (requestedAt.isBefore(LocalDateTime.now())) {
            throw new ConsultationException("과거 날짜 또는 시간은 예약할 수 없습니다.");
        }
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
