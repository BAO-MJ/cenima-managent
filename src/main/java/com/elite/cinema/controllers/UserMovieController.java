package com.elite.cinema.controllers;

import com.elite.cinema.models.tables.pojos.Movies;
import com.elite.cinema.utils.ImageHelper;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class UserMovieController {
    @FXML
    private ImageView moviePoster;

    @FXML
    private ImageView movieRating;

    @FXML
    private Label movieTitle;

    @FXML
    private Label movieDuration;

    @FXML
    private Label moviePremiereDate;

    public void initialize() {
        moviePoster.imageProperty().addListener((_, _, _) -> ImageHelper.centerImage(moviePoster));
    }

    public void setMovie(Movies movie) {
        movieTitle.setText(movie.getTitle());
        movieDuration.setText(String.valueOf(movie.getDuration()));
        if (movie.getPoster() != null) {
            moviePoster.setImage(ImageHelper.byteArrayToImage(movie.getPoster()));
        }
    }
}
