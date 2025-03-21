package com.elite.cinema.controllers;

import static com.elite.cinema.models.Tables.SCREENINGS;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.elite.cinema.models.enums.MoviesRating;
import com.elite.cinema.utils.ImageHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import lombok.Setter;

import com.elite.cinema.db.DbSet;
import com.elite.cinema.models.enums.ScreeningsDisplayType;
import com.elite.cinema.models.enums.ScreeningsTranslationType;
import com.elite.cinema.models.tables.pojos.Movies;
import com.elite.cinema.models.tables.pojos.Screenings;
import com.elite.cinema.schedule.MovieSchedule;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import org.kordamp.ikonli.javafx.FontIcon;

public class MovieDetailsController extends MainController {

    @FXML
    private Button bookTicketButton;

    @FXML
    private Label title;

    @FXML
    private Text director, genre, releaseDate, duration, language, contentRating;

    @FXML
    private Label description;

    @FXML
    private ImageView moviePoster;

    private Pair<Stage, ChooseDateController> chooseDateDialog;

    @FXML
    private FontIcon backButton;

    @Setter
    private Movies movie;

    private double dialogXOffset = 0;
    private double dialogYOffset = 0;

    public void initialize() {
        try {
            var loader = new FXMLLoader(getClass().getResource("/com/elite/cinema/user/choose-date.fxml"));
            Parent root = loader.load();
            ChooseDateController controller = loader.getController();

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);

            root.setOnMousePressed(event -> {
                dialogXOffset = event.getSceneX();
                dialogYOffset = event.getSceneY();
            });
            root.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - dialogXOffset);
                stage.setY(event.getScreenY() - dialogYOffset);
            });

            var scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.initStyle(StageStyle.TRANSPARENT);

            stage.setScene(scene);
            chooseDateDialog = new Pair<>(stage, controller);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        backButton.setIconSize(32);
        backButton.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                changeScene.accept("user/available-movies.fxml", null);
            }
        });
    }

    public void onBookTicket() {
        var ctx = DbSet.getContext();
        var screenings = ctx.selectFrom(SCREENINGS)
                .where(SCREENINGS.SCREENING_TIME
                        .between(LocalDateTime.now(), LocalDateTime.of(LocalDate.now().plusDays(14), LocalTime.MAX))
                        .and(SCREENINGS.MOVIE_ID.eq(movie.getId())))
                .fetchInto(Screenings.class)
                .stream()
                .collect(Collectors.groupingBy(v -> v.getScreeningTime().toLocalDate()));

        var schedules = screenings.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            var v = entry.getValue();
            var schedule = new MovieSchedule();
            for (var s : v) {
                var time = s.getScreeningTime().toLocalTime();
                SortedSet<LocalTime> set;
                if (s.getDisplayType() == ScreeningsDisplayType._2D
                        && s.getTranslationType() == ScreeningsTranslationType.Subtitle) {
                    set = schedule.subtitle2D;
                }
                else if (s.getDisplayType() == ScreeningsDisplayType._2D
                        && s.getTranslationType() == ScreeningsTranslationType.Dubbing) {
                    set = schedule.dubbing2D;
                }
                else if (s.getDisplayType() == ScreeningsDisplayType._3D
                        && s.getTranslationType() == ScreeningsTranslationType.Subtitle) {
                    set = schedule.subtitle3D;
                }
                else if (s.getDisplayType() == ScreeningsDisplayType._3D
                        && s.getTranslationType() == ScreeningsTranslationType.Dubbing) {
                    set = schedule.dubbing3D;
                }
                else {
                    throw new IllegalStateException("Unknown screening type");
                }

                set.add(time);
            }

            return schedule;
        }, (a, _) -> a, TreeMap::new));

        chooseDateDialog.getValue().setSchedule(schedules);
        chooseDateDialog.getKey().showAndWait();
        var result = chooseDateDialog.getValue().getResult();

        if (result != null) {
            var screening = DbSet.getContext().selectFrom(SCREENINGS).where(
                SCREENINGS.MOVIE_ID.eq(movie.getId()).and(
                SCREENINGS.SCREENING_TIME.eq(result.date()))
                .and(SCREENINGS.DISPLAY_TYPE.eq(result.display()))
                .and(SCREENINGS.TRANSLATION_TYPE.eq(result.translation()))
            ).fetchOneInto(Screenings.class);

            changeScene.accept("user/booking-ticket.fxml", screening);
        }
    }

    private final HashMap<MoviesRating, String> ratingMap = new HashMap<>() {{
        put(MoviesRating.P, "P - Suitable for general viewing.");
        put(MoviesRating.K, "K - Viewers under 13 years old are admitted provided that they are with their parents or guardians.");
        put(MoviesRating.T13, "T13 - Viewers under age 13 are not admitted.");
        put(MoviesRating.T16, "T16 - Viewers under age 16 are not admitted.");
        put(MoviesRating.T18, "T18 - Viewers under age 18 are not admitted.");
        put(MoviesRating.C, "C - Prohibited");
    }};

    @Override
    public void ready(Object params)
    {
        this.movie = (Movies)params;
        if (movie == null)
        {
            return;
        }

        bookTicketButton.setVisible(movie.getReleaseDate().isBefore(LocalDate.now()));
        if (movie.getPoster() != null)
        {
            moviePoster.setImage(ImageHelper.byteArrayToImage(movie.getPoster()));
        }
        else
        {
            moviePoster.setImage(new Image(Objects.requireNonNull(getClass().getResource("/image/default-poster.png")).toString()));
        }
        ImageHelper.centerImage(moviePoster);
        title.setText(movie.getTitle());
        director.setText(movie.getDirector());
        genre.setText(movie.getGenre());
        releaseDate.setText(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(movie.getReleaseDate()));
        duration.setText(movie.getDuration().toString() + " minutes");
        language.setText(movie.getNativeLanguage() ? "Vietnamese" : "Foreign");
        contentRating.setText(ratingMap.get(movie.getRating()));
        description.setText(movie.getDescription());
    }
}
