package com.roadmap.backend.review.repository;

import com.roadmap.backend.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Review findByReviewId(Long reviewId);
    List<Review> findByAuthorPhoneNumber(String authorPhoneNumber);
    List<Review> findByStatusAndIsTopTrueOrderByCreatedAtDesc(String status);
    List<Review> findByStatusAndIsTopTrueAndBranchOrderByCreatedAtDesc(String status, String branch);
    Page<Review> findByStatus(String status, Pageable pageable);
    Page<Review> findByStatusAndBranch(String status, String branch, Pageable pageable);
}
