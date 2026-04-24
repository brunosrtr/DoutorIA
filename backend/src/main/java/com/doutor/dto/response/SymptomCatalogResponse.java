package com.doutor.dto.response;

import java.util.List;
import java.util.UUID;

public record SymptomCatalogResponse(
        UUID id,
        String nome,
        String categoria,
        List<String> sinonimos
) {}
