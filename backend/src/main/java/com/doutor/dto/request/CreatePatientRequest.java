package com.doutor.dto.request;

import com.doutor.domain.enums.SexoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreatePatientRequest(
        @NotBlank @Size(max = 255) String nome,
        LocalDate dataNascimento,
        SexoEnum sexo,
        @Size(max = 5) String tipoSanguineo,
        List<String> alergiasConhecidas
) {}
