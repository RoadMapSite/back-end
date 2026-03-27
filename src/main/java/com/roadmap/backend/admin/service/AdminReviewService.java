package com.roadmap.backend.admin.service;

import com.roadmap.backend.admin.config.JwtProvider;
import com.roadmap.backend.admin.dto.AdminReviewDetailResponse;
import com.roadmap.backend.admin.dto.AdminReviewModels.PageResponse;
import com.roadmap.backend.admin.dto.AdminReviewModels.ReviewItem;
import com.roadmap.backend.admin.dto.ReviewStatusUpdateRequest;
import com.roadmap.backend.admin.dto.ReviewStatusUpdateResponse;
import com.roadmap.backend.admin.dto.ReviewTopUpdateRequest;
import com.roadmap.backend.admin.dto.ReviewTopUpdateResponse;
import com.roadmap.backend.admin.exception.AdminAuthException;
import com.roadmap.backend.review.entity.Review;
import com.roadmap.backend.review.entity.ReviewImage;
import com.roadmap.backend.review.entity.ReviewStatusType;
import com.roadmap.backend.review.repository.ReviewRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReviewService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final DateTimeFormatter CREATED_DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final ReviewRepository reviewRepository;
    private final JwtProvider jwtProvider;

    public PageResponse getAllReviews(String token, int pageOneBased, int size) {
        validateAdminToken(token);

        int pageIndex = Math.max(0, pageOneBased - 1);
        Pageable pageable = PageRequest.of(pageIndex, size);
        Page<Review> pageResult = reviewRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<ReviewItem> reviews = pageResult.getContent().stream()
                .map(this::toReviewItem)
                .toList();

        return PageResponse.builder()
                .currentPage(pageResult.getNumber() + 1)
                .totalPages(pageResult.getTotalPages())
                .totalElements(pageResult.getTotalElements())
                .reviews(reviews)
                .build();
    }

    /**
     * 관리자 후기 상세. {@code readOnly} 트랜잭션에서 조회만 하며 {@link Review#incrementViewCount()}를 호출하지 않아 조회수가 변하지 않는다.
     */
    public AdminReviewDetailResponse getReviewDetail(String token, Long reviewId) {
        validateAdminToken(token);

        Review review = reviewRepository.findByReviewIdWithImages(reviewId)
                .orElseThrow(() -> new AdminAuthException("후기를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        List<String> imageUrls = review.getImages().stream()
                .sorted(Comparator.comparing(ReviewImage::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .map(ReviewImage::getImageUrl)
                .toList();

        boolean approved = ReviewStatusType.APPROVED.name().equals(review.getStatus());
        String createdAtUtc = DateTimeFormatter.ISO_INSTANT.format(
                review.getCreatedAt().atZone(SEOUL).toInstant());

        return AdminReviewDetailResponse.builder()
                .reviewId(review.getReviewId())
                .title(review.getTitle())
                .content(review.getContent())
                .authorName(review.getAuthorName())
                .imageUrls(imageUrls)
                .viewCount(review.getViewCount())
                .isApproved(approved)
                .isBest(Boolean.TRUE.equals(review.getIsTop()))
                .createdAt(createdAtUtc)
                .build();
    }

    private ReviewItem toReviewItem(Review review) {
        boolean approved = ReviewStatusType.APPROVED.name().equals(review.getStatus());
        return ReviewItem.builder()
                .reviewId(review.getReviewId())
                .title(review.getTitle())
                .authorName(review.getAuthorName())
                .createdAt(review.getCreatedAt().toLocalDate().format(CREATED_DATE_FMT))
                .isApproved(approved)
                .isBest(Boolean.TRUE.equals(review.getIsTop()))
                .build();
    }

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
