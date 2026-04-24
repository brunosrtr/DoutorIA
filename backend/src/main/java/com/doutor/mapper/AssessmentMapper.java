package com.doutor.mapper;

import com.doutor.domain.entity.SymptomAssessment;
import com.doutor.dto.response.AssessmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssessmentMapper {

    @Mapping(target = "patientId", source = "patient.id")
    AssessmentResponse toResponse(SymptomAssessment assessment);
}
