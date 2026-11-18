package org.delcom.ifs25010springbootcashflow.domain.repository;

import org.delcom.ifs25010springbootcashflow.domain.entity.Role;
import org.delcom.ifs25010springbootcashflow.domain.entity.User;

import java.util.Optional;

/** Port spesifik pengguna. */
public interface IUserRepository extends IRepository<User, Integer> {
    /** Menyimpan pengguna baru. */
    User save(String username, String passwordHash, String fullName, Role role);

    Optional<User> findByUsername(String username);
}
