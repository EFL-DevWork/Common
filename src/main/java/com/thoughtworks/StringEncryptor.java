package com.thoughtworks;

import com.thoughtworks.config.CryptoConfig;

import javax.persistence.AttributeConverter;

public class StringEncryptor implements AttributeConverter<String , String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return CryptoConfig.get().encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return CryptoConfig.get().decrypt(dbData);
    }
}
