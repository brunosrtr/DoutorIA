package com.doutor.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AddSymptomsRequest(
        @NotEmpty @Valid List<SymptomItemRequest> sintomas
) {}
