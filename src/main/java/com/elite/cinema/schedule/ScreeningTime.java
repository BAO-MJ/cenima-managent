package com.elite.cinema.schedule;

import java.time.LocalDateTime;

import org.jooq.types.ULong;

import com.elite.cinema.models.enums.ScreeningsDisplayType;
import com.elite.cinema.models.enums.ScreeningsTranslationType;

public record ScreeningTime(ULong id, ULong roomId, LocalDateTime time, ULong movieId, Integer duration,
        ScreeningsDisplayType displayType, ScreeningsTranslationType translationType) {}