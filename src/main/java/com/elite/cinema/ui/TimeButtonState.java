package com.elite.cinema.ui;

public enum TimeButtonState {
    AVAILABLE, // can be chosen
    SELECTED, // has been chosen, but can be unselected
    LOCKED, // has been chosen and cannot be unselected
    INTERMISSION, // time between each screening (30 minutes)
    OTHER_SCREENING, // time is not available because of other screening
    UNAVAILABLE; // time is not available
}
