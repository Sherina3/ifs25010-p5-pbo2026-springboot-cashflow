package org.delcom.ifs25010springbootcashflow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DotEnvTest {
    private static final String[] KEYS = {
            "DOTENV_PLAIN", "DOTENV_SPACED", "DOTENV_SINGLE", "DOTENV_MISMATCH", "DOTENV_EMPTY", "DOTENV_PRESET"
    };

    @TempDir
    Path tempDir;

    @AfterEach
    void cleanUp() {
        for (String key : KEYS) {
            System.clearProperty(key);
        }
    }

    @Test
    void loadsKeyValuePairsAndSkipsInvalidLines() throws IOException {
        System.setProperty("DOTENV_PRESET", "dari-sistem");
        Path env = tempDir.resolve(".env");
        Files.writeString(env, String.join("\n",
                "# komentar",
                "",
                "TANPA_SAMA_DENGAN",
                "=tanpa_kunci",
                "DOTENV_PLAIN=nilai",
                " DOTENV_SPACED = \"dalam kutip\" ",
                "DOTENV_SINGLE='kutip tunggal'",
                "DOTENV_MISMATCH=\"tidak seimbang'",
                "DOTENV_EMPTY=",
                "DOTENV_PRESET=ditimpa",
                "PATH=tidak-boleh-menimpa-env"));

        DotEnv.load(env);

        assertEquals("nilai", System.getProperty("DOTENV_PLAIN"));
        assertEquals("dalam kutip", System.getProperty("DOTENV_SPACED"));
        assertEquals("kutip tunggal", System.getProperty("DOTENV_SINGLE"));
        assertEquals("\"tidak seimbang'", System.getProperty("DOTENV_MISMATCH"));
        assertEquals("", System.getProperty("DOTENV_EMPTY"));
        assertEquals("dari-sistem", System.getProperty("DOTENV_PRESET"));
        assertNull(System.getProperty("TANPA_SAMA_DENGAN"));
        assertNull(System.getProperty("PATH"));
    }

    @Test
    void ignoresMissingFileAndDirectory() {
        assertDoesNotThrow(() -> DotEnv.load(tempDir.resolve("tidak-ada.env")));
        assertDoesNotThrow(() -> DotEnv.load(tempDir));
        assertDoesNotThrow(() -> DotEnv.load());
    }

    @Test
    void ignoresUnreadableFile() throws IOException {
        Path env = tempDir.resolve("rusak.env");
        Files.write(env, new byte[]{(byte) 0xC3, (byte) 0x28, (byte) 0xA0, (byte) 0xA1});
        assertDoesNotThrow(() -> DotEnv.load(env));
    }
}
