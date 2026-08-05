package com.umc.todayter.domain.fortune.exception;

import lombok.Getter;

@Getter
public class FortuneReportGenerationException extends RuntimeException {
    private final String failureCode;
    private final String publicMessage;

    public FortuneReportGenerationException(String failureCode, String publicMessage) {
        super(publicMessage);
        this.failureCode = failureCode;
        this.publicMessage = publicMessage;
    }

    public FortuneReportGenerationException(String failureCode, String publicMessage, Throwable cause) {
        super(publicMessage, cause);
        this.failureCode = failureCode;
        this.publicMessage = publicMessage;
    }
}
