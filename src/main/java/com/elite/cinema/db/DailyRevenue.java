package com.elite.cinema.db;

import org.jooq.types.ULong;
import java.time.LocalDate;

public record DailyRevenue(
        LocalDate screeningDate,
        ULong movieId,
        String movieTitle,
        int ticketsSold,
        long revenue,
        double occupancyRate
) {}
