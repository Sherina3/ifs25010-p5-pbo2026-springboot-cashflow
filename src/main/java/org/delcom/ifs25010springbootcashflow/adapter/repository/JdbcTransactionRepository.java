package org.delcom.ifs25010springbootcashflow.adapter.repository;

import org.delcom.ifs25010springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25010springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25010springbootcashflow.domain.repository.ITransactionRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** Implementasi JDBC (PostgreSQL) untuk repository transaksi. */
public class JdbcTransactionRepository implements ITransactionRepository {
    private static final String COLUMNS = "id, user_id, description, amount, type, proof_path, created_at";

    private final JdbcExecutor executor;

    public JdbcTransactionRepository(DataSource dataSource) {
        this.executor = new JdbcExecutor(dataSource);
    }

    @Override
    public List<Transaction> findAll() {
        return executor.query("SELECT " + COLUMNS + " FROM transactions ORDER BY id", statement -> { }, this::map);
    }

    @Override
    public Optional<Transaction> findById(Integer id) {
        return executor.query("SELECT " + COLUMNS + " FROM transactions WHERE id = ?",
                statement -> statement.setInt(1, id), this::map).stream().findFirst();
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return executor.query("SELECT " + COLUMNS + " FROM transactions WHERE user_id = ? ORDER BY id",
                statement -> statement.setInt(1, userId), this::map);
    }

    @Override
    public Transaction save(Transaction entity) {
        int id = executor.insert(
                "INSERT INTO transactions (user_id, description, amount, type, proof_path, created_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                statement -> {
                    statement.setInt(1, entity.getUserId());
                    statement.setString(2, entity.getDescription());
                    statement.setLong(3, entity.getAmount());
                    statement.setString(4, entity.getType().name());
                    statement.setString(5, entity.getProofPath());
                    statement.setTimestamp(6, Timestamp.valueOf(entity.getCreatedAt()));
                });
        return new Transaction(id, entity.getUserId(), entity.getDescription(), entity.getAmount(),
                entity.getType(), entity.getProofPath(), entity.getCreatedAt());
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type, String proofPath) {
        return save(new Transaction(0, userId, description, amount, type, proofPath, LocalDateTime.now()));
    }

    @Override
    public boolean deleteById(Integer id) {
        return executor.update("DELETE FROM transactions WHERE id = ?", statement -> statement.setInt(1, id)) > 0;
    }

    @Override
    public void update(Transaction entity) {
        executor.update("UPDATE transactions SET description = ?, amount = ?, proof_path = ? WHERE id = ?",
                statement -> {
                    statement.setString(1, entity.getDescription());
                    statement.setLong(2, entity.getAmount());
                    statement.setString(3, entity.getProofPath());
                    statement.setInt(4, entity.getId());
                });
    }

    @Override
    public List<Transaction> findBy(Predicate<Transaction> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    private Transaction map(ResultSet resultSet) throws SQLException {
        return new Transaction(
                resultSet.getInt("id"),
                resultSet.getInt("user_id"),
                resultSet.getString("description"),
                resultSet.getLong("amount"),
                TransactionType.valueOf(resultSet.getString("type")),
                resultSet.getString("proof_path"),
                resultSet.getTimestamp("created_at").toLocalDateTime());
    }
}
