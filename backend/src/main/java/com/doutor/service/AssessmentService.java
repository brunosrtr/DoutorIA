package com.doutor.service;

import com.doutor.dto.request.AddSymptomsRequest;
import com.doutor.dto.request.SubmitAnswersRequest;
import com.doutor.dto.response.AddSymptomsResponse;
import com.doutor.dto.response.AssessmentResponse;
import com.doutor.dto.response.AssessmentResultResponse;
import com.doutor.dto.response.AssessmentSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AssessmentService {

    AssessmentResponse create(UUID patientId);

    AddSymptomsResponse addSymptoms(UUID assessmentId, AddSymptomsRequest request);

    AssessmentResultResponse submit(UUID assessmentId);

    AssessmentResultResponse submitAnswers(UUID assessmentId, SubmitAnswersRequest request);

    AssessmentResultResponse getResult(UUID assessmentId);

    Page<AssessmentSummaryResponse> getHistory(Pageable pageable);

    void deleteAssessment(UUID assessmentId);
}
