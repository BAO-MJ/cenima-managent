package com.elite.cinema.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DbConnectionPool {
    private static HikariDataSource ds;

    private DbConnectionPool() {}

    public static HikariDataSource getConnection() {
        if (ds == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mariadb://localhost:3306/cinema");
            config.setUsername("root");
            config.setPassword("password");
            config.setMaximumPoolSize(20);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            ds = new HikariDataSource(config);
            return ds;
        }
        else {
            return ds;
        }

    }

}
