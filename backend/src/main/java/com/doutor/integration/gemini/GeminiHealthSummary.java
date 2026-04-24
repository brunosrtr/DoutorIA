package com.doutor.integration.gemini;

import java.util.List;

public record GeminiHealthSummary(
        String resumo,
        List<Ponto> pontosMelhoria
) {
    public record Ponto(
            String titulo,
            String descricao,
            String categoria,
            String prioridade
    ) {}
}
