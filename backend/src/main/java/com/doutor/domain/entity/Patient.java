package com.doutor.domain.entity;

import com.doutor.converter.StringArrayConverter;
import com.doutor.domain.enums.SexoEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String nome;

    @Column(name = "data_nascimento", columnDefinition = "TEXT")
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SexoEnum sexo = SexoEnum.nao_informado;

    @Column(name = "tipo_sanguineo", length = 5)
    private String tipoSanguineo;

    @Convert(converter = StringArrayConverter.class)
    @Column(name = "alergias_conhecidas", nullable = false)
    private String[] alergiasConhecidas = new String[0];

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TEXT")
    private OffsetDateTime createdAt;

    @Column(name = "health_summary", columnDefinition = "TEXT")
    private String healthSummary;

    @Column(name = "health_summary_at", columnDefinition = "TEXT")
    private OffsetDateTime healthSummaryAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (sexo == null) sexo = SexoEnum.nao_informado;
        if (alergiasConhecidas == null) alergiasConhecidas = new String[0];
    }
}
