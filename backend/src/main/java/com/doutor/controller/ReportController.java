package com.doutor.controller;

import com.doutor.domain.entity.MedicalReport;
import com.doutor.repository.MedicalReportRepository;
import com.doutor.service.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final MedicalReportRepository reportRepository;
    private final FileStorageService fileStorageService;

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable UUID id) {
        MedicalReport report = reportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Relatório não encontrado: " + id));

        byte[] bytes = fileStorageService.load(report.getFilePath());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(report.getFileType()));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(report.getFileName()).build()
        );
        headers.setContentLength(bytes.length);

        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
