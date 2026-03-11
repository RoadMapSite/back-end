package com.roadmap.backend.waitlist.service;

import com.roadmap.backend.auth.entity.PhoneVerification;
import com.roadmap.backend.auth.repository.PhoneVerificationRepository;
import com.roadmap.backend.waitlist.dto.WaitlistRegisterRequest;
import com.roadmap.backend.waitlist.dto.WaitlistRegisterResponse;
import com.roadmap.backend.waitlist.entity.Season;
import com.roadmap.backend.waitlist.entity.Waitlist;
import com.roadmap.backend.waitlist.exception.WaitlistException;
import com.roadmap.backend.waitlist.repository.WaitlistRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final PhoneVerificationRepository phoneVerificationRepository;

    @Transactional
    public WaitlistRegisterResponse registerWaitlist(WaitlistRegisterRequest request, String token) {
        // 토큰 검증 및 전화번호 일치 확인 (상담 신청 API와 동일한 방식)
        PhoneVerification verification = validateTokenAndPhoneNumber(token, request.getPhoneNumber());

        // 1인당 1시즌 1회 등록 중복 방지 (동일 phoneNumber + 동일 season → branch 무관하게 차단)
        String phoneNumber = verification.getPhoneNumber();
        if (waitlistRepository.existsByPhoneNumberAndSeason(phoneNumber, request.getSeason())) {
            throw new WaitlistException("이미 해당 시즌에 대기 등록이 완료된 연락처입니다.", HttpStatus.CONFLICT);
        }

        // 시즌별 branch 분기 로직
        String branchToSave = resolveBranch(request.getSeason(), request.getBranch());

        // DB 저장
        LocalDateTime now = LocalDateTime.now();
        Waitlist waitlist = Waitlist.builder()
                .branch(branchToSave)
                .season(request.getSeason())
                .studentName(request.getName())
                .studentAge(request.getAge())
                .phoneNumber(phoneNumber)
                .status("WAITING")
                .registeredAt(now)
                .updatedAt(now)
                .build();

        Waitlist saved = waitlistRepository.save(waitlist);

        // 대기 등록 완료 SMS 발송
        sendWaitlistCompletionSms(saved);

        return WaitlistRegisterResponse.builder()
                .success(true)
                .message("대기 등록이 완료되었습니다.")
                .waitlistId(saved.getWaitlistId())
                .registeredAt(saved.getRegisteredAt())
                .build();
    }

    /**
     * 토큰 검증 및 휴대폰 번호 일치 확인.
     * 상담 신청 API와 동일: PhoneVerification 기반 verificationToken 검증.
     * 토큰 위조/만료 또는 번호 불일치 시 401 Unauthorized 반환.
     */
    private PhoneVerification validateTokenAndPhoneNumber(String token, String requestPhoneNumber) {
        Optional<PhoneVerification> verificationOpt = phoneVerificationRepository
                .findFirstByVerificationTokenAndIsVerifiedTrue(token);

        if (verificationOpt.isEmpty()) {
            throw new WaitlistException("유효하지 않거나 만료된 인증 토큰입니다. 휴대폰 인증을 다시 진행해주세요.", HttpStatus.UNAUTHORIZED);
        }

        PhoneVerification verification = verificationOpt.get();

        if (LocalDateTime.now().isAfter(verification.getExpiresAt())) {
            throw new WaitlistException("인증 토큰이 만료되었습니다. 휴대폰 인증을 다시 진행해주세요.", HttpStatus.UNAUTHORIZED);
        }

        if (!verification.getPhoneNumber().equals(requestPhoneNumber)) {
            throw new WaitlistException("휴대폰 번호가 인증한 번호와 일치하지 않습니다.", HttpStatus.FORBIDDEN);
        }

        return verification;
    }

    /**
     * 시즌별 branch 분기:
     * - SUMMER, WINTER: branch 무시, null 반환
     * - SEMESTER_1, SEMESTER_2: branch 필수 검증 (N 또는 Hi-end)
     */
    private String resolveBranch(String season, String branch) {
        if (season == null || season.isBlank()) {
            throw new WaitlistException("시즌은 필수입니다.");
        }

        Season s = Season.valueOf(season);

        if (s == Season.SUMMER || s == Season.WINTER) {
            return null;
        }

        if (s == Season.SEMESTER_1 || s == Season.SEMESTER_2) {
            if (branch == null || branch.isBlank()) {
                throw new WaitlistException("SEMESTER_1, SEMESTER_2 시즌은 지점(branch)이 필수입니다. N 또는 Hi-end를 입력해주세요.");
            }
            String normalized = branch.trim();
            if ("N".equalsIgnoreCase(normalized) || "Hi-end".equalsIgnoreCase(normalized) || "HI_END".equalsIgnoreCase(normalized)) {
                return "Hi-end".equalsIgnoreCase(normalized) ? "Hi-end" : "N";
            }
            throw new WaitlistException("지점(branch)은 N 또는 Hi-end만 입력 가능합니다.");
        }

        return null;
    }

    /**
     * 대기 등록 완료 SMS 발송.
     * 실제 SMS API 연동은 TODO로 남겨둠.
     */
    private void sendWaitlistCompletionSms(Waitlist waitlist) {
        // TODO: SMS 발송 API 연동 (예: 알리고, NHN Cloud 등)
        // - 수신 번호: waitlist.getPhoneNumber()
        // - 발송 메시지: "대기 등록이 완료되었습니다. 시즌: {season}, 순번 안내는 별도 연락드리겠습니다." 등
        // - 발송 실패 시 재시도 또는 로깅 처리
    }
}
