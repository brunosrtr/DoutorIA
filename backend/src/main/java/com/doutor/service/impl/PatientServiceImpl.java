package com.doutor.service.impl;

import com.doutor.domain.entity.Patient;
import com.doutor.dto.request.CreatePatientRequest;
import com.doutor.dto.response.PatientResponse;
import com.doutor.mapper.PatientMapper;
import com.doutor.repository.PatientRepository;
import com.doutor.service.PatientService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    @Override
    @Transactional
    public PatientResponse create(CreatePatientRequest request) {
        Patient patient = patientMapper.toEntity(request);
        return patientMapper.toResponse(patientRepository.save(patient));
    }

    @Override
    public PatientResponse findById(UUID id) {
        return patientRepository.findById(id)
                .map(patientMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Paciente não encontrado: " + id));
    }

    @Override
    public List<PatientResponse> findAll() {
        return patientRepository.findAll().stream()
                .map(patientMapper::toResponse)
                .toList();
    }
}
