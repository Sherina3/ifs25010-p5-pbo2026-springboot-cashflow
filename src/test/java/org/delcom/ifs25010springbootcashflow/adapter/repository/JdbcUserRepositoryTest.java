package org.delcom.ifs25010springbootcashflow.adapter.repository;

import org.delcom.ifs25010springbootcashflow.domain.entity.Role;
import org.delcom.ifs25010springbootcashflow.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcUserRepositoryTest {
    private JdbcUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JdbcUserRepository(JdbcSupportForTests.newH2WithSchema());
    }

    @Test
    void savesAndFindsUsers() {
        User saved = repository.save("sherina", "hash", "Sherina Pasaribu", Role.USER);
        User admin = repository.save("admin", "hash2", "Administrator", Role.ADMIN);

        assertTrue(saved.getId() > 0);
        assertEquals(2, repository.findAll().size());
        User found = repository.findById(saved.getId()).orElseThrow();
        assertEquals("sherina", found.getUsername());
        assertEquals(Role.USER, found.getRole());
        assertNull(found.getPhotoPath());
        assertEquals(admin.getId(), repository.findByUsername("admin").orElseThrow().getId());
        assertTrue(repository.findByUsername("lain").isEmpty());
        assertTrue(repository.findById(999).isEmpty());
        assertEquals(1, repository.findBy(User::isAdmin).size());
    }

    @Test
    void updatesProfileFields() {
        User saved = repository.save("sherina", "hash", "Sherina", Role.USER);
        saved.changeFullName("Sherina Pasaribu");
        saved.changePasswordHash("hash-baru");
        saved.changePhotoPath("foto.png");

        repository.update(saved);

        User reloaded = repository.findById(saved.getId()).orElseThrow();
        assertEquals("Sherina Pasaribu", reloaded.getFullName());
        assertEquals("hash-baru", reloaded.getPasswordHash());
        assertEquals("foto.png", reloaded.getPhotoPath());
    }

    @Test
    void deletesById() {
        User saved = repository.save("sherina", "hash", "Sherina", Role.USER);
        assertTrue(repository.deleteById(saved.getId()));
        assertFalse(repository.deleteById(saved.getId()));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void usernameMustBeUnique() {
        repository.save("sherina", "hash", "Sherina", Role.USER);
        assertThrows(IllegalStateException.class, () -> repository.save("sherina", "x", "Lain", Role.USER));
    }
}
