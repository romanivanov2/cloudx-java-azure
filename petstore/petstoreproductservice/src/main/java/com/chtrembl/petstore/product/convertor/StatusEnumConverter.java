package com.chtrembl.petstore.product.convertor;

import com.chtrembl.petstore.product.model.Product;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class StatusEnumConverter implements AttributeConverter<Product.StatusEnum, String> {
    @Override
    public String convertToDatabaseColumn(Product.StatusEnum status) {
        return status != null ? status.getValue() : null;
    }

    @Override
    public Product.StatusEnum convertToEntityAttribute(String value) {
        return value != null ? Product.StatusEnum.fromValue(value) : null;
    }
}
