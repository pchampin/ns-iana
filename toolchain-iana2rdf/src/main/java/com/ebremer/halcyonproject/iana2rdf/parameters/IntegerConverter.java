package com.ebremer.halcyonproject.iana2rdf.parameters;

import com.beust.jcommander.IStringConverter;

public class IntegerConverter implements IStringConverter<Integer> {

    @Override
    public Integer convert(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Integer value cannot be null or empty");
        }
        return Integer.valueOf(value);
    }
}
