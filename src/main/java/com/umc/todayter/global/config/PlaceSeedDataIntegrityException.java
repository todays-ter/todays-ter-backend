package com.umc.todayter.global.config;

import lombok.Getter;
import org.springframework.dao.DataIntegrityViolationException;

@Getter
class PlaceSeedDataIntegrityException extends RuntimeException {

    enum Operation {
        INSERT,
        UPDATE
    }

    private final String placeName;
    private final Operation operation;
    private final DataIntegrityViolationException dataIntegrityViolationException;

    PlaceSeedDataIntegrityException(
            String placeName,
            Operation operation,
            DataIntegrityViolationException dataIntegrityViolationException
    ) {
        super(dataIntegrityViolationException);
        this.placeName = placeName;
        this.operation = operation;
        this.dataIntegrityViolationException = dataIntegrityViolationException;
    }

    boolean isInsert() {
        return operation == Operation.INSERT;
    }
}
