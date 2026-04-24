package com.doutor.integration.gemini;

public record SymptomDTO(
        String nome,
        Integer intensidade,
        Integer duracaoDias,
        String localizacaoCorpo,
        String observacoes
) {}
