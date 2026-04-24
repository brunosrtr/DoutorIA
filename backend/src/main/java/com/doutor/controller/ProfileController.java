package com.doutor.controller;

import com.doutor.domain.entity.Patient;
import com.doutor.dto.request.CreatePatientRequest;
import com.doutor.dto.response.PatientResponse;
import com.doutor.mapper.PatientMapper;
import com.doutor.repository.PatientRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    @GetMapping
    public ResponseEntity<PatientResponse> get() {
        return findProfile()
                .map(p -> ResponseEntity.ok(patientMapper.toResponse(p)))
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping
    @Transactional
    public ResponseEntity<PatientResponse> upsert(@Valid @RequestBody CreatePatientRequest request) {
        Patient existing = findProfile().orElse(null);
        Patient entity = patientMapper.toEntity(request);

        if (existing != null) {
            existing.setNome(entity.getNome());
            existing.setDataNascimento(entity.getDataNascimento());
            existing.setSexo(entity.getSexo());
            existing.setTipoSanguineo(entity.getTipoSanguineo());
            existing.setAlergiasConhecidas(entity.getAlergiasConhecidas());
            return ResponseEntity.ok(patientMapper.toResponse(patientRepository.save(existing)));
        }

        return ResponseEntity.ok(patientMapper.toResponse(patientRepository.save(entity)));
    }

    private java.util.Optional<Patient> findProfile() {
        List<Patient> all = patientRepository.findAll(
                PageRequest.of(0, 1, Sort.by("createdAt").ascending())
        ).getContent();
        return all.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(all.get(0));
    }
}
