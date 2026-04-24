package com.doutor.dto.response;

import com.doutor.domain.enums.OcrStatusEnum;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReportResponse(
        UUID id,
        UUID assessmentId,
        String fileName,
        String fileType,
        Long fileSizeBytes,
        OcrStatusEnum ocrStatus,
        OffsetDateTime createdAt
) {}
