package org.delcom.ifs25010springbootcashflow.domain.exception;

/** Dilempar saat validasi aturan bisnis atau input gagal. */
public class ValidationException extends DomainException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
