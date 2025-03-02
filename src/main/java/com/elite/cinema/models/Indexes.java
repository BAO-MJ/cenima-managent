/*
 * This file is generated by jOOQ.
 */
package com.elite.cinema.models;


import com.elite.cinema.models.tables.Screenings;

import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling indexes of tables in cinema.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index SCREENINGS_MOVIE_ID = Internal.createIndex(DSL.name("movie_id"), Screenings.SCREENINGS, new OrderField[] { Screenings.SCREENINGS.MOVIE_ID }, false);
}
