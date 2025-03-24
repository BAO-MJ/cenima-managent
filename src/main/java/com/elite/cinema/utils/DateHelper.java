package com.elite.cinema.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateHelper
{
    public static LocalDate clampDate(LocalDate date, LocalDate minDate, LocalDate maxDate)
    {
        if (date.isBefore(minDate))
        {
            return minDate;
        }
        else if (date.isAfter(maxDate))
        {
            return maxDate;
        }
        return date;
    }

    public static LocalDate minDate(LocalDate a, LocalDate b)
    {
        return a.isBefore(b) ? a : b;
    }

    public static LocalDate maxDate(LocalDate a, LocalDate b)
    {
        return a.isAfter(b) ? a : b;
    }

    public static boolean between(LocalDate date, LocalDate minDate, LocalDate maxDate)
    {
        return !date.isBefore(minDate) && !date.isAfter(maxDate);
    }

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String formatDate(LocalDate date)
    {
        if (date == null)
        {
            return "";
        }
        return date.format(dateFormatter);
    }

    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    public static String formatTime(LocalTime date)
    {
        if (date == null)
        {
            return "";
        }
        return date.format(timeFormatter);
    }

    public static String formatDateTime(LocalDateTime date)
    {
        if (date == null)
        {
            return "";
        }
        return date.format(dateFormatter) + " " + date.format(timeFormatter);
    }
}
