package com.doutor.repository;

import com.doutor.domain.entity.AssessmentSymptom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssessmentSymptomRepository extends JpaRepository<AssessmentSymptom, UUID> {

    List<AssessmentSymptom> findByAssessmentId(UUID assessmentId);
}
