package org.delcom.ifs25010springbootcashflow.adapter.repository;

import org.delcom.ifs25010springbootcashflow.domain.entity.Role;
import org.delcom.ifs25010springbootcashflow.domain.entity.User;
import org.delcom.ifs25010springbootcashflow.domain.repository.IUserRepository;

import java.util.Optional;

/** Repository pengguna in-memory (untuk unit test). */
public class UserRepository extends InMemoryRepository<User, Integer> implements IUserRepository {
    private int idCounter = 0;

    public UserRepository() {
        super(User::getId);
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        return save(new User(++idCounter, username, passwordHash, fullName, role, null));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return data.stream().filter(user -> user.getUsername().equals(username)).findFirst();
    }
}
