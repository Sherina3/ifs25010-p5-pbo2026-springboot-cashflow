package org.delcom.ifs25010springbootcashflow.adapter.repository;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SchemaInitializerTest {

    @Test
    void createsTablesAndIsIdempotent() {
        DataSource dataSource = JdbcSupportForTests.newH2();

        assertDoesNotThrow(() -> SchemaInitializer.initialize(dataSource));
        assertDoesNotThrow(() -> SchemaInitializer.initialize(dataSource));

        JdbcExecutor executor = new JdbcExecutor(dataSource);
        assertEquals(0, executor.query("SELECT id FROM users", statement -> { }, resultSet -> resultSet.getInt(1)).size());
        assertEquals(0, executor.query("SELECT id FROM transactions", statement -> { }, resultSet -> resultSet.getInt(1)).size());
    }
}
