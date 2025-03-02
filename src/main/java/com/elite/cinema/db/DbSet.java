package com.elite.cinema.db;

import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;


public class DbSet implements AutoCloseable {
    private final HikariDataSource dataSource;
    private final DSLContext dslContext;

    public DbSet() {
        dataSource = DbConnectionPool.getConnection();
        dslContext = DSL.using(dataSource, SQLDialect.MARIADB);
    }

    public DSLContext context() { return dslContext; }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
