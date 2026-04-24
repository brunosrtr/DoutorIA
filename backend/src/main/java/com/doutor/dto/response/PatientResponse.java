package com.doutor.dto.response;

import com.doutor.domain.enums.SexoEnum;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        String nome,
        LocalDate dataNascimento,
        SexoEnum sexo,
        String tipoSanguineo,
        List<String> alergiasConhecidas,
        OffsetDateTime createdAt
) {}
