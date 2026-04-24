package com.doutor.dto.response;

public record FollowupQuestionResponse(
        String pergunta,
        String categoria,
        String contexto,
        String resposta
) {}
