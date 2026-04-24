package com.doutor.domain.entity;

import com.doutor.domain.enums.SugestaoTipoEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "suggestions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Suggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    private Diagnosis diagnosis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SugestaoTipoEnum tipo;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private Integer prioridade = 3;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TEXT")
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (prioridade == null) prioridade = 3;
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
