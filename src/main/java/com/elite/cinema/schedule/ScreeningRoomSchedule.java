package com.elite.cinema.schedule;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.jooq.types.ULong;

import com.elite.cinema.models.enums.ScreeningsDisplayType;
import com.elite.cinema.models.enums.ScreeningsTranslationType;
import com.elite.cinema.schedule.Range.Type;

import javafx.util.Pair;

public class ScreeningRoomSchedule {
    private final List<Pair<Range, ScreeningTime>> schedule;
    private ScreeningTime previousDateScreening;

    public ScreeningRoomSchedule(LocalDate date, List<ScreeningTime> screenings) {
        schedule = new ArrayList<>(); // 5 minute intervals

        screenings.sort(Comparator.comparing(ScreeningTime::time));
        for (ScreeningTime screening : screenings) {
            if (screening.id() == null)
                continue;

            int startTime = (int)Duration.between(date.atStartOfDay(), screening.time()).toMinutes() / 5;
            int endTime = startTime + screening.duration() / 5;

            if (startTime < 0) {
                previousDateScreening = screening;
            }

            schedule.add(new Pair<>(
                    new Range(Type.SCREENING, Math.max(0, startTime), Math.min(endTime - Math.max(0, startTime), 24 * 12)),
                    screening));

            schedule.add(new Pair<>(new Range(Type.INTERMISSION, endTime, 6), null));
        }
    }

    public List<Pair<ULong, Integer>> getScheduleByMovie(ULong movieId, ScreeningsDisplayType displayType, ScreeningsTranslationType translationType) {
        return schedule.stream()
                .filter(pair -> pair.getKey().type() == Type.SCREENING &&
                        pair.getValue().movieId().equals(movieId) &&
                        pair.getValue().displayType() == displayType && pair.getValue().translationType() == translationType &&
                        (previousDateScreening == null || pair.getKey().start() != 0 || !pair.getValue().equals(previousDateScreening))
                )
                .map(pair -> new Pair<>(pair.getValue().id(), pair.getKey().start())).toList();
    }

    public List<Range> getEmptyTimes() {
        List<Range> vacantTimes = new ArrayList<>();
        if (schedule.isEmpty()) {
            vacantTimes.add(new Range(Type.EMPTY, 0, 24 * 12));
        }
        else {
            if (schedule.getFirst().getKey().start() > 0) {
                vacantTimes.add(new Range(Type.EMPTY, 0, schedule.getFirst().getKey().start()));
            }

            for (int i = 0; i < schedule.size() - 1; i++) {
                vacantTimes.add(new Range(Type.EMPTY, schedule.get(i).getKey().end(),
                        schedule.get(i + 1).getKey().start() - schedule.get(i).getKey().end()));
            }

            if (schedule.getLast().getKey().end() < 24 * 12) {
                vacantTimes.add(new Range(Type.EMPTY, schedule.getLast().getKey().end(),
                        24 * 12 - schedule.getLast().getKey().end()));
            }
        }

        return vacantTimes;
    }
}
