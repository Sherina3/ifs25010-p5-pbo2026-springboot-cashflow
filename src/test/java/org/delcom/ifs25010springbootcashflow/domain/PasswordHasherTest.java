package org.delcom.ifs25010springbootcashflow.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void hashesWithSha256Hex() {
        assertEquals("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8",
                PasswordHasher.hash("password"));
    }

    @Test
    void matchesComparesAgainstStoredHash() {
        String hashed = PasswordHasher.hash("rahasia");
        assertTrue(PasswordHasher.matches("rahasia", hashed));
        assertFalse(PasswordHasher.matches("salah", hashed));
    }

    @Test
    void unknownAlgorithmIsReportedAsIllegalState() {
        assertThrows(IllegalStateException.class, () -> PasswordHasher.hash("x", "ALGORITMA-TIDAK-ADA"));
    }
}
