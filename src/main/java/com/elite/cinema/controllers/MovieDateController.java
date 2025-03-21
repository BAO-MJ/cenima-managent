package com.elite.cinema.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MovieDateController {
    @FXML
    private Label date;

    @FXML
    private Label day;

    public void setDate(String date) {
        this.date.setText(date);
    }

    public void setDay(String day) {
        this.day.setText(day);
    }
}
