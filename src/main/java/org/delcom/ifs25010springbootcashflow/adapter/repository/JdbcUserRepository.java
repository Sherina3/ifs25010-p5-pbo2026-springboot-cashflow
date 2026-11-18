package org.delcom.ifs25010springbootcashflow.adapter.repository;

import org.delcom.ifs25010springbootcashflow.domain.entity.Role;
import org.delcom.ifs25010springbootcashflow.domain.entity.User;
import org.delcom.ifs25010springbootcashflow.domain.repository.IUserRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** Implementasi JDBC (PostgreSQL) untuk repository pengguna. */
public class JdbcUserRepository implements IUserRepository {
    private static final String COLUMNS = "id, username, password_hash, full_name, role, photo_path";

    private final JdbcExecutor executor;

    public JdbcUserRepository(DataSource dataSource) {
        this.executor = new JdbcExecutor(dataSource);
    }

    @Override
    public List<User> findAll() {
        return executor.query("SELECT " + COLUMNS + " FROM users ORDER BY id", statement -> { }, this::map);
    }

    @Override
    public Optional<User> findById(Integer id) {
        return executor.query("SELECT " + COLUMNS + " FROM users WHERE id = ?",
                statement -> statement.setInt(1, id), this::map).stream().findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return executor.query("SELECT " + COLUMNS + " FROM users WHERE username = ?",
                statement -> statement.setString(1, username), this::map).stream().findFirst();
    }

    @Override
    public User save(User entity) {
        int id = executor.insert(
                "INSERT INTO users (username, password_hash, full_name, role, photo_path) VALUES (?, ?, ?, ?, ?)",
                statement -> {
                    statement.setString(1, entity.getUsername());
                    statement.setString(2, entity.getPasswordHash());
                    statement.setString(3, entity.getFullName());
                    statement.setString(4, entity.getRole().name());
                    statement.setString(5, entity.getPhotoPath());
                });
        return new User(id, entity.getUsername(), entity.getPasswordHash(), entity.getFullName(),
                entity.getRole(), entity.getPhotoPath());
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        return save(new User(0, username, passwordHash, fullName, role, null));
    }

    @Override
    public boolean deleteById(Integer id) {
        return executor.update("DELETE FROM users WHERE id = ?", statement -> statement.setInt(1, id)) > 0;
    }

    @Override
    public void update(User entity) {
        executor.update("UPDATE users SET password_hash = ?, full_name = ?, photo_path = ? WHERE id = ?",
                statement -> {
                    statement.setString(1, entity.getPasswordHash());
                    statement.setString(2, entity.getFullName());
                    statement.setString(3, entity.getPhotoPath());
                    statement.setInt(4, entity.getId());
                });
    }

    @Override
    public List<User> findBy(Predicate<User> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    private User map(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getInt("id"),
                resultSet.getString("username"),
                resultSet.getString("password_hash"),
                resultSet.getString("full_name"),
                Role.valueOf(resultSet.getString("role")),
                resultSet.getString("photo_path"));
    }
}
