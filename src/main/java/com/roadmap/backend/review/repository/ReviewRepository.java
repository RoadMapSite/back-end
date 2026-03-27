package com.roadmap.backend.review.repository;

import com.roadmap.backend.review.entity.Review;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Review findByReviewId(Long reviewId);
    List<Review> findByAuthorPhoneNumber(String authorPhoneNumber);
    List<Review> findByStatusAndIsTopTrueOrderByCreatedAtDesc(String status);
    Page<Review> findByStatusAndIsTopFalse(String status, Pageable pageable);

    /** 관리자: 전체 후기, 생성일 최신순 */
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** 관리자 상세: 이미지까지 한 번에 로드 (조회수 증가 없이 읽기 전용으로 사용) */
    @Query("SELECT DISTINCT r FROM Review r LEFT JOIN FETCH r.images WHERE r.reviewId = :reviewId")
    Optional<Review> findByReviewIdWithImages(@Param("reviewId") Long reviewId);
}
