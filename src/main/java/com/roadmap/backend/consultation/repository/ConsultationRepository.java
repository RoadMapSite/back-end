package com.roadmap.backend.consultation.repository;

import com.roadmap.backend.consultation.entity.Branch;
import com.roadmap.backend.consultation.entity.Consultation;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    List<Consultation> findByBranchAndConsultationDateBetween(
            Branch branch, LocalDate startDate, LocalDate endDate);

    List<Consultation> findByBranchAndConsultationDateBetweenOrderByConsultationDateAscConsultationTimeAsc(
            Branch branch, LocalDate startDate, LocalDate endDate);

    boolean existsByBranchAndConsultationDateAndConsultationTime(
            Branch branch,
            LocalDate consultationDate,
            String consultationTime
    );

    boolean existsByPhoneNumberAndConsultationDate(String phoneNumber, LocalDate consultationDate);
}
