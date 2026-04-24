package com.doutor.domain.entity;

import com.doutor.domain.enums.AssessmentStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "symptom_assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymptomAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentStatusEnum status = AssessmentStatusEnum.rascunho;

    @Column(name = "resumo_paciente")
    private String resumoPaciente;

    @Column(nullable = false)
    private Boolean urgente = false;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TEXT")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "TEXT")
    private OffsetDateTime updatedAt;

    @Column(name = "followup_qa", columnDefinition = "TEXT")
    private String followupQa;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AssessmentSymptom> symptoms = new ArrayList<>();

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MedicalReport> reports = new ArrayList<>();

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Diagnosis> diagnoses = new ArrayList<>();

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null) status = AssessmentStatusEnum.rascunho;
        if (urgente == null) urgente = false;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
