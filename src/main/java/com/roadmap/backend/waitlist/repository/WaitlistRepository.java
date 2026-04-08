package com.roadmap.backend.waitlist.repository;

import com.roadmap.backend.waitlist.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long>, JpaSpecificationExecutor<Waitlist> {

    boolean existsByPhoneNumberAndSeason(String phoneNumber, String season);
}
