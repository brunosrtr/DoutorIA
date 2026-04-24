package com.doutor.domain.entity;

import com.doutor.domain.enums.GravidadeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "diagnoses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private SymptomAssessment assessment;

    @Column(name = "nome_diagnostico", nullable = false, length = 500)
    private String nomeDiagnostico;

    @Column(name = "cid_codigo", length = 20)
    private String cidCodigo;

    @Column(name = "confianca_percentual", precision = 5, scale = 2)
    private BigDecimal confiancaPercentual;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GravidadeEnum gravidade;

    @Column
    private String justificativa;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TEXT")
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "diagnosis", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Suggestion> suggestions = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
