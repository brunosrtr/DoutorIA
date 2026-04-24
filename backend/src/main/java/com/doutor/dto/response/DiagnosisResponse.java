package com.doutor.dto.response;

import com.doutor.domain.enums.GravidadeEnum;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DiagnosisResponse(
        UUID id,
        String nomeDiagnostico,
        String cidCodigo,
        BigDecimal confiancaPercentual,
        GravidadeEnum gravidade,
        String justificativa,
        List<SuggestionResponse> sugestoes
) {}
