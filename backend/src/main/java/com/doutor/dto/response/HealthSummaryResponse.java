package com.doutor.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record HealthSummaryResponse(
        List<ImprovementPoint> pontosMelhoria,
        String resumo,
        OffsetDateTime geradoEm,
        int totalAvaliacoes,
        boolean semDados
) {
    public record ImprovementPoint(
            String titulo,
            String descricao,
            String categoria,
            String prioridade
    ) {}
}
