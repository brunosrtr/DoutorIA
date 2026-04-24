package com.doutor.mapper;

import com.doutor.domain.entity.Diagnosis;
import com.doutor.domain.entity.Suggestion;
import com.doutor.dto.response.DiagnosisResponse;
import com.doutor.dto.response.SuggestionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DiagnosisMapper {

    @Mapping(target = "sugestoes", source = "suggestions")
    DiagnosisResponse toResponse(Diagnosis diagnosis);

    @Mapping(target = "diagnosticId", source = "diagnosis.id")
    SuggestionResponse toSuggestionResponse(Suggestion suggestion);
}
