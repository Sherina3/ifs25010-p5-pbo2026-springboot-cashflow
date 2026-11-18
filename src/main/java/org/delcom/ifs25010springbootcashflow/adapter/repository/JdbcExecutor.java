package org.delcom.ifs25010springbootcashflow.adapter.repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Pembantu JDBC tipis: mengelola Connection/PreparedStatement/ResultSet dan menerjemahkan
 * SQLException menjadi IllegalStateException, sehingga repository tidak mengulang boilerplate.
 */
public class JdbcExecutor {

    /** Mengisi parameter PreparedStatement. */
    @FunctionalInterface
    public interface Binder {
        void bind(PreparedStatement statement) throws SQLException;
    }

    /** Memetakan satu baris ResultSet menjadi objek. */
    @FunctionalInterface
    public interface RowMapper<R> {
        R map(ResultSet resultSet) throws SQLException;
    }

    private final DataSource dataSource;

    public JdbcExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <R> List<R> query(String sql, Binder binder, RowMapper<R> mapper) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<R> rows = new ArrayList<>();
                while (resultSet.next()) {
                    rows.add(mapper.map(resultSet));
                }
                return rows;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal mengakses database", ex);
        }
    }

    /** Menjalankan INSERT/UPDATE/DELETE dan mengembalikan jumlah baris terdampak. */
    public int update(String sql, Binder binder) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            return statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal mengakses database", ex);
        }
    }

    /** Menjalankan INSERT dan mengembalikan nilai kolom id yang dibangkitkan. */
    public int insert(String sql, Binder binder) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"})) {
            binder.bind(statement);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal mengakses database", ex);
        }
    }

    /** Menjalankan pernyataan DDL/SQL tanpa parameter. */
    public void execute(String sql) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal mengakses database", ex);
        }
    }
}
