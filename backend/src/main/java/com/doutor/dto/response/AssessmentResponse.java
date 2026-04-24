package com.doutor.dto.response;

import com.doutor.domain.enums.AssessmentStatusEnum;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AssessmentResponse(
        UUID id,
        UUID patientId,
        AssessmentStatusEnum status,
        OffsetDateTime createdAt
) {}
