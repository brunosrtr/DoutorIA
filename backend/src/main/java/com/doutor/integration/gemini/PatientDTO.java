package com.doutor.integration.gemini;

import java.time.LocalDate;
import java.util.List;

public record PatientDTO(
        String nome,
        LocalDate dataNascimento,
        String sexo,
        String tipoSanguineo,
        List<String> alergiasConhecidas
) {}
