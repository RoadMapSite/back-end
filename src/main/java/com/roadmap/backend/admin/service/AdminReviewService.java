package com.roadmap.backend.admin.service;

import com.roadmap.backend.admin.config.JwtProvider;
import com.roadmap.backend.admin.dto.ReviewStatusUpdateRequest;
import com.roadmap.backend.admin.dto.ReviewStatusUpdateResponse;
import com.roadmap.backend.admin.dto.ReviewTopUpdateRequest;
import com.roadmap.backend.admin.dto.ReviewTopUpdateResponse;
import com.roadmap.backend.admin.exception.AdminAuthException;
import com.roadmap.backend.review.entity.Review;
import com.roadmap.backend.review.repository.ReviewRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReviewService {

    private static final String ROLE_ADMIN = "ADMIN";

    private final ReviewRepository reviewRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public ReviewTopUpdateResponse updateReviewTop(String token, Long reviewId, ReviewTopUpdateRequest request) {
        validateAdminToken(token);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AdminAuthException("후기를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        review.updateIsTop(request.getIsTop(), now);

        return ReviewTopUpdateResponse.builder()
                .success(true)
                .message("우수 후기 설정이 변경되었습니다.")
                .build();
    }

    @Transactional
    public ReviewStatusUpdateResponse updateReviewStatus(String token, Long reviewId, ReviewStatusUpdateRequest request) {
        validateAdminToken(token);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AdminAuthException("후기를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        review.updateStatus(request.getStatus(), now);

        return ReviewStatusUpdateResponse.builder()
                .success(true)
                .message("후기 승인 상태가 변경되었습니다.")
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
}
