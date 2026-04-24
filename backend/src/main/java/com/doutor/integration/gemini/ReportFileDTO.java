package com.doutor.integration.gemini;

public record ReportFileDTO(
        byte[] content,
        String mimeType,
        String fileName
) {}
