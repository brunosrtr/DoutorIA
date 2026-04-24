package com.doutor.mapper;

import com.doutor.domain.entity.Patient;
import com.doutor.dto.request.CreatePatientRequest;
import com.doutor.dto.response.PatientResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Arrays;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "alergiasConhecidas", expression = "java(toArray(request.alergiasConhecidas()))")
    Patient toEntity(CreatePatientRequest request);

    @Mapping(target = "alergiasConhecidas", expression = "java(toList(patient.getAlergiasConhecidas()))")
    PatientResponse toResponse(Patient patient);

    default String[] toArray(List<String> list) {
        if (list == null) return new String[0];
        return list.toArray(new String[0]);
    }

    default List<String> toList(String[] array) {
        if (array == null) return List.of();
        return Arrays.asList(array);
    }
}
