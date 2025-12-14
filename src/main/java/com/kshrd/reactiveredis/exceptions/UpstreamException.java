package com.kshrd.reactiveredis.exceptions;

public class UpstreamException extends RuntimeException {
    public UpstreamException(String message) {
        super(message);
    }
}
