package com.doutor.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SubmitAnswersRequest(
        @NotNull List<AnswerItem> respostas
) {
    public record AnswerItem(
            @NotNull String pergunta,
            String resposta
    ) {}
}
