package org.delcom.ifs25010springbootcashflow.usecase;

import org.delcom.ifs25010springbootcashflow.domain.exception.ValidationException;

/** Validasi input bersama yang dipakai use case. */
final class InputValidator {

    private InputValidator() {
    }

    static String username(String value) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Username tidak boleh kosong");
        }
        String trimmed = value.trim();
        if (trimmed.length() < 3 || trimmed.length() > 50) {
            throw new ValidationException("Username harus 3-50 karakter");
        }
        if (!trimmed.matches("[A-Za-z0-9._-]+")) {
            throw new ValidationException("Username hanya boleh huruf, angka, titik, garis bawah, dan strip");
        }
        return trimmed;
    }

    static void password(String value) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Password tidak boleh kosong");
        }
        if (value.length() < 6) {
            throw new ValidationException("Password minimal 6 karakter");
        }
    }

    static String fullName(String value) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong");
        }
        return value.trim();
    }
}
