package com.roadmap.backend.waitlist.service;

import com.roadmap.backend.domain.Grade;
import com.roadmap.backend.auth.entity.PhoneVerification;
import com.roadmap.backend.auth.repository.PhoneVerificationRepository;
import com.roadmap.backend.consultation.entity.Branch;
import com.roadmap.backend.admin.dto.AdminWaitlistCreateRequest;
import com.roadmap.backend.waitlist.dto.WaitlistRegisterRequest;
import com.roadmap.backend.waitlist.dto.WaitlistRegisterResponse;
import com.roadmap.backend.waitlist.entity.Season;
import com.roadmap.backend.waitlist.entity.Waitlist;
import com.roadmap.backend.waitlist.exception.WaitlistException;
import com.roadmap.backend.sms.service.SmsService;
import com.roadmap.backend.sms.util.SmsMessageUtil;
import com.roadmap.backend.waitlist.repository.WaitlistRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final PhoneVerificationRepository phoneVerificationRepository;
    private final SmsService smsService;

    @Value("${solapi.sender.number.n}")
    private String solapiSenderNumberN;

    @Value("${solapi.sender.number.highend}")
    private String solapiSenderNumberHighend;

    /** 관리자용: 대기열 ID로 조회 후 물리 삭제. 없으면 EntityNotFoundException. */
    @Transactional
    public void deleteWaitlistById(Long waitlistId) {
        Waitlist waitlist = waitlistRepository
                .findById(waitlistId)
                .orElseThrow(() -> new EntityNotFoundException("대기열을 찾을 수 없습니다. (waitlistId=" + waitlistId + ")"));
        waitlistRepository.delete(waitlist);
    }

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

        // 나이 vs 학교·학년 검증
        validateAgeOrSchoolGrade(
                request.getSeason(),
                branchToSave,
                request.getAge(),
                request.getSchool(),
                request.getGrade());

        // DB 저장
        LocalDateTime now = LocalDateTime.now();
        Waitlist.WaitlistBuilder builder = Waitlist.builder()
                .branch(branchToSave)
                .season(request.getSeason())
                .studentName(request.getName())
                .gender(request.getGender())
                .phoneNumber(phoneNumber)
                .isExisting(Boolean.TRUE.equals(request.getIsExisting()))
                .status("WAITING")
                .registeredAt(now)
                .updatedAt(now);

        Season seasonEnum = Season.valueOf(request.getSeason());
        if (seasonEnum == Season.SUMMER || seasonEnum == Season.WINTER) {
            builder.studentAge(request.getAge())
                    .studentSchool(request.getSchool())
                    .studentGrade(request.getGrade());
        } else if ("N".equals(branchToSave)) {
            builder.studentAge(request.getAge()).studentSchool(null).studentGrade(null);
        } else {
            builder.studentAge(null).studentSchool(request.getSchool()).studentGrade(request.getGrade());
        }

        Waitlist waitlist = builder.build();

        Waitlist saved = waitlistRepository.save(waitlist);

        String phoneForSms = saved.getPhoneNumber();
        String smsMessage = buildWaitlistRegisterSmsMessage(saved);
        final String senderFrom = resolveWaitlistSmsSenderFrom(branchToSave);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                smsService.send(phoneForSms, smsMessage, senderFrom);
            }
        });

        return WaitlistRegisterResponse.builder()
                .success(true)
                .message("대기 등록이 완료되었습니다.")
                .waitlistId(saved.getWaitlistId())
                .registeredAt(saved.getRegisteredAt())
                .build();
    }

    /**
     * 관리자 직접 등록: SMS·알림톡 없이 DB 저장만 수행.
     */
    @Transactional
    public WaitlistRegisterResponse registerWaitlistByAdmin(AdminWaitlistCreateRequest request) {
        String phoneNumber = normalizePhoneDigits(request.getPhoneNumber());
        if (phoneNumber.isBlank()) {
            throw new IllegalArgumentException("휴대폰 번호는 필수입니다.");
        }

        if (waitlistRepository.existsByPhoneNumberAndSeason(phoneNumber, request.getSeason())) {
            throw new IllegalArgumentException("이미 해당 시즌에 대기 등록이 완료된 연락처입니다.");
        }

        String branchToSave;
        try {
            branchToSave = resolveBranch(request.getSeason(), request.getBranch());
        } catch (WaitlistException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        try {
            validateAgeOrSchoolGrade(
                    request.getSeason(),
                    branchToSave,
                    request.getAge(),
                    request.getSchool(),
                    request.getGrade());
        } catch (WaitlistException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        LocalDateTime now = LocalDateTime.now();
        Waitlist.WaitlistBuilder builder = Waitlist.builder()
                .branch(branchToSave)
                .season(request.getSeason())
                .studentName(request.getName())
                .gender(request.getGender())
                .phoneNumber(phoneNumber)
                .isExisting(Boolean.TRUE.equals(request.getIsExisting()))
                .status("WAITING")
                .registeredAt(now)
                .updatedAt(now);

        Season seasonEnum = Season.valueOf(request.getSeason());
        if (seasonEnum == Season.SUMMER || seasonEnum == Season.WINTER) {
            builder.studentAge(request.getAge())
                    .studentSchool(request.getSchool())
                    .studentGrade(request.getGrade());
        } else if ("N".equals(branchToSave)) {
            builder.studentAge(request.getAge()).studentSchool(null).studentGrade(null);
        } else {
            builder.studentAge(null).studentSchool(request.getSchool()).studentGrade(request.getGrade());
        }

        Waitlist saved = waitlistRepository.save(builder.build());

        return WaitlistRegisterResponse.builder()
                .success(true)
                .message("대기 등록이 완료되었습니다.")
                .waitlistId(saved.getWaitlistId())
                .registeredAt(saved.getRegisteredAt())
                .build();
    }

    private static String normalizePhoneDigits(String phoneNumber) {
        if (phoneNumber == null) {
            return "";
        }
        return phoneNumber.replaceAll("[^0-9]", "");
    }

    /**
     * 대기 등록 SMS 발신 번호. 캠프(SUMMER/WINTER) 등 branch가 없을 때는 하이엔드 번호(인증·관리자와 동일).
     * 학기(SEMESTER)는 N / Hi-end 문자열로 분기.
     */
    private String resolveWaitlistSmsSenderFrom(String branchSavedOrNull) {
        if (branchSavedOrNull == null || branchSavedOrNull.isBlank()) {
            return solapiSenderNumberHighend;
        }
        try {
            Branch b = Branch.fromString(branchSavedOrNull);
            if (b == null) {
                return solapiSenderNumberHighend;
            }
            return b.resolveSolapiSenderNumber(solapiSenderNumberN, solapiSenderNumberHighend);
        } catch (IllegalArgumentException e) {
            return solapiSenderNumberHighend;
        }
    }

    /**
     * 등록 대기 완료 SMS 메시지 생성.
     */
    private String buildWaitlistRegisterSmsMessage(Waitlist waitlist) {
        String seasonAndBranch = SmsMessageUtil.formatSeasonAndBranch(waitlist.getSeason(), waitlist.getBranch());
        return String.format(
                "[로드맵 독서실]\n"
                        + "%s 학생의 %s 등록 예약이 완료되었습니다. 등록 가능 시점에 다시 안내 드리겠습니다. 감사합니다.",
                waitlist.getStudentName(),
                seasonAndBranch
        );
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
     * 시즌·branch에 따른 나이·학교·학년 검증.
     * - SUMMER, WINTER: 나이, 학교, 학년 모두 필수
     * - SEMESTER + N: 나이 필수, 학교·학년은 없어야 함
     * - SEMESTER + Hi-end: 학교·학년 필수, 나이는 없어야 함
     */
    private void validateAgeOrSchoolGrade(String season, String branch, Integer age, String school, Grade grade) {
        Season s = Season.valueOf(season);
        boolean hasAge = age != null;
        boolean hasSchoolGrade = (school != null && !school.isBlank()) && grade != null;

        if (s == Season.SUMMER || s == Season.WINTER) {
            if (!hasAge || !hasSchoolGrade) {
                throw new WaitlistException("여름캠프·겨울캠프는 나이, 학교, 학년을 모두 입력해주세요.", HttpStatus.BAD_REQUEST);
            }
            return;
        }

        boolean isN = "N".equals(branch);
        if (isN) {
            if (!hasAge) {
                throw new WaitlistException("N수관은 나이를 필수로 입력해주세요.", HttpStatus.BAD_REQUEST);
            }
            if (hasSchoolGrade) {
                throw new WaitlistException("N수관은 나이만 입력 가능합니다.", HttpStatus.BAD_REQUEST);
            }
        } else {
            if (!hasSchoolGrade) {
                throw new WaitlistException("Hi-end는 학교와 학년을 필수로 입력해주세요.", HttpStatus.BAD_REQUEST);
            }
            if (hasAge) {
                throw new WaitlistException("Hi-end는 학교·학년만 입력 가능합니다.", HttpStatus.BAD_REQUEST);
            }
        }
    }

}
