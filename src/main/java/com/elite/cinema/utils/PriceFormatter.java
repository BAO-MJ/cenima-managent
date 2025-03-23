package com.elite.cinema.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class PriceFormatter
{
    private static final NumberFormat CurrencyFormatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    public static String format(long price)
    {
        return CurrencyFormatter.format(price) + " VNĐ";
    }
}
