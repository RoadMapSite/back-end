package com.roadmap.backend.consultation.repository;

import com.roadmap.backend.consultation.entity.Branch;
import com.roadmap.backend.consultation.entity.Consultation;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    boolean existsByBranchAndConsultationDateAndConsultationTime(
            Branch branch,
            LocalDate consultationDate,
            String consultationTime
    );

    boolean existsByPhoneNumberAndConsultationDate(String phoneNumber, LocalDate consultationDate);
}
