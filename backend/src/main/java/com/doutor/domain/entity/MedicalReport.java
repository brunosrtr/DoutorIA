package com.doutor.domain.entity;

import com.doutor.domain.enums.OcrStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "medical_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private SymptomAssessment assessment;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_type", nullable = false, length = 50)
    private String fileType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "texto_extraido")
    private String textoExtraido;

    @Enumerated(EnumType.STRING)
    @Column(name = "ocr_status", nullable = false)
    private OcrStatusEnum ocrStatus = OcrStatusEnum.pendente;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TEXT")
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (ocrStatus == null) ocrStatus = OcrStatusEnum.pendente;
    }
}
