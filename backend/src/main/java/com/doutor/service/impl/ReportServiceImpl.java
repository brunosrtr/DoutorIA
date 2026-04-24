package com.doutor.service.impl;

import com.doutor.domain.entity.MedicalReport;
import com.doutor.domain.entity.SymptomAssessment;
import com.doutor.domain.enums.AssessmentStatusEnum;
import com.doutor.domain.enums.OcrStatusEnum;
import com.doutor.dto.response.ReportResponse;
import com.doutor.mapper.ReportMapper;
import com.doutor.repository.MedicalReportRepository;
import com.doutor.repository.SymptomAssessmentRepository;
import com.doutor.service.FileStorageService;
import com.doutor.service.ReportService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private static final int MAX_FILES = 5;
    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "application/pdf"
    );

    private final SymptomAssessmentRepository assessmentRepository;
    private final MedicalReportRepository reportRepository;
    private final FileStorageService fileStorageService;
    private final ReportMapper reportMapper;

    @Override
    public ReportResponse addReport(UUID assessmentId, MultipartFile file) {
        SymptomAssessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new EntityNotFoundException("Avaliação não encontrada: " + assessmentId));

        if (assessment.getStatus() != AssessmentStatusEnum.rascunho) {
            throw new IllegalStateException("Arquivos só podem ser adicionados a avaliações em rascunho");
        }

        long count = reportRepository.countByAssessmentId(assessmentId);
        if (count >= MAX_FILES) {
            throw new IllegalStateException("Limite de " + MAX_FILES + " arquivos por avaliação atingido");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Tipo de arquivo não suportado. Use JPG, PNG ou PDF.");
        }

        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("Arquivo excede o tamanho máximo de 10 MB");
        }

        String filePath = fileStorageService.store(assessmentId, file);

        MedicalReport report = MedicalReport.builder()
                .assessment(assessment)
                .filePath(filePath)
                .fileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "arquivo")
                .fileType(contentType)
                .fileSizeBytes(file.getSize())
                .ocrStatus(OcrStatusEnum.pendente)
                .build();

        return reportMapper.toResponse(reportRepository.save(report));
    }
}
