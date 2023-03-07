package com.thoughtworks.user.model;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

@Slf4j
public class MaskingSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        String maskedData = value;
        if (value != null) {
            Mask mask = getMaskedAnnotation(gen);
            if (mask != null) {
                try {
                    DataMask dataMask;
                    if (mask.maskType().equals(DefaultDataMask.class)) {
                        dataMask = new DefaultDataMask(mask.defaultMaskValue());
                    } else {
                        dataMask = mask.maskType().getDeclaredConstructor().newInstance();
                    }
                    maskedData = dataMask.mask(value);
                } catch (InstantiationException |
                        IllegalAccessException |
                        NoSuchMethodException |
                        InvocationTargetException e) {
                    log.error(String.format("Exception while masking the data %s", e.getCause()));
                }
            }
        }
        gen.writeString(maskedData);
    }

    public Mask getMaskedAnnotation(JsonGenerator gen) {
        Class<?> enClosingClass = gen.getCurrentValue().getClass();
        Field field = FieldUtils.getDeclaredField(enClosingClass, gen.getOutputContext().getCurrentName(), true);
        if (field.getType().equals(String.class) && field.isAnnotationPresent(Mask.class)) {
            return field.getDeclaredAnnotation(Mask.class);
        }
        return null;
    }


}
