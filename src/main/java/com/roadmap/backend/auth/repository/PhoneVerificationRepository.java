package com.roadmap.backend.auth.repository;

import com.roadmap.backend.auth.entity.PhoneVerification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Long> {

    Optional<PhoneVerification> findTopByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);

    Optional<PhoneVerification> findFirstByVerificationTokenAndIsVerifiedTrue(String verificationToken);
}
