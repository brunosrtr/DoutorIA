package com.doutor.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "assessment_symptoms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentSymptom {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private SymptomAssessment assessment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "symptom_id")
    private SymptomsCatalog symptomCatalog;

    @Column(name = "nome_custom", length = 255)
    private String nomeCustom;

    @Column(nullable = false)
    private Integer intensidade;

    @Column(name = "duracao_dias")
    private Integer duracaoDias;

    @Column(name = "localizacao_corpo", length = 100)
    private String localizacaoCorpo;

    @Column
    private String observacoes;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TEXT")
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
