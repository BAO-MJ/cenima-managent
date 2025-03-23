package com.elite.cinema.db;

import com.elite.cinema.models.tables.daos.*;
import lombok.Getter;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DbSet {
    private static HikariDataSource ds;
    @Getter
    private static DSLContext context;

    private DbSet() {}

    public static void initialize() {
        HikariConfig config = new HikariConfig("hikari.properties");
        ds = new HikariDataSource(config);
        context = DSL.using(ds, SQLDialect.MARIADB);
    }

    public static MoviesDao movies() {
        return new MoviesDao(context.configuration());
    }

    public static ScreeningsDao screenings() {
        return new ScreeningsDao(context.configuration());
    }

    public static ScreeningRoomsDao screeningRooms() {
        return new ScreeningRoomsDao(context.configuration());
    }

    public static RefreshmentsDao refreshments() {
        return new RefreshmentsDao(context.configuration());
    }

    public static ReservationsDao reservations() {
        return new ReservationsDao(context.configuration());
    }

    public static ReservedSeatsDao reservedSeats() {
        return new ReservedSeatsDao(context.configuration());
    }

    public static SeatsDao seats() {
        return new SeatsDao(context.configuration());
    }

    public static UsersDao users() {
        return new UsersDao(context.configuration());
    }

    public static RevenuesDao revenues()
    {
        return new RevenuesDao(context);
    }
}
