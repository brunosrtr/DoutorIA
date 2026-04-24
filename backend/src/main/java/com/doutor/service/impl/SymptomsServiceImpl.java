package com.doutor.service.impl;

import com.doutor.domain.entity.SymptomsCatalog;
import com.doutor.dto.response.SymptomCatalogResponse;
import com.doutor.repository.SymptomsCatalogRepository;
import com.doutor.service.SymptomsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SymptomsServiceImpl implements SymptomsService {

    private static final int MAX_RESULTS = 20;
    private final SymptomsCatalogRepository repository;

    @Override
    public List<SymptomCatalogResponse> search(String query, String categoria) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        List<SymptomsCatalog> results = categoria != null && !categoria.isBlank()
                ? repository.findByNomeContainingIgnoreCaseAndCategoria(query.trim(), categoria)
                : repository.findByNomeContainingIgnoreCase(query.trim());

        return results.stream()
                .limit(MAX_RESULTS)
                .map(s -> new SymptomCatalogResponse(
                        s.getId(),
                        s.getNome(),
                        s.getCategoria(),
                        Arrays.asList(s.getSinonimos() != null ? s.getSinonimos() : new String[0])
                ))
                .toList();
    }
}
