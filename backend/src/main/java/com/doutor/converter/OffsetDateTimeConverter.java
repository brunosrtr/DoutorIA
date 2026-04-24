package com.doutor.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.OffsetDateTime;

@Converter(autoApply = true)
public class OffsetDateTimeConverter implements AttributeConverter<OffsetDateTime, String> {

    @Override
    public String convertToDatabaseColumn(OffsetDateTime attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public OffsetDateTime convertToEntityAttribute(String dbData) {
        return dbData == null || dbData.isBlank() ? null : OffsetDateTime.parse(dbData);
    }
}
