package org.delcom.ifs25010springbootcashflow.usecase;

import org.delcom.ifs25010springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25010springbootcashflow.domain.entity.User;
import org.delcom.ifs25010springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25010springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25010springbootcashflow.domain.repository.IUserRepository;

import java.util.List;

/** Use case pengelolaan pengguna dan profil. */
public class UserUseCase {
    private final IUserRepository userRepository;

    public UserUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Daftar semua pengguna; hanya boleh untuk ADMIN. */
    public List<User> listUsers(User actor) throws ValidationException {
        if (!actor.isAdmin()) {
            throw new ValidationException("Akses ditolak: hanya admin");
        }
        return userRepository.findAll();
    }

    public User getUser(int id) throws EntityNotFoundException {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(id));
    }

    /** Mengubah nama; password baru opsional (kosong = tidak diubah). */
    public User updateProfile(int userId, String fullName, String newPassword)
            throws EntityNotFoundException, ValidationException {
        User user = getUser(userId);
        String validFullName = InputValidator.fullName(fullName);
        boolean changePassword = newPassword != null && !newPassword.isEmpty();
        if (changePassword) {
            InputValidator.password(newPassword);
        }
        user.changeFullName(validFullName);
        if (changePassword) {
            user.changePasswordHash(PasswordHasher.hash(newPassword));
        }
        userRepository.update(user);
        return user;
    }

    /** Mengubah path foto profil. */
    public User updatePhoto(int userId, String photoPath) throws EntityNotFoundException {
        User user = getUser(userId);
        user.changePhotoPath(photoPath);
        userRepository.update(user);
        return user;
    }
}
