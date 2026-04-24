package com.doutor.service.impl;

import com.doutor.domain.entity.Diagnosis;
import com.doutor.domain.entity.Patient;
import com.doutor.domain.entity.SymptomAssessment;
import com.doutor.domain.enums.AssessmentStatusEnum;
import com.doutor.dto.response.HealthSummaryResponse;
import com.doutor.integration.gemini.GeminiHealthSummary;
import com.doutor.integration.gemini.GeminiService;
import com.doutor.integration.gemini.HealthSummaryContext;
import com.doutor.integration.gemini.PatientDTO;
import com.doutor.repository.DiagnosisRepository;
import com.doutor.repository.PatientRepository;
import com.doutor.repository.SymptomAssessmentRepository;
import com.doutor.service.HealthSummaryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HealthSummaryServiceImpl implements HealthSummaryService {

    private final PatientRepository patientRepository;
    private final SymptomAssessmentRepository assessmentRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public HealthSummaryResponse getSummary(boolean forceRefresh) {
        Patient patient = findPatient();
        if (patient == null) {
            return emptyResponse(0);
        }

        List<SymptomAssessment> completed = assessmentRepository
                .findAllByPatientIdAndStatus(patient.getId(), AssessmentStatusEnum.concluido);

        if (completed.isEmpty()) {
            return emptyResponse(0);
        }

        if (!forceRefresh && isCacheValid(patient, completed)) {
            return loadCached(patient, completed.size());
        }

        GeminiHealthSummary generated = geminiService.generateHealthSummary(buildContext(patient, completed));

        try {
            patient.setHealthSummary(objectMapper.writeValueAsString(generated));
            patient.setHealthSummaryAt(OffsetDateTime.now());
            patientRepository.save(patient);
        } catch (Exception e) {
            log.warn("Falha ao persistir resumo de saúde: {}", e.getMessage());
        }

        return toResponse(generated, patient.getHealthSummaryAt(), completed.size());
    }

    private Patient findPatient() {
        var page = patientRepository.findAll(PageRequest.of(0, 1, Sort.by("createdAt").ascending()));
        return page.isEmpty() ? null : page.getContent().get(0);
    }

    private boolean isCacheValid(Patient patient, List<SymptomAssessment> completed) {
        if (patient.getHealthSummary() == null || patient.getHealthSummaryAt() == null) return false;
        OffsetDateTime cachedAt = patient.getHealthSummaryAt();
        return completed.stream().noneMatch(a -> a.getUpdatedAt() != null && a.getUpdatedAt().isAfter(cachedAt));
    }

    private HealthSummaryResponse loadCached(Patient patient, int total) {
        try {
            GeminiHealthSummary cached = objectMapper.readValue(patient.getHealthSummary(), new TypeReference<>() {});
            return toResponse(cached, patient.getHealthSummaryAt(), total);
        } catch (Exception e) {
            log.warn("Cache de resumo inválido, ignorando: {}", e.getMessage());
            return null;
        }
    }

    private HealthSummaryResponse toResponse(GeminiHealthSummary summary, OffsetDateTime geradoEm, int total) {
        List<HealthSummaryResponse.ImprovementPoint> points = summary.pontosMelhoria().stream()
                .map(p -> new HealthSummaryResponse.ImprovementPoint(
                        p.titulo(), p.descricao(), p.categoria(), p.prioridade()))
                .toList();
        return new HealthSummaryResponse(points, summary.resumo(), geradoEm, total, false);
    }

    private HealthSummaryResponse emptyResponse(int total) {
        return new HealthSummaryResponse(List.of(), null, null, total, true);
    }

    private HealthSummaryContext buildContext(Patient patient, List<SymptomAssessment> assessments) {
        PatientDTO patientDTO = new PatientDTO(
                patient.getNome(),
                patient.getDataNascimento(),
                patient.getSexo() != null ? patient.getSexo().name() : "nao_informado",
                patient.getTipoSanguineo(),
                patient.getAlergiasConhecidas() != null ? Arrays.asList(patient.getAlergiasConhecidas()) : List.of()
        );

        List<HealthSummaryContext.AssessmentSummary> summaries = new ArrayList<>();
        List<SymptomAssessment> sorted = new ArrayList<>(assessments);
        sorted.sort(Comparator.comparing(SymptomAssessment::getCreatedAt));

        for (SymptomAssessment a : sorted) {
            List<String> sintomas = a.getSymptoms().stream()
                    .map(s -> {
                        String nome = s.getSymptomCatalog() != null ? s.getSymptomCatalog().getNome()
                                : (s.getNomeCustom() != null ? s.getNomeCustom() : "desconhecido");
                        return nome + " (intensidade " + s.getIntensidade() + "/10"
                                + (s.getDuracaoDias() != null ? ", " + s.getDuracaoDias() + "d" : "") + ")";
                    })
                    .toList();

            List<Diagnosis> diagnoses = diagnosisRepository.findByAssessmentIdOrderByConfiancaPercentualDesc(a.getId());
            List<HealthSummaryContext.DiagnosticoResumo> diagResumo = diagnoses.stream()
                    .map(d -> new HealthSummaryContext.DiagnosticoResumo(
                            d.getNomeDiagnostico(),
                            d.getGravidade() != null ? d.getGravidade().name() : null,
                            d.getConfiancaPercentual() != null ? d.getConfiancaPercentual().doubleValue() / 100.0 : null,
                            d.getJustificativa()
                    ))
                    .toList();

            List<String> sugestoes = diagnoses.stream()
                    .flatMap(d -> d.getSuggestions().stream())
                    .map(s -> s.getDescricao())
                    .toList();

            summaries.add(new HealthSummaryContext.AssessmentSummary(
                    a.getCreatedAt(),
                    Boolean.TRUE.equals(a.getUrgente()),
                    a.getResumoPaciente(),
                    sintomas,
                    diagResumo,
                    sugestoes
            ));
        }

        return new HealthSummaryContext(patientDTO, summaries);
    }
}
