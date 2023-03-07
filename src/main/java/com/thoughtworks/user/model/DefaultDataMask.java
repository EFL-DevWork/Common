package com.thoughtworks.user.model;

public class DefaultDataMask implements DataMask {
    private String mask ;

    public DefaultDataMask(String mask){
        this.mask = mask;
    }

    @Override
    public String mask(String unMaskedData) {
        return mask;
    }

}
