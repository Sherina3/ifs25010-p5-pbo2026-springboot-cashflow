package org.delcom.ifs25010springbootcashflow.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hash dan verifikasi password dengan SHA-256 (hasil heksadesimal).
 */
public final class PasswordHasher {

    private PasswordHasher() {
    }

    /** Menghasilkan hash SHA-256 (hex) dari password. */
    public static String hash(String rawPassword) {
        return hash(rawPassword, "SHA-256");
    }

    /** Memeriksa apakah password mentah cocok dengan hash tersimpan. */
    public static boolean matches(String rawPassword, String hashedPassword) {
        return hash(rawPassword).equals(hashedPassword);
    }

    static String hash(String rawPassword, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] bytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Algoritma hash tidak tersedia: " + algorithm, ex);
        }
    }
}
