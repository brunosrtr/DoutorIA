package com.doutor.dto.response;

import com.doutor.domain.enums.AssessmentStatusEnum;

import java.util.List;
import java.util.UUID;

public record AssessmentResultResponse(
        UUID assessmentId,
        AssessmentStatusEnum status,
        Boolean urgente,
        String resumoGeral,
        String message,
        List<DiagnosisResponse> diagnosticos,
        List<SuggestionResponse> sugestoes,
        List<ReportResponse> relatorios,
        List<FollowupQuestionResponse> perguntas,
        Boolean precisaMaisInfo
) {}
