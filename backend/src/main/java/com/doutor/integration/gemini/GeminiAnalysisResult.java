package com.doutor.integration.gemini;

import java.util.List;

public record GeminiAnalysisResult(
        boolean precisaMaisInfo,
        List<GeminiPergunta> perguntas,
        List<GeminiDiagnostico> diagnosticos,
        List<GeminiSugestao> sugestoes,
        String resumoGeral,
        boolean urgente
) {
    public record GeminiPergunta(
            String pergunta,
            String categoria,
            String contexto
    ) {}

    public record GeminiDiagnostico(
            String nome,
            String cid,
            Double confianca,
            String gravidade,
            String justificativa
    ) {}

    public record GeminiSugestao(
            String tipo,
            String descricao,
            Integer prioridade
    ) {}
}
