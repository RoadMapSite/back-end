package com.roadmap.backend.admin.service;

import com.roadmap.backend.admin.config.JwtProvider;
import com.roadmap.backend.admin.dto.AdminWaitlistCreateRequest;
import com.roadmap.backend.admin.dto.AdminWaitlistResponse;
import com.roadmap.backend.admin.dto.WaitlistDeleteResponse;
import com.roadmap.backend.admin.dto.WaitlistDetail;
import com.roadmap.backend.admin.dto.WaitlistStatusUpdateRequest;
import com.roadmap.backend.admin.dto.WaitlistStatusUpdateResponse;
import com.roadmap.backend.admin.exception.AdminAuthException;
import com.roadmap.backend.admin.sms.WaitlistStatusSmsTemplate;
import com.roadmap.backend.consultation.entity.Branch;
import com.roadmap.backend.sms.service.SmsService;
import com.roadmap.backend.sms.util.SmsMessageUtil;
import com.roadmap.backend.waitlist.entity.Gender;
import com.roadmap.backend.waitlist.dto.WaitlistRegisterResponse;
import com.roadmap.backend.waitlist.entity.Season;
import com.roadmap.backend.waitlist.entity.Waitlist;
import com.roadmap.backend.waitlist.repository.WaitlistRepository;
import com.roadmap.backend.waitlist.service.WaitlistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.data.jpa.domain.Specification;
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
public class AdminWaitlistService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String DELETE_SUCCESS_MESSAGE = "해당 학생의 대기 등록이 성공적으로 삭제되었습니다.";

    private final WaitlistRepository waitlistRepository;
    private final WaitlistService waitlistService;
    private final JwtProvider jwtProvider;
    private final SmsService smsService;

    @Value("${solapi.sender.number.n}")
    private String solapiSenderNumberN;

    @Value("${solapi.sender.number.highend}")
    private String solapiSenderNumberHighend;

    @Transactional
    public WaitlistRegisterResponse createWaitlist(String token, AdminWaitlistCreateRequest request) {
        validateAdminToken(token);
        return waitlistService.registerWaitlistByAdmin(request);
    }

    public AdminWaitlistResponse getWaitlistList(String token, String seasonParam, String branchParam, Gender gender) {
        validateAdminToken(token);
        validateSeasonAndBranch(seasonParam, branchParam);

        List<Waitlist> waitlists = fetchWaitlists(seasonParam, branchParam, gender);

        List<WaitlistDetail> details = IntStream.range(0, waitlists.size())
                .mapToObj(i -> {
                    Waitlist w = waitlists.get(i);
                    return WaitlistDetail.builder()
                            .waitlistId(w.getWaitlistId())
                            .waitingNumber(i + 1)
                            .branch(w.getBranch())
                            .season(w.getSeason())
                            .name(w.getStudentName())
                            .gender(w.getGender())
                            .age(w.getStudentAge())
                            .school(w.getStudentSchool())
                            .grade(w.getStudentGrade())
                            .phoneNumber(w.getPhoneNumber())
                            .status(w.getStatus())
                            .registeredAt(w.getRegisteredAt())
                            .isExisting(w.isExisting())
                            .build();
                })
                .toList();

        return AdminWaitlistResponse.builder()
                .waitlists(details)
                .build();
    }

    @Transactional
    public WaitlistStatusUpdateResponse updateWaitlistStatus(String token, Long waitlistId, WaitlistStatusUpdateRequest request) {
        validateAdminToken(token);

        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new AdminAuthException("대기열 데이터를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        waitlist.updateStatus(request.getStatus(), now);

        WaitlistStatusSmsTemplate smsTemplate = WaitlistStatusSmsTemplate.fromStatus(request.getStatus());
        if (smsTemplate != null) {
            String phoneForSms = waitlist.getPhoneNumber();
            String seasonAndBranch = SmsMessageUtil.formatSeasonAndBranch(waitlist.getSeason(), waitlist.getBranch());
            String smsMessage = smsTemplate.format(waitlist.getStudentName(), seasonAndBranch);
            final String senderFrom = resolveAdminWaitlistStatusSmsSender(waitlist.getBranch());
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    smsService.send(phoneForSms, smsMessage, senderFrom);
                }
            });
        }

        String responseMessage = smsTemplate != null
                ? "상태가 성공적으로 변경되었으며, 해당 학생에게 알림 문자가 발송되었습니다."
                : "상태가 성공적으로 변경되었습니다.";
        return WaitlistStatusUpdateResponse.builder()
                .success(true)
                .message(responseMessage)
                .build();
    }

    @Transactional
    public WaitlistDeleteResponse deleteWaitlist(String token, Long waitlistId) {
        validateAdminToken(token);
        waitlistService.deleteWaitlistById(waitlistId);
        return WaitlistDeleteResponse.builder()
                .success(true)
                .message(DELETE_SUCCESS_MESSAGE)
                .build();
    }

    private void validateAdminToken(String token) {
        if (token == null || token.isBlank()) {
            throw new AdminAuthException("토큰이 없습니다.", HttpStatus.UNAUTHORIZED);
        }
        try {
            Claims claims = jwtProvider.parseToken(token);
            String role = claims.get(JwtProvider.CLAIM_ROLE, String.class);
            if (!ROLE_ADMIN.equals(role)) {
                throw new AdminAuthException("관리자 권한이 필요합니다.", HttpStatus.FORBIDDEN);
            }
        } catch (ExpiredJwtException e) {
            throw new AdminAuthException("토큰이 만료되었습니다. 다시 로그인해주세요.", HttpStatus.UNAUTHORIZED);
        } catch (SignatureException e) {
            throw new AdminAuthException("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            throw new AdminAuthException("인증에 실패했습니다.", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Waitlist.branch는 String 저장. 캠프(SUMMER/WINTER) 등 branch가 비어 있으면 하이엔드 발신번호(기본).
     * N → N수관, Hi-end → 하이엔드.
     */
    private String resolveAdminWaitlistStatusSmsSender(String branchStored) {
        if (branchStored == null || branchStored.isBlank()) {
            return solapiSenderNumberHighend;
        }
        try {
            Branch b = Branch.fromString(branchStored);
            if (b == null) {
                return solapiSenderNumberHighend;
            }
            return b.resolveSolapiSenderNumber(solapiSenderNumberN, solapiSenderNumberHighend);
        } catch (IllegalArgumentException e) {
            return solapiSenderNumberHighend;
        }
    }

    private void validateSeasonAndBranch(String seasonParam, String branchParam) {
        if (seasonParam == null || seasonParam.isBlank()) {
            throw new AdminAuthException("season은 필수입니다.", HttpStatus.BAD_REQUEST);
        }

        Season season;
        try {
            season = Season.valueOf(seasonParam);
        } catch (IllegalArgumentException e) {
            throw new AdminAuthException("season은 SUMMER, WINTER, SEMESTER_1, SEMESTER_2 중 하나여야 합니다.", HttpStatus.BAD_REQUEST);
        }

        if (season == Season.SEMESTER_1 || season == Season.SEMESTER_2) {
            if (branchParam == null || branchParam.isBlank()) {
                throw new AdminAuthException("SEMESTER_1, SEMESTER_2 시즌은 branch가 필수입니다.", HttpStatus.BAD_REQUEST);
            }
            String normalized = branchParam.trim();
            if (!("N".equalsIgnoreCase(normalized) || "Hi-end".equalsIgnoreCase(normalized) || "HI_END".equalsIgnoreCase(normalized))) {
                throw new AdminAuthException("branch는 N 또는 Hi-end만 입력 가능합니다.", HttpStatus.BAD_REQUEST);
            }
        }
    }

    private List<Waitlist> fetchWaitlists(String seasonParam, String branchParam, Gender genderFilter) {
        Specification<Waitlist> spec = waitlistListSpecification(seasonParam, branchParam, genderFilter);
        return waitlistRepository.findAll(spec);
    }

    /**
     * 시즌·지점(학기) 조건 및 선택적 성별 필터.
     * 정렬: isExisting DESC, registeredAt ASC (기존 재원생 우선, 동일 시 신청 순).
     */
    private Specification<Waitlist> waitlistListSpecification(String seasonParam, String branchParam, Gender genderFilter) {
        return (root, query, cb) -> {
            if (query != null) {
                query.orderBy(cb.desc(root.get("isExisting")), cb.asc(root.get("registeredAt")));
            }
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("season"), seasonParam));

            Season season = Season.valueOf(seasonParam);
            if (season == Season.SUMMER || season == Season.WINTER) {
                predicates.add(cb.isNull(root.get("branch")));
            } else {
                predicates.add(cb.equal(root.get("branch"), normalizeBranch(branchParam)));
            }
            if (genderFilter != null) {
                predicates.add(cb.equal(root.get("gender"), genderFilter));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String normalizeBranch(String branchParam) {
        String normalized = branchParam.trim();
        return "Hi-end".equalsIgnoreCase(normalized) || "HI_END".equalsIgnoreCase(normalized) ? "Hi-end" : "N";
    }
}
