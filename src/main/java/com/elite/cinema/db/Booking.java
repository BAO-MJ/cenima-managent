package com.elite.cinema.db;

import com.elite.cinema.models.tables.pojos.ReservedSeats;
import org.jooq.types.ULong;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record Booking(ULong orderId, String movieTitle, String screeningRoom, LocalTime bookingTime, LocalDateTime showTime, List<ReservedSeats> seats) {}
