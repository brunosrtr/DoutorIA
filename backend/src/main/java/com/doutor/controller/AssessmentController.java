package com.doutor.controller;

import com.doutor.dto.request.AddSymptomsRequest;
import com.doutor.dto.request.SubmitAnswersRequest;
import com.doutor.dto.response.AddSymptomsResponse;
import com.doutor.dto.response.AssessmentResponse;
import com.doutor.dto.response.AssessmentResultResponse;
import com.doutor.dto.response.AssessmentSummaryResponse;
import com.doutor.dto.response.ReportResponse;
import com.doutor.service.AssessmentService;
import com.doutor.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<AssessmentResponse> create(@RequestBody Map<String, UUID> body) {
        UUID patientId = body.get("patientId");
        if (patientId == null) {
            throw new IllegalArgumentException("patientId é obrigatório");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(assessmentService.create(patientId));
    }

    @PostMapping("/{id}/symptoms")
    public ResponseEntity<AddSymptomsResponse> addSymptoms(
            @PathVariable UUID id,
            @Valid @RequestBody AddSymptomsRequest request) {
        return ResponseEntity.ok(assessmentService.addSymptoms(id, request));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<AssessmentResultResponse> submit(@PathVariable UUID id) {
        AssessmentResultResponse result = assessmentService.submit(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/answers")
    public ResponseEntity<AssessmentResultResponse> submitAnswers(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitAnswersRequest request) {
        return ResponseEntity.ok(assessmentService.submitAnswers(id, request));
    }

    @GetMapping("/{id}/result")
    public ResponseEntity<AssessmentResultResponse> getResult(@PathVariable UUID id) {
        return ResponseEntity.ok(assessmentService.getResult(id));
    }

    @PostMapping("/{id}/reports")
    public ResponseEntity<ReportResponse> addReport(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.addReport(id, file));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<AssessmentSummaryResponse>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(assessmentService.getHistory(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssessmentResultResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(assessmentService.getResult(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        assessmentService.deleteAssessment(id);
        return ResponseEntity.noContent().build();
    }
}
