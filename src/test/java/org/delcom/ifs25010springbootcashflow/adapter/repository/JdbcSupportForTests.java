package org.delcom.ifs25010springbootcashflow.adapter.repository;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.UUID;

/** Pembantu tes JDBC: DataSource H2 in-memory yang unik per tes, dan DataSource yang selalu gagal. */
final class JdbcSupportForTests {

    private JdbcSupportForTests() {
    }

    static DataSource newH2() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    static DataSource newH2WithSchema() {
        DataSource dataSource = newH2();
        SchemaInitializer.initialize(dataSource);
        return dataSource;
    }

    static DataSource failing() {
        return (DataSource) Proxy.newProxyInstance(
                JdbcSupportForTests.class.getClassLoader(),
                new Class<?>[]{DataSource.class},
                (proxy, method, args) -> {
                    throw new SQLException("koneksi gagal");
                });
    }
}
