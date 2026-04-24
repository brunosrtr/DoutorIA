package com.doutor.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SymptomItemRequest(
        UUID symptomId,
        String nomeCustom,
        @NotNull @Min(1) @Max(10) Integer intensidade,
        Integer duracaoDias,
        String localizacaoCorpo,
        String observacoes
) {}
