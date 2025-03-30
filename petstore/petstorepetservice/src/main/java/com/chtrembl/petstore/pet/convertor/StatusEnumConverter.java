package com.chtrembl.petstore.pet.convertor;

import com.chtrembl.petstore.pet.model.Pet;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class StatusEnumConverter implements AttributeConverter<Pet.StatusEnum, String> {
    @Override
    public String convertToDatabaseColumn(Pet.StatusEnum status) {
        return status != null ? status.getValue() : null;
    }

    @Override
    public Pet.StatusEnum convertToEntityAttribute(String value) {
        return value != null ? Pet.StatusEnum.fromValue(value) : null;
    }
}
