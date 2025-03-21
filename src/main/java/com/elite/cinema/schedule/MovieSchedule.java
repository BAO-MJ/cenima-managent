package com.elite.cinema.schedule;

import java.time.LocalTime;
import java.util.SortedSet;
import java.util.TreeSet;

public class MovieSchedule {
    public SortedSet<LocalTime> subtitle2D = new TreeSet<>();
    public SortedSet<LocalTime> subtitle3D = new TreeSet<>();
    public SortedSet<LocalTime> dubbing2D = new TreeSet<>();
    public SortedSet<LocalTime> dubbing3D = new TreeSet<>();
}
