package com.kshrd.reactiveredis.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ConversionException extends JsonProcessingException {
    public ConversionException(String message) {
        super(message);
    }
}
