package com.doutor.integration.gemini;

import java.time.OffsetDateTime;
import java.util.List;

public record HealthSummaryContext(
        PatientDTO patient,
        List<AssessmentSummary> assessments
) {
    public record AssessmentSummary(
            OffsetDateTime data,
            boolean urgente,
            String resumoGeral,
            List<String> sintomas,
            List<DiagnosticoResumo> diagnosticos,
            List<String> sugestoes
    ) {}

    public record DiagnosticoResumo(
            String nome,
            String gravidade,
            Double confianca,
            String justificativa
    ) {}
}
