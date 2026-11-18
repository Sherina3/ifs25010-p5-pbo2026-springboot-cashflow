package org.delcom.ifs25010springbootcashflow.adapter.storage;

import org.delcom.ifs25010springbootcashflow.domain.exception.ValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Penyimpanan file upload lokal (foto profil dan bukti transaksi).
 * File disimpan dengan nama UUID + ekstensi; yang dikembalikan hanya nama file (path relatif)
 * dan dilayani publik di URL {@code /uploads/<nama-file>}.
 */
public class FileStorage {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp");

    private final Path root;

    public FileStorage(String uploadDir) throws IOException {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    /** URI folder upload (berakhiran slash) untuk resource handler Spring MVC. */
    public String rootLocation() {
        return root.toUri().toString();
    }

    /** Menyimpan gambar dan mengembalikan nama file hasil simpan. */
    public String store(String originalFilename, InputStream data) throws ValidationException, IOException {
        String name = originalFilename == null ? "" : originalFilename;
        int dot = name.lastIndexOf('.');
        String extension = dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ValidationException("Format file tidak didukung (gunakan PNG, JPG, GIF, atau WEBP)");
        }
        String storedName = UUID.randomUUID() + "." + extension;
        Files.copy(data, root.resolve(storedName));
        return storedName;
    }

    /** Menghapus file; diabaikan bila path kosong atau file tidak ada. */
    public void delete(String path) throws IOException {
        if (path == null || path.isBlank()) {
            return;
        }
        Files.deleteIfExists(root.resolve(Path.of(path).getFileName()));
    }
}
