package com.doutor.dto.response;

import com.doutor.domain.enums.SugestaoTipoEnum;

import java.util.UUID;

public record SuggestionResponse(
        UUID id,
        UUID diagnosticId,
        SugestaoTipoEnum tipo,
        String descricao,
        Integer prioridade
) {}
