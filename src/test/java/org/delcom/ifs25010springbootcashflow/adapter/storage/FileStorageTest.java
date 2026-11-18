package org.delcom.ifs25010springbootcashflow.adapter.storage;

import org.delcom.ifs25010springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageTest {

    @TempDir
    Path tempDir;

    private ByteArrayInputStream data(String text) {
        return new ByteArrayInputStream(text.getBytes());
    }

    @Test
    void createsRootAndExposesLocation() throws IOException {
        Path nested = tempDir.resolve("a/b/uploads");
        FileStorage storage = new FileStorage(nested.toString());

        assertTrue(Files.isDirectory(nested));
        assertTrue(storage.rootLocation().endsWith("/"));
    }

    @Test
    void storesImagesUnderRandomNamesKeepingExtension() throws Exception {
        FileStorage storage = new FileStorage(tempDir.toString());

        String lower = storage.store("foto.png", data("isi"));
        String upper = storage.store("FOTO.JPG", data("isi2"));

        assertTrue(lower.endsWith(".png"));
        assertTrue(upper.endsWith(".jpg"));
        assertArrayEquals("isi".getBytes(), Files.readAllBytes(tempDir.resolve(lower)));
    }

    @Test
    void rejectsUnsupportedOrMissingExtensions() throws IOException {
        FileStorage storage = new FileStorage(tempDir.toString());

        assertThrows(ValidationException.class, () -> storage.store(null, data("x")));
        assertThrows(ValidationException.class, () -> storage.store("tanpa-ekstensi", data("x")));
        assertThrows(ValidationException.class, () -> storage.store("skrip.exe", data("x")));
    }

    @Test
    void deleteRemovesFileAndIgnoresEmptyOrMissingPaths() throws Exception {
        FileStorage storage = new FileStorage(tempDir.toString());
        String name = storage.store("a.gif", data("x"));

        storage.delete(name);
        assertFalse(Files.exists(tempDir.resolve(name)));

        storage.delete(name);
        storage.delete(null);
        storage.delete("   ");
        storage.delete("../../di-luar-folder.png");
    }
}
