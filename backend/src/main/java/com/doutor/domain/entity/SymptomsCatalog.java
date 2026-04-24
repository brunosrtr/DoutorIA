package com.doutor.domain.entity;

import com.doutor.converter.StringArrayConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "symptoms_catalog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymptomsCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String nome;

    @Column(nullable = false, length = 100)
    private String categoria;

    @Column
    private String descricao;

    @Convert(converter = StringArrayConverter.class)
    @Column(nullable = false)
    private String[] sinonimos = new String[0];

    @PrePersist
    void prePersist() {
        if (sinonimos == null) sinonimos = new String[0];
    }
}
