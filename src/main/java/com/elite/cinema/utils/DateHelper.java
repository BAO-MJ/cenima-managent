package com.elite.cinema.utils;

import java.time.LocalDate;

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
}
