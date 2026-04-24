package com.doutor.mapper;

import com.doutor.domain.entity.MedicalReport;
import com.doutor.dto.response.ReportResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    @Mapping(target = "assessmentId", source = "assessment.id")
    ReportResponse toResponse(MedicalReport report);
}
