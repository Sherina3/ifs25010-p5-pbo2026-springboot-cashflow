package org.delcom.ifs25010springbootcashflow.usecase;

import org.delcom.ifs25010springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25010springbootcashflow.domain.entity.Role;
import org.delcom.ifs25010springbootcashflow.domain.entity.User;
import org.delcom.ifs25010springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25010springbootcashflow.domain.repository.IUserRepository;

import java.util.Objects;

/** Use case autentikasi: registrasi, login, dan seed admin root. */
public class AuthUseCase {
    private static final String LOGIN_FAILED = "Username atau password salah";

    private final IUserRepository userRepository;

    public AuthUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Mendaftarkan pengguna baru dengan role USER. */
    public User register(String username, String password, String fullName) throws ValidationException {
        String validUsername = InputValidator.username(username);
        InputValidator.password(password);
        String validFullName = InputValidator.fullName(fullName);
        if (userRepository.findByUsername(validUsername).isPresent()) {
            throw new ValidationException("Username sudah digunakan");
        }
        return userRepository.save(validUsername, PasswordHasher.hash(password), validFullName, Role.USER);
    }

    /** Memverifikasi kredensial; melempar ValidationException bila salah. */
    public User login(String username, String password) throws ValidationException {
        String validUsername = Objects.toString(username, "").trim();
        User user = userRepository.findByUsername(validUsername)
                .orElseThrow(() -> new ValidationException(LOGIN_FAILED));
        if (!PasswordHasher.matches(Objects.toString(password, ""), user.getPasswordHash())) {
            throw new ValidationException(LOGIN_FAILED);
        }
        return user;
    }

    /** Membuat akun ADMIN dari konfigurasi ROOT_* bila belum ada. */
    public void ensureRootAdmin(String username, String password) {
        if (userRepository.findByUsername(username).isEmpty()) {
            userRepository.save(username, PasswordHasher.hash(password), "Administrator", Role.ADMIN);
        }
    }
}
