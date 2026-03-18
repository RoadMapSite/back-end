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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PhoneVerificationRepository phoneVerificationRepository;

    @Transactional
    public ReviewsResponse getReviews(Integer page) {
        int pageNumber = (page == null || page < 1) ? 0 : page - 1;

        // 한 페이지에 가져올 후기 개수
        int pageSize = 10;

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Review> topReviews = reviewRepository
                .findByStatusAndIsTopTrueOrderByCreatedAtDesc("APPROVED");

        Page<Review> reviewPage = reviewRepository
                .findByStatus("APPROVED", pageable);

        // DTO 변환
        List<ReviewSummary> topDtos = topReviews.stream()
                .map(this::toReviewSummary)
                .toList();

        List<ReviewSummary> normalDtos = reviewPage.getContent().stream()
                .map(this::toReviewSummary)
                .toList();

        // 고정글 + 일반글 합치기
        List<ReviewSummary> merged = new ArrayList<>();
        merged.addAll(topDtos);
        merged.addAll(normalDtos);

        return ReviewsResponse.builder()
                .currentPage(reviewPage.getNumber() + 1)
                .totalPages(reviewPage.getTotalPages())
                .reviews(merged)
                .build();
    }

    @Transactional
    public ReviewResponse getReview(Long reviewId) {
        Review review = reviewRepository.findByReviewId(reviewId);

        if(review == null) {
            throw new ReviewException("존재하지 않는 후기입니다.", HttpStatus.NOT_FOUND);
        }

        List<String> imageUrls = Optional.ofNullable(review.getImages())
                .orElse(List.of())
                .stream()
                .map(ReviewImage :: getImageUrl)
                .toList();

        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .title(review.getTitle())
                .content(review.getContent())
                .authorName(review.getAuthorName())
                .imageUrls(imageUrls)
                .viewCount(review.getViewCount()+1)
                .createdAt(review.getCreatedAt())
                .build();
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

    private ReviewSummary toReviewSummary(Review review) {
        return ReviewSummary.builder()
                .reviewId(review.getReviewId())
                .title(review.getTitle())
                .authorName(maskName(review.getAuthorName()))
                .viewCount(review.getViewCount())
                .createdAt(review.getCreatedAt())
                .isTop(review.getIsTop())
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
