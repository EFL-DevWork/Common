package com.thoughtworks.user.model;


import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JacksonAnnotationsInside
@JsonSerialize(using = MaskingSerializer.class)
public @interface Mask {
    String DEFAULT_MASK = "***";
    Class<? extends DataMask> maskType() default DefaultDataMask.class;
    String defaultMaskValue() default DEFAULT_MASK;
}
