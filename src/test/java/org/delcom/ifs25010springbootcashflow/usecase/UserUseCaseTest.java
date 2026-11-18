package org.delcom.ifs25010springbootcashflow.usecase;

import org.delcom.ifs25010springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25010springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25010springbootcashflow.domain.entity.Role;
import org.delcom.ifs25010springbootcashflow.domain.entity.User;
import org.delcom.ifs25010springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25010springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserUseCaseTest {
    private UserRepository repository;
    private UserUseCase useCase;
    private User admin;
    private User user;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
        useCase = new UserUseCase(repository);
        admin = repository.save("admin", PasswordHasher.hash("admin"), "Administrator", Role.ADMIN);
        user = repository.save("sherina", PasswordHasher.hash("rahasia1"), "Sherina", Role.USER);
    }

    @Test
    void listUsersIsAdminOnly() throws ValidationException {
        assertEquals(2, useCase.listUsers(admin).size());
        assertThrows(ValidationException.class, () -> useCase.listUsers(user));
    }

    @Test
    void getUserFindsOrThrows() throws EntityNotFoundException {
        assertEquals("sherina", useCase.getUser(user.getId()).getUsername());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> useCase.getUser(99));
        assertEquals(99, ex.getId());
    }

    @Test
    void updateProfileChangesNameAndOptionalPassword() throws Exception {
        String oldHash = user.getPasswordHash();

        useCase.updateProfile(user.getId(), " Sherina Pasaribu ", null);
        assertEquals("Sherina Pasaribu", user.getFullName());
        assertEquals(oldHash, user.getPasswordHash());

        useCase.updateProfile(user.getId(), "Sherina Pasaribu", "");
        assertEquals(oldHash, user.getPasswordHash());

        useCase.updateProfile(user.getId(), "Sherina Pasaribu", "baru-123");
        assertEquals(PasswordHasher.hash("baru-123"), user.getPasswordHash());
    }

    @Test
    void updateProfileValidatesInput() {
        assertThrows(ValidationException.class, () -> useCase.updateProfile(user.getId(), " ", null));
        assertThrows(ValidationException.class, () -> useCase.updateProfile(user.getId(), "Nama", "123"));
        assertThrows(EntityNotFoundException.class, () -> useCase.updateProfile(99, "Nama", null));
        assertEquals("Sherina", user.getFullName());
    }

    @Test
    void updatePhotoStoresPath() throws EntityNotFoundException {
        assertEquals("foto.png", useCase.updatePhoto(user.getId(), "foto.png").getPhotoPath());
        assertThrows(EntityNotFoundException.class, () -> useCase.updatePhoto(99, "x.png"));
    }
}
