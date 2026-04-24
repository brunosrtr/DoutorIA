package com.doutor.integration.gemini;

import java.util.List;

public record AssessmentContext(
        PatientDTO patient,
        List<SymptomDTO> symptoms,
        List<ReportFileDTO> reports,
        List<FollowupQA> followupQA
) {
    public record FollowupQA(String pergunta, String resposta) {}
}
