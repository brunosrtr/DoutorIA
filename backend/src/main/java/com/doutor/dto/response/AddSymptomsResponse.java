package com.doutor.dto.response;

import java.util.UUID;

public record AddSymptomsResponse(
        UUID assessmentId,
        int sintomasAdicionados
) {}
