package com.elite.cinema;

import java.time.LocalDateTime;

import org.jooq.types.ULong;
import org.jooq.types.UShort;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ScreeningInfo {
    private ULong id;
    private ULong movieId;
    private String movieTitle;
    private UShort movieDuration;
    private ULong roomId;
    private String roomName;
    private LocalDateTime screeningTime;
    private String displayType;
    private String translationType;

    public LocalDateTime getEndTime() {
        return screeningTime.plusMinutes(movieDuration.intValue());
    }
}
