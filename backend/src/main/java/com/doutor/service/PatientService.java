package com.doutor.service;

import com.doutor.dto.request.CreatePatientRequest;
import com.doutor.dto.response.PatientResponse;

import java.util.List;
import java.util.UUID;

public interface PatientService {

    PatientResponse create(CreatePatientRequest request);

    PatientResponse findById(UUID id);

    List<PatientResponse> findAll();
}
