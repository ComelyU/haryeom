package com.ioi.haryeom.matching.exception;

public class MatchingOperationException extends RuntimeException {

    public MatchingOperationException(String message) {
        super(message);
    }

    public MatchingOperationException(String message, Throwable cause) {
        super(message, cause);
    }

}