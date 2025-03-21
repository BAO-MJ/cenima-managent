package com.elite.cinema.schedule;

public record Range(Type type, int start, int duration) {
    public int end() {
        return start + duration;
    }

    public enum Type {
        SCREENING,
        INTERMISSION,
        EMPTY
    }
}
