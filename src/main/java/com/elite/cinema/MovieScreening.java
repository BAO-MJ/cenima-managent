package com.elite.cinema;

import java.time.LocalDateTime;

import com.elite.cinema.models.enums.ScreeningsDisplayType;
import com.elite.cinema.models.enums.ScreeningsTranslationType;

@SuppressWarnings("exports")
public record MovieScreening(LocalDateTime date, ScreeningsDisplayType display,
        ScreeningsTranslationType translation) {}
