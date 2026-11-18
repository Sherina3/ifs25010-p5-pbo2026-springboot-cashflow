package org.delcom.ifs25010springbootcashflow.usecase;

import org.delcom.ifs25010springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25010springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25010springbootcashflow.domain.entity.Role;
import org.delcom.ifs25010springbootcashflow.domain.entity.User;
import org.delcom.ifs25010springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthUseCaseTest {
    private UserRepository repository;
    private AuthUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
        useCase = new AuthUseCase(repository);
    }

    @Test
    void registerCreatesUserWithHashedPassword() throws ValidationException {
        User user = useCase.register("  sherina.p  ", "rahasia1", "  Sherina Pasaribu ");
        assertEquals("sherina.p", user.getUsername());
        assertEquals("Sherina Pasaribu", user.getFullName());
        assertEquals(Role.USER, user.getRole());
        assertEquals(PasswordHasher.hash("rahasia1"), user.getPasswordHash());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void registerRejectsInvalidInput() throws ValidationException {
        assertThrows(ValidationException.class, () -> useCase.register(null, "rahasia1", "Nama"));
        assertThrows(ValidationException.class, () -> useCase.register("  ", "rahasia1", "Nama"));
        assertThrows(ValidationException.class, () -> useCase.register("ab", "rahasia1", "Nama"));
        assertThrows(ValidationException.class, () -> useCase.register("a".repeat(51), "rahasia1", "Nama"));
        assertThrows(ValidationException.class, () -> useCase.register("ada spasi", "rahasia1", "Nama"));
        assertThrows(ValidationException.class, () -> useCase.register("sherina", null, "Nama"));
        assertThrows(ValidationException.class, () -> useCase.register("sherina", "   ", "Nama"));
        assertThrows(ValidationException.class, () -> useCase.register("sherina", "12345", "Nama"));
        assertThrows(ValidationException.class, () -> useCase.register("sherina", "rahasia1", null));
        assertThrows(ValidationException.class, () -> useCase.register("sherina", "rahasia1", " "));
        assertTrue(repository.findAll().isEmpty());

        useCase.register("sherina", "rahasia1", "Nama");
        ValidationException duplicate =
                assertThrows(ValidationException.class, () -> useCase.register("sherina", "rahasia2", "Lain"));
        assertEquals("Username sudah digunakan", duplicate.getMessage());
    }

    @Test
    void loginAcceptsCorrectCredentialsOnly() throws ValidationException {
        User registered = useCase.register("sherina", "rahasia1", "Sherina");

        assertEquals(registered.getId(), useCase.login(" sherina ", "rahasia1").getId());
        assertThrows(ValidationException.class, () -> useCase.login("sherina", "salah"));
        assertThrows(ValidationException.class, () -> useCase.login("tidak-ada", "rahasia1"));
        assertThrows(ValidationException.class, () -> useCase.login(null, null));
        assertThrows(ValidationException.class, () -> useCase.login("sherina", null));
    }

    @Test
    void ensureRootAdminCreatesAdminOnlyOnce() throws ValidationException {
        useCase.ensureRootAdmin("admin", "admin");
        useCase.ensureRootAdmin("admin", "ganti");

        assertEquals(1, repository.findAll().size());
        User admin = useCase.login("admin", "admin");
        assertTrue(admin.isAdmin());
        assertEquals("Administrator", admin.getFullName());
    }
}
