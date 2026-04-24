package com.doutor.repository;

import com.doutor.domain.entity.SymptomAssessment;
import com.doutor.domain.enums.AssessmentStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SymptomAssessmentRepository extends JpaRepository<SymptomAssessment, UUID> {

    @Query("SELECT a FROM SymptomAssessment a WHERE a.patient.id = :patientId ORDER BY a.createdAt DESC")
    Page<SymptomAssessment> findByPatientIdOrderByCreatedAtDesc(@Param("patientId") UUID patientId, Pageable pageable);

    @Query("SELECT a FROM SymptomAssessment a WHERE a.patient.id = :patientId AND a.status = :status ORDER BY a.createdAt DESC")
    Page<SymptomAssessment> findByPatientIdAndStatusOrderByCreatedAtDesc(
            @Param("patientId") UUID patientId,
            @Param("status") AssessmentStatusEnum status,
            Pageable pageable);

    @Query("SELECT a FROM SymptomAssessment a LEFT JOIN FETCH a.symptoms LEFT JOIN FETCH a.reports LEFT JOIN FETCH a.diagnoses WHERE a.id = :id")
    Optional<SymptomAssessment> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT a FROM SymptomAssessment a ORDER BY a.createdAt DESC")
    Page<SymptomAssessment> findAllOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT a FROM SymptomAssessment a WHERE a.patient.id = :patientId AND a.status = :status ORDER BY a.createdAt DESC")
    java.util.List<SymptomAssessment> findAllByPatientIdAndStatus(
            @Param("patientId") UUID patientId,
            @Param("status") AssessmentStatusEnum status);
}
