package com.doutor.service.impl;

import com.doutor.domain.entity.*;
import com.doutor.domain.enums.AssessmentStatusEnum;
import com.doutor.domain.enums.GravidadeEnum;
import com.doutor.domain.enums.SugestaoTipoEnum;
import com.doutor.dto.request.AddSymptomsRequest;
import com.doutor.dto.request.SubmitAnswersRequest;
import com.doutor.dto.request.SymptomItemRequest;
import com.doutor.dto.response.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.doutor.integration.gemini.*;
import com.doutor.mapper.AssessmentMapper;
import com.doutor.mapper.DiagnosisMapper;
import com.doutor.mapper.ReportMapper;
import com.doutor.repository.*;
import com.doutor.service.AssessmentService;
import com.doutor.service.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AssessmentServiceImpl implements AssessmentService {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final SymptomAssessmentRepository assessmentRepository;
    private final PatientRepository patientRepository;
    private final SymptomsCatalogRepository symptomsCatalogRepository;
    private final AssessmentSymptomRepository assessmentSymptomRepository;
    private final MedicalReportRepository medicalReportRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final SuggestionRepository suggestionRepository;
    private final AssessmentMapper assessmentMapper;
    private final DiagnosisMapper diagnosisMapper;
    private final ReportMapper reportMapper;
    private final GeminiService geminiService;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public AssessmentResponse create(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Paciente não encontrado: " + patientId));

        SymptomAssessment assessment = SymptomAssessment.builder()
                .patient(patient)
                .status(AssessmentStatusEnum.rascunho)
                .urgente(false)
                .build();

        return assessmentMapper.toResponse(assessmentRepository.save(assessment));
    }

    @Override
    @Transactional
    public AddSymptomsResponse addSymptoms(UUID assessmentId, AddSymptomsRequest request) {
        SymptomAssessment assessment = findRascunho(assessmentId);

        List<AssessmentSymptom> symptoms = request.sintomas().stream()
                .map(item -> buildSymptom(assessment, item))
                .toList();

        assessmentSymptomRepository.saveAll(symptoms);
        return new AddSymptomsResponse(assessmentId, symptoms.size());
    }

    @Override
    @Transactional
    public AssessmentResultResponse submit(UUID assessmentId) {
        SymptomAssessment assessment = findRascunho(assessmentId);

        List<AssessmentSymptom> symptoms = assessmentSymptomRepository.findByAssessmentId(assessmentId);
        if (symptoms.isEmpty()) {
            throw new IllegalArgumentException("A avaliação precisa ter pelo menos 1 sintoma para ser submetida");
        }

        assessment.setStatus(AssessmentStatusEnum.processando);
        assessmentRepository.save(assessment);

        return runAnalysis(assessment, symptoms);
    }

    @Override
    @Transactional
    public AssessmentResultResponse submitAnswers(UUID assessmentId, SubmitAnswersRequest request) {
        SymptomAssessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new EntityNotFoundException("Avaliação não encontrada: " + assessmentId));

        if (assessment.getStatus() != AssessmentStatusEnum.processando) {
            throw new IllegalStateException("Avaliação não está aguardando respostas (status: " + assessment.getStatus() + ")");
        }

        List<FollowupQuestionResponse> existing = loadFollowupQa(assessment);
        Map<String, String> answerMap = new HashMap<>();
        for (var a : request.respostas()) {
            if (a.resposta() != null && !a.resposta().isBlank()) {
                answerMap.put(a.pergunta(), a.resposta());
            }
        }

        List<FollowupQuestionResponse> updated = existing.stream()
                .map(q -> answerMap.containsKey(q.pergunta())
                        ? new FollowupQuestionResponse(q.pergunta(), q.categoria(), q.contexto(), answerMap.get(q.pergunta()))
                        : q)
                .toList();

        saveFollowupQa(assessment, updated);

        List<AssessmentSymptom> symptoms = assessmentSymptomRepository.findByAssessmentId(assessmentId);
        return runAnalysis(assessment, symptoms);
    }

    private AssessmentResultResponse runAnalysis(SymptomAssessment assessment, List<AssessmentSymptom> symptoms) {
        try {
            List<FollowupQuestionResponse> history = loadFollowupQa(assessment);
            AssessmentContext context = buildContext(assessment, symptoms, history);
            GeminiAnalysisResult result = geminiService.analyze(context);

            if (result.precisaMaisInfo() && !result.perguntas().isEmpty()) {
                List<FollowupQuestionResponse> merged = new ArrayList<>(history);
                for (var q : result.perguntas()) {
                    boolean alreadyAsked = merged.stream().anyMatch(m -> m.pergunta().equalsIgnoreCase(q.pergunta()));
                    if (!alreadyAsked) {
                        merged.add(new FollowupQuestionResponse(q.pergunta(), q.categoria(), q.contexto(), null));
                    }
                }
                saveFollowupQa(assessment, merged);
                assessment.setStatus(AssessmentStatusEnum.processando);
                assessmentRepository.save(assessment);
                return buildPendingResponse(assessment, merged);
            }

            assessment.setUrgente(result.urgente());
            assessment.setResumoPaciente(result.resumoGeral());
            assessment.setStatus(AssessmentStatusEnum.concluido);

            List<Diagnosis> diagnoses = persistDiagnoses(assessment, result);
            assessmentRepository.save(assessment);

            medicalReportRepository.findByAssessmentId(assessment.getId()).forEach(r -> {
                r.setOcrStatus(com.doutor.domain.enums.OcrStatusEnum.processado);
                medicalReportRepository.save(r);
            });

            return buildResultResponse(assessment, diagnoses);
        } catch (Exception e) {
            log.error("Erro ao processar avaliação {}: {}", assessment.getId(), e.getMessage(), e);
            assessment.setStatus(AssessmentStatusEnum.erro);
            assessmentRepository.save(assessment);
            throw new RuntimeException("Falha na análise: " + e.getMessage(), e);
        }
    }

    @Override
    public AssessmentResultResponse getResult(UUID assessmentId) {
        SymptomAssessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new EntityNotFoundException("Avaliação não encontrada: " + assessmentId));

        List<FollowupQuestionResponse> followups = loadFollowupQa(assessment);
        boolean hasPending = followups.stream().anyMatch(q -> q.resposta() == null || q.resposta().isBlank());

        if (assessment.getStatus() == AssessmentStatusEnum.processando && hasPending) {
            return buildPendingResponse(assessment, followups);
        }

        if (assessment.getStatus() == AssessmentStatusEnum.rascunho) {
            return new AssessmentResultResponse(assessmentId, assessment.getStatus(),
                    null, null, "Avaliação em rascunho.", null, null, null, null, null);
        }

        List<Diagnosis> diagnoses = diagnosisRepository.findByAssessmentIdOrderByConfiancaPercentualDesc(assessmentId);
        return buildResultResponse(assessment, diagnoses);
    }

    private SymptomAssessment findRascunho(UUID assessmentId) {
        SymptomAssessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new EntityNotFoundException("Avaliação não encontrada: " + assessmentId));
        if (assessment.getStatus() != AssessmentStatusEnum.rascunho) {
            throw new IllegalStateException("Avaliação não está em status rascunho: " + assessment.getStatus());
        }
        return assessment;
    }

    private AssessmentSymptom buildSymptom(SymptomAssessment assessment, SymptomItemRequest item) {
        AssessmentSymptom symptom = AssessmentSymptom.builder()
                .assessment(assessment)
                .intensidade(item.intensidade())
                .duracaoDias(item.duracaoDias())
                .localizacaoCorpo(item.localizacaoCorpo())
                .observacoes(item.observacoes())
                .build();

        if (item.symptomId() != null) {
            symptomsCatalogRepository.findById(item.symptomId())
                    .ifPresent(symptom::setSymptomCatalog);
        }
        if (item.nomeCustom() != null && !item.nomeCustom().isBlank()) {
            symptom.setNomeCustom(item.nomeCustom());
        }
        return symptom;
    }

    private AssessmentContext buildContext(SymptomAssessment assessment, List<AssessmentSymptom> symptoms,
                                           List<FollowupQuestionResponse> followups) {
        Patient patient = assessment.getPatient();
        PatientDTO patientDTO = new PatientDTO(
                patient.getNome(),
                patient.getDataNascimento(),
                patient.getSexo() != null ? patient.getSexo().name() : "nao_informado",
                patient.getTipoSanguineo(),
                patient.getAlergiasConhecidas() != null ? Arrays.asList(patient.getAlergiasConhecidas()) : List.of()
        );

        List<SymptomDTO> symptomDTOs = symptoms.stream()
                .map(s -> new SymptomDTO(
                        s.getSymptomCatalog() != null ? s.getSymptomCatalog().getNome() :
                                (s.getNomeCustom() != null ? s.getNomeCustom() : "Sintoma não identificado"),
                        s.getIntensidade(),
                        s.getDuracaoDias(),
                        s.getLocalizacaoCorpo(),
                        s.getObservacoes()
                ))
                .toList();

        List<ReportFileDTO> reportDTOs = medicalReportRepository
                .findByAssessmentId(assessment.getId())
                .stream()
                .map(r -> {
                    try {
                        byte[] bytes = fileStorageService.load(r.getFilePath());
                        return new ReportFileDTO(bytes, r.getFileType(), r.getFileName());
                    } catch (Exception e) {
                        log.warn("Falha ao carregar relatório {}: {}", r.getId(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        List<AssessmentContext.FollowupQA> qa = followups.stream()
                .filter(f -> f.resposta() != null && !f.resposta().isBlank())
                .map(f -> new AssessmentContext.FollowupQA(f.pergunta(), f.resposta()))
                .toList();

        return new AssessmentContext(patientDTO, symptomDTOs, reportDTOs, qa);
    }

    private List<Diagnosis> persistDiagnoses(SymptomAssessment assessment, GeminiAnalysisResult result) {
        List<Diagnosis> diagnoses = new ArrayList<>();

        for (var gDiag : result.diagnosticos()) {
            GravidadeEnum gravidade;
            try {
                gravidade = GravidadeEnum.valueOf(gDiag.gravidade().toLowerCase());
            } catch (Exception e) {
                gravidade = GravidadeEnum.media;
            }

            Diagnosis diagnosis = Diagnosis.builder()
                    .assessment(assessment)
                    .nomeDiagnostico(gDiag.nome())
                    .cidCodigo(gDiag.cid())
                    .confiancaPercentual(gDiag.confianca() != null
                            ? BigDecimal.valueOf(gDiag.confianca() * 100).setScale(2, java.math.RoundingMode.HALF_UP)
                            : null)
                    .gravidade(gravidade)
                    .justificativa(gDiag.justificativa())
                    .build();

            Diagnosis saved = diagnosisRepository.save(diagnosis);
            diagnoses.add(saved);
        }

        if (!result.sugestoes().isEmpty() && !diagnoses.isEmpty()) {
            for (var gSug : result.sugestoes()) {
                SugestaoTipoEnum tipo;
                try {
                    tipo = SugestaoTipoEnum.valueOf(gSug.tipo().toLowerCase());
                } catch (Exception e) {
                    tipo = SugestaoTipoEnum.habito;
                }

                Suggestion suggestion = Suggestion.builder()
                        .diagnosis(diagnoses.get(0))
                        .tipo(tipo)
                        .descricao(gSug.descricao())
                        .prioridade(gSug.prioridade() != null ? gSug.prioridade() : 3)
                        .build();
                suggestionRepository.save(suggestion);
                diagnoses.get(0).getSuggestions().add(suggestion);
            }
        }

        return diagnoses;
    }

    @Override
    public Page<AssessmentSummaryResponse> getHistory(Pageable pageable) {
        return assessmentRepository.findAllOrderByCreatedAtDesc(pageable)
                .map(a -> new AssessmentSummaryResponse(
                        a.getId(),
                        a.getPatient().getId(),
                        a.getPatient().getNome(),
                        a.getStatus(),
                        a.getUrgente(),
                        a.getSymptoms().size(),
                        a.getDiagnoses().size(),
                        a.getCreatedAt()
                ));
    }

    @Override
    @Transactional
    public void deleteAssessment(UUID assessmentId) {
        if (!assessmentRepository.existsById(assessmentId)) {
            throw new EntityNotFoundException("Avaliação não encontrada: " + assessmentId);
        }
        assessmentRepository.deleteById(assessmentId);
    }

    private AssessmentResultResponse buildResultResponse(SymptomAssessment assessment, List<Diagnosis> diagnoses) {
        List<DiagnosisResponse> diagnosisResponses = diagnoses.stream()
                .map(diagnosisMapper::toResponse)
                .toList();

        List<SuggestionResponse> allSuggestions = diagnosisResponses.stream()
                .flatMap(d -> d.sugestoes().stream())
                .sorted(Comparator.comparingInt(SuggestionResponse::prioridade))
                .toList();

        List<ReportResponse> reports = medicalReportRepository
                .findByAssessmentId(assessment.getId())
                .stream()
                .map(reportMapper::toResponse)
                .toList();

        List<FollowupQuestionResponse> followups = loadFollowupQa(assessment);

        return new AssessmentResultResponse(
                assessment.getId(),
                assessment.getStatus(),
                assessment.getUrgente(),
                assessment.getResumoPaciente(),
                null,
                diagnosisResponses,
                allSuggestions,
                reports,
                followups,
                false
        );
    }

    private AssessmentResultResponse buildPendingResponse(SymptomAssessment assessment,
                                                           List<FollowupQuestionResponse> followups) {
        List<ReportResponse> reports = medicalReportRepository
                .findByAssessmentId(assessment.getId())
                .stream()
                .map(reportMapper::toResponse)
                .toList();

        return new AssessmentResultResponse(
                assessment.getId(),
                assessment.getStatus(),
                assessment.getUrgente(),
                assessment.getResumoPaciente(),
                "A IA precisa de mais informações para refinar a análise.",
                List.of(),
                List.of(),
                reports,
                followups,
                true
        );
    }

    private List<FollowupQuestionResponse> loadFollowupQa(SymptomAssessment assessment) {
        String raw = assessment.getFollowupQa();
        if (raw == null || raw.isBlank()) return new ArrayList<>();
        try {
            return JSON.readValue(raw, new TypeReference<List<FollowupQuestionResponse>>() {});
        } catch (Exception e) {
            log.warn("Falha ao ler followup_qa da avaliação {}: {}", assessment.getId(), e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveFollowupQa(SymptomAssessment assessment, List<FollowupQuestionResponse> data) {
        try {
            assessment.setFollowupQa(JSON.writeValueAsString(data));
        } catch (Exception e) {
            log.error("Falha ao serializar followup_qa: {}", e.getMessage());
        }
    }
}
