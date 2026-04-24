package com.doutor.repository;

import com.doutor.domain.entity.MedicalReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicalReportRepository extends JpaRepository<MedicalReport, UUID> {

    List<MedicalReport> findByAssessmentId(UUID assessmentId);

    long countByAssessmentId(UUID assessmentId);
}
