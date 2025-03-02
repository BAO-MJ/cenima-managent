package com.elite.cinema;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AvailableMovie extends RecursiveTreeObject<AvailableMovie> {
    private final StringProperty title;
    private final StringProperty genre;
    private final StringProperty showingDate;

    public AvailableMovie(String title, String genre, String showingDate) {
        this.title = new SimpleStringProperty(title);
        this.genre = new SimpleStringProperty(genre);
        this.showingDate = new SimpleStringProperty(showingDate);
    }

    public StringProperty titleProperty() { return title; }

    public StringProperty genreProperty() { return genre; }

    public StringProperty showingDateProperty() { return showingDate; }
}
