package com.elite.cinema.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;

/**
 * Custom ToggleButton that represents a selectable date with day of week and date display
 */
public class DateToggleButton extends ToggleButton {

    private final Label weekDayLabel;
    private final Label dayLabel;

    @Getter
    private LocalDate date;

    public DateToggleButton() {
        // Configure the toggle button
        setMaxWidth(Double.MAX_VALUE);
        setMnemonicParsing(false);

        setPrefHeight(60.0);
        setPrefWidth(60.0);

        getStyleClass().add("date");
        getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/elite/cinema/user/choose-date.css")).toExternalForm());

        // Create container
        VBox container = new VBox();
        container.setAlignment(Pos.TOP_CENTER);

        // Create date label (day of week)
        weekDayLabel = new Label();

        // Create day label (date display)
        dayLabel = new Label();
        dayLabel.setId("day");
        dayLabel.setAlignment(Pos.CENTER);
        dayLabel.setMaxHeight(Double.MAX_VALUE);
        dayLabel.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(dayLabel, Priority.ALWAYS);

        // Add labels to container
        container.getChildren().addAll(weekDayLabel, dayLabel);

        // Set container as graphic
        setGraphic(container);
    }

    public void setDate(LocalDate date) {
        this.date = date;
        dayLabel.setText(String.valueOf(date.getDayOfMonth()));
        weekDayLabel.setText(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
    }
}