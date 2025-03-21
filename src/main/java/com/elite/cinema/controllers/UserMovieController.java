package com.elite.cinema.controllers;

import com.elite.cinema.models.tables.pojos.Movies;
import com.elite.cinema.utils.ImageHelper;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

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
        movieDuration.setText(movie.getDuration() + " min");
        moviePremiereDate.setText(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(movie.getReleaseDate()));

        var ratingSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/image/rating-sprite.png")));
        movieRating.setImage(ratingSprite);

        movieRating.setViewport(switch (movie.getRating()) {
            case P -> new Rectangle2D(0, 0, 44, 28);
            case T13 -> new Rectangle2D(47, 0, 44, 28);
            case T16 -> new Rectangle2D(94, 0, 44, 28);
            case T18 -> new Rectangle2D(141, 0, 44, 28);
            case K -> new Rectangle2D(187, 0, 44, 28);
            case C -> new Rectangle2D(235, 0, 44, 28);
        });

        if (movie.getPoster() != null) {
            moviePoster.setImage(ImageHelper.byteArrayToImage(movie.getPoster()));
        }
    }
}
