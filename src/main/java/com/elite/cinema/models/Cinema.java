/*
 * This file is generated by jOOQ.
 */
package com.elite.cinema.models;


import com.elite.cinema.models.tables.MovieInformations;
import com.elite.cinema.models.tables.Movies;
import com.elite.cinema.models.tables.ScreeningRooms;
import com.elite.cinema.models.tables.Screenings;
import com.elite.cinema.models.tables.Users;

import java.util.Arrays;
import java.util.List;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Cinema extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>cinema</code>
     */
    public static final Cinema CINEMA = new Cinema();

    /**
     * The table <code>cinema.movie_informations</code>.
     */
    public final MovieInformations MOVIE_INFORMATIONS = MovieInformations.MOVIE_INFORMATIONS;

    /**
     * The table <code>cinema.movies</code>.
     */
    public final Movies MOVIES = Movies.MOVIES;

    /**
     * The table <code>cinema.screening_rooms</code>.
     */
    public final ScreeningRooms SCREENING_ROOMS = ScreeningRooms.SCREENING_ROOMS;

    /**
     * The table <code>cinema.screenings</code>.
     */
    public final Screenings SCREENINGS = Screenings.SCREENINGS;

    /**
     * The table <code>cinema.users</code>.
     */
    public final Users USERS = Users.USERS;

    /**
     * No further instances allowed
     */
    private Cinema() {
        super(DSL.name("cinema"), null, DSL.comment(""));
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.asList(
            MovieInformations.MOVIE_INFORMATIONS,
            Movies.MOVIES,
            ScreeningRooms.SCREENING_ROOMS,
            Screenings.SCREENINGS,
            Users.USERS
        );
    }
}
