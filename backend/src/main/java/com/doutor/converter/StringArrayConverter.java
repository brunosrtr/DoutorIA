package com.doutor.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StringArrayConverter implements AttributeConverter<String[], String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(String[] attribute) {
        try {
            return MAPPER.writeValueAsString(attribute == null ? new String[0] : attribute);
        } catch (Exception e) {
            return "[]";
        }
    }

    @Override
    public String[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return new String[0];
        try {
            return MAPPER.readValue(dbData, new TypeReference<String[]>() {});
        } catch (Exception e) {
            return new String[0];
        }
    }
}
