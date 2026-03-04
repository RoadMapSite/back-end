package com.roadmap.backend.consultation.service;

import com.roadmap.backend.auth.entity.PhoneVerification;
import com.roadmap.backend.auth.repository.PhoneVerificationRepository;
import com.roadmap.backend.consultation.dto.ConsultationRequest;
import com.roadmap.backend.consultation.dto.ConsultationResponse;
import com.roadmap.backend.consultation.entity.Consultation;
import com.roadmap.backend.consultation.exception.ConsultationException;
import com.roadmap.backend.consultation.repository.ConsultationRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultationService {

    private static final Pattern TIME_30MIN_PATTERN = Pattern.compile("^([01]?\\d|2[0-3]):(00|30)$");

    private final ConsultationRepository consultationRepository;
    private final PhoneVerificationRepository phoneVerificationRepository;

    @Transactional
    public ConsultationResponse registerConsultation(ConsultationRequest request, String token) {
        // 로직 0: 상담 시간 검증 (30분 단위만 허용)
        validateConsultationTime(request.getTime());

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

        return ConsultationResponse.builder()
                .success(true)
                .message("상담 예약이 완료되었습니다.")
                .consultationId(saved.getConsultationId())
                .registeredAt(saved.getRegisteredAt())
                .build();
    }

    private void validateConsultationTime(String time) {
        if (time == null || time.isBlank()) {
            throw new ConsultationException("상담 시간은 필수이며, 30분 단위(예: 18:00, 18:30)만 입력 가능합니다.");
        }
        if (!TIME_30MIN_PATTERN.matcher(time.trim()).matches()) {
            throw new ConsultationException("상담 시간은 30분 단위(예: 09:00, 09:30, 18:00)로만 입력 가능합니다.");
        }
    }
}
