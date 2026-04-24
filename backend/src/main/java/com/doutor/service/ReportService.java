package com.doutor.service;

import com.doutor.dto.response.ReportResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ReportService {

    ReportResponse addReport(UUID assessmentId, MultipartFile file);
}
