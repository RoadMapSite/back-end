package com.roadmap.backend.waitlist.repository;

import com.roadmap.backend.waitlist.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {

    boolean existsByPhoneNumberAndSeason(String phoneNumber, String season);
}
