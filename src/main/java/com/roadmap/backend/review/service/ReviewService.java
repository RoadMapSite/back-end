package com.roadmap.backend.review.service;

import com.roadmap.backend.auth.entity.PhoneVerification;
import com.roadmap.backend.auth.repository.PhoneVerificationRepository;
import com.roadmap.backend.review.dto.*;
import com.roadmap.backend.review.entity.Review;
import com.roadmap.backend.review.entity.ReviewImage;
import com.roadmap.backend.review.exception.ReviewException;
import com.roadmap.backend.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PhoneVerificationRepository phoneVerificationRepository;

    /**
     * 우수 후기: 승인 + isTop=true, 최신순, 페이징 없음.
     * 목록용: 작성자명 마스킹, 조회수 증가 없음.
     */
    public List<ReviewResponse> getTopApprovedReviews() {
        List<Review> topReviews = reviewRepository.findByStatusAndIsTopTrueOrderByCreatedAtDesc("APPROVED");
        return topReviews.stream().map(this::toReviewResponseForPublicList).toList();
    }

    /**
     * 일반 후기: 승인 + isTop=false만, 항상 작성일 최신순.
     * totalElements는 순수 일반 후기 건수만 포함(Page 전체 메타).
     */
    public Page<ReviewResponse> getApprovedRegularReviews(Pageable pageable) {
        Pageable sorted = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> page = reviewRepository.findApprovedNonTop("APPROVED", sorted);
        return page.map(this::toReviewResponseForPublicList);
    }

    @Transactional
    public ReviewResponse getReview(Long reviewId) {
        Review review = reviewRepository.findByReviewId(reviewId);

        if(review == null) {
            throw new ReviewException("존재하지 않는 후기입니다.", HttpStatus.NOT_FOUND);
        }

        review.incrementViewCount();

        return toReviewResponseDetail(review);
    }

    @Transactional
    public ReviewRegisterResponse registerReview(ReviewRegisterRequest request, String token) {
        // 토큰 검증
        PhoneVerification verification = validateTokenAndPhoneNumber(token);

        // DB 저장
        LocalDateTime now = LocalDateTime.now();
        Review review = Review.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .authorName(request.getName())
                .authorPhoneNumber(verification.getPhoneNumber())
                .createdAt(now)
                .updatedAt(now)
                .build();

        if(request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for(int i=0; i < request.getImageUrls().size(); i++) {
                ReviewImage reviewImage = ReviewImage.builder()
                        .imageUrl(request.getImageUrls().get(i))
                        .sortOrder(i + 1)
                        .build();
                review.addImage(reviewImage);
            }
        }

        Review saved = reviewRepository.save(review);

        return ReviewRegisterResponse.builder()
                .success(true)
                .message("후기 작성이 완료되었습니다.")
                .reviewId(saved.getReviewId())
                .build();
    }

    @Transactional
    public ReviewDeleteResponse deleteReview(Long reviewId, String token) {
        // 토큰 검증 (휴대폰 번호를 요청으로 받지 않아서, 후기에 등록된 번호와 대조)
        PhoneVerification verification = validateTokenAndPhoneNumber(token);

        Review review = reviewRepository.findByReviewId(reviewId);

        if(review == null) {
            throw new ReviewException("존재하지 않는 후기입니다.", HttpStatus.NOT_FOUND);
        }

        if(!verification.getPhoneNumber().equals(review.getAuthorPhoneNumber())) {
            throw new ReviewException("해당 후기를 삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        // DB 삭제
        reviewRepository.delete(review);

        return ReviewDeleteResponse.builder()
                .success(true)
                .message("후기 삭제가 완료되었습니다.")
                .build();
    }

    @Transactional
    public MyReviewsResponse getMyReviews(String token) {
        // 토큰 검증
        PhoneVerification verification = validateTokenAndPhoneNumber(token);

        List<Review> reviews = reviewRepository.findByAuthorPhoneNumber(verification.getPhoneNumber());

        List<MyReviewItem> myReviews = reviews.stream()
                .map(this::toMyReviewItem)
                .toList();

        return MyReviewsResponse.builder()
                .myReviews(myReviews)
                .build();
    }

    /**
     * 토큰 검증 및 휴대폰 번호 일치 확인.
     * 상담 신청 API와 동일: PhoneVerification 기반 verificationToken 검증.
     * 토큰 위조/만료 또는 번호 불일치 시 401 Unauthorized 반환.
     */
    private PhoneVerification validateTokenAndPhoneNumber(String token) {
        Optional<PhoneVerification> verificationOpt = phoneVerificationRepository
                .findFirstByVerificationTokenAndIsVerifiedTrue(token);

        if (verificationOpt.isEmpty()) {
            throw new ReviewException("유효하지 않거나 만료된 인증 토큰입니다. 휴대폰 인증을 다시 진행해주세요.", HttpStatus.UNAUTHORIZED);
        }

        PhoneVerification verification = verificationOpt.get();

        if (LocalDateTime.now().isAfter(verification.getExpiresAt())) {
            throw new ReviewException("인증 토큰이 만료되었습니다. 휴대폰 인증을 다시 진행해주세요.", HttpStatus.UNAUTHORIZED);
        }

        return verification;
    }

    /**
     * 목록·우수 후기 조회용: 마스킹, 조회수 미증가.
     */
    private ReviewResponse toReviewResponseForPublicList(Review review) {
        List<String> imageUrls = Optional.ofNullable(review.getImages())
                .orElse(List.of())
                .stream()
                .map(ReviewImage::getImageUrl)
                .toList();

        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .title(review.getTitle())
                .content(review.getContent())
                .authorName(maskName(review.getAuthorName()))
                .imageUrls(imageUrls)
                .viewCount(review.getViewCount())
                .createdAt(review.getCreatedAt())
                .build();
    }

    /**
     * 상세 조회용: 작성자명 원문, 조회수는 호출 전 증가된 값 반영.
     */
    private ReviewResponse toReviewResponseDetail(Review review) {
        List<String> imageUrls = Optional.ofNullable(review.getImages())
                .orElse(List.of())
                .stream()
                .map(ReviewImage::getImageUrl)
                .toList();

        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .title(review.getTitle())
                .content(review.getContent())
                .authorName(review.getAuthorName())
                .imageUrls(imageUrls)
                .viewCount(review.getViewCount())
                .createdAt(review.getCreatedAt())
                .build();
    }

    private MyReviewItem toMyReviewItem(Review review) {
        return MyReviewItem.builder()
                .reviewId(review.getReviewId())
                .title(review.getTitle())
                .authorName(maskName(review.getAuthorName()))
                .createdAt(review.getCreatedAt())
                .status(review.getStatus())
                .build();
    }

    /**
     * 이름 마스킹
     */
    private String maskName(String name) {
        if (name == null || name.length() < 2) {
            return name;
        }

        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }

        return name.charAt(0) + "*" + name.charAt(name.length() - 1);
    }
}
