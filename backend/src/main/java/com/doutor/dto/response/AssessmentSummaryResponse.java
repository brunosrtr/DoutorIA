package com.doutor.dto.response;

import com.doutor.domain.enums.AssessmentStatusEnum;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AssessmentSummaryResponse(
        UUID id,
        UUID patientId,
        String patientNome,
        AssessmentStatusEnum status,
        Boolean urgente,
        int totalSintomas,
        int totalDiagnosticos,
        OffsetDateTime createdAt
) {}
