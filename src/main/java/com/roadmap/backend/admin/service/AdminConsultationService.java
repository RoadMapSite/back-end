package com.roadmap.backend.admin.service;

import com.roadmap.backend.admin.config.JwtProvider;
import com.roadmap.backend.admin.dto.AdminConsultationListResponse;
import com.roadmap.backend.admin.dto.ConsultationDetail;
import com.roadmap.backend.admin.exception.AdminAuthException;
import com.roadmap.backend.consultation.entity.Branch;
import com.roadmap.backend.consultation.entity.Consultation;
import com.roadmap.backend.consultation.repository.ConsultationRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminConsultationService {

    private static final String ROLE_ADMIN = "ADMIN";

    private final ConsultationRepository consultationRepository;
    private final JwtProvider jwtProvider;

    public AdminConsultationListResponse getConsultationList(
            String token, String branchParam, LocalDate startDate, LocalDate endDate) {
        validateAdminToken(token);
        Branch branch = parseBranch(branchParam);

        List<Consultation> consultations = consultationRepository
                .findByBranchAndConsultationDateBetweenOrderByConsultationDateAscConsultationTimeAsc(
                        branch, startDate, endDate);

        List<ConsultationDetail> details = consultations.stream()
                .map(c -> ConsultationDetail.builder()
                        .consultationId(c.getConsultationId())
                        .branch(c.getBranch().toApiValue())
                        .date(c.getConsultationDate())
                        .time(c.getConsultationTime())
                        .name(c.getStudentName())
                        .age(c.getStudentAge())
                        .phoneNumber(c.getPhoneNumber())
                        .registeredAt(c.getRegisteredAt())
                        .build())
                .toList();

        return AdminConsultationListResponse.builder()
                .consultations(details)
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

    private Branch parseBranch(String branchParam) {
        if (branchParam == null || branchParam.isBlank()) {
            throw new AdminAuthException("지점(branch)은 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        return Branch.fromString(branchParam);
    }
}
