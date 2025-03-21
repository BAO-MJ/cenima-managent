package com.elite.cinema.controllers;

import com.elite.cinema.db.DbSet;
import com.elite.cinema.models.tables.pojos.Movies;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import static com.elite.cinema.models.Tables.MOVIES;

public class AvailableMoviesController extends MainController implements Initializable {

    @FXML
    private Button nowShowingButton;

    @FXML
    private Button comingSoonButton;

    @FXML
    private FlowPane moviesFlowPane;

    private static final String SELECTED_BUTTON_STYLE = "-fx-background-color: #ff4b2b;";
    private static final String UNSELECTED_BUTTON_STYLE = "";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set default selection to "Now Showing"
        nowShowingButton.setStyle(SELECTED_BUTTON_STYLE);
        showNowShowingMovies();
    }

    @FXML
    public void showNowShowingMovies() {
        // Update button styles
        nowShowingButton.setStyle(SELECTED_BUTTON_STYLE);
        comingSoonButton.setStyle(UNSELECTED_BUTTON_STYLE);

        // Clear current movies
        moviesFlowPane.getChildren().clear();

        // Get now showing movies (movies with screenings on or after today)
        List<Movies> nowShowingMovies = DbSet.getContext()
                .selectFrom(MOVIES)
                .where(MOVIES.RELEASE_DATE.le(LocalDate.now()).and(MOVIES.END_DATE.ge(LocalDate.now())))
                .orderBy(MOVIES.TITLE)
                .fetchInto(Movies.class);

        // Display movies
        displayMovies(nowShowingMovies);
    }

    @FXML
    public void showComingSoonMovies() {
        // Update button styles
        nowShowingButton.setStyle(UNSELECTED_BUTTON_STYLE);
        comingSoonButton.setStyle(SELECTED_BUTTON_STYLE);

        // Clear current movies
        moviesFlowPane.getChildren().clear();

        // Get coming soon movies (future release date with no current screenings)
        List<Movies> comingSoonMovies = DbSet.getContext()
                .select(MOVIES.asterisk())
                .from(MOVIES)
                .where(MOVIES.RELEASE_DATE.gt(LocalDate.now()))
                .orderBy(MOVIES.TITLE)
                .fetchInto(Movies.class);

        // Display movies
        displayMovies(comingSoonMovies);
    }

    private void displayMovies(List<Movies> movies) {
        for (Movies movie : movies) {
            try {
                var loader = new FXMLLoader(getClass().getResource("/com/elite/cinema/user/movie.fxml"));
                // Load movie card from FXML
                VBox movieCard = loader.load();
                UserMovieController controller = loader.getController();
                controller.setMovie(movie);

                // Add click handler to navigate to details
                movieCard.setOnMouseClicked(_ -> openMovieDetails(movie));

                // Add movie card to the flow pane
                moviesFlowPane.getChildren().add(movieCard);
            } catch (Exception e) {
                System.err.println("Error loading movie card: " + e.getMessage());
            }
        }
    }

    private void openMovieDetails(Movies movie) {
//         Navigate to movie details page with the selected movie ID
        changeScene.accept("user/movie-details.fxml", movie);
    }
}