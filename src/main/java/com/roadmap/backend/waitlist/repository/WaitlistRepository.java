package com.roadmap.backend.waitlist.repository;

import com.roadmap.backend.waitlist.entity.Waitlist;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {

    boolean existsByPhoneNumberAndSeason(String phoneNumber, String season);

    List<Waitlist> findBySeasonOrderByIsExistingDescRegisteredAtAsc(String season);

    List<Waitlist> findBySeasonAndBranchOrderByIsExistingDescRegisteredAtAsc(String season, String branch);
}
