package com.elite.cinema.db;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;

import java.time.LocalDate;
import java.util.List;

import static com.elite.cinema.models.Tables.MOVIES;
import static com.elite.cinema.models.Tables.REVENUES;
import static org.jooq.impl.DSL.*;

public class RevenuesDao
{
    private final DSLContext context;
    public RevenuesDao(DSLContext context)
    {
        this.context = context;
    }

    public List<DailyRevenue> dailyMovieRevenue(LocalDate startDate, LocalDate endDate)
    {
        return context.select(
                    REVENUES.SCREENING_TIME.cast(LocalDate.class).as("screening_date"),
                    REVENUES.MOVIE_ID,
                    MOVIES.TITLE.as("movie_title"),
                    coalesce(sum(REVENUES.TOTAL_TICKETS), 0).as("tickets_sold"),
                    coalesce(sum(REVENUES.TOTAL_REVENUE), 0).as("revenue"),
                    coalesce(avg(REVENUES.OCCUPANCY_RATE), 0).as("occupancy_rate")
                )
                .from(REVENUES)
                .join(MOVIES).on(REVENUES.MOVIE_ID.eq(MOVIES.ID))
                .where(REVENUES.SCREENING_TIME.cast(LocalDate.class).between(startDate, endDate))
                .groupBy(DSL.field("screening_date"), REVENUES.MOVIE_ID, MOVIES.TITLE)
                .fetchInto(DailyRevenue.class);
    }
}
