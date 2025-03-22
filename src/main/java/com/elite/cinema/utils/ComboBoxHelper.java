package com.elite.cinema.utils;

import javafx.util.StringConverter;

import java.util.function.Function;

public class ComboBoxHelper
{
    public static<T> StringConverter<T> getStringConverter(Function<T, String> toString, String defaultValue)
    {
        return new StringConverter<>()
        {
            @Override
            public String toString(T object)
            {
                return object == null ? defaultValue : toString.apply(object);
            }

            @Override
            public T fromString(String string)
            {
                return null;
            }
        };
    }

    public static<T> StringConverter<T> getStringConverter(Function<T, String> toString)
    {
        return getStringConverter(toString, "");
    }
}
