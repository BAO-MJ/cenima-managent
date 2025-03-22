package com.elite.cinema.controllers.admin;

import java.util.Objects;
import java.util.function.Predicate;

import atlantafx.base.theme.Styles;
import com.elite.cinema.controllers.MainController;
import com.elite.cinema.models.enums.MoviesRating;
import com.elite.cinema.utils.ComboBoxHelper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;

import com.elite.cinema.models.tables.pojos.Movies;
import com.elite.cinema.utils.ImageHelper;
import com.elite.cinema.db.DbSet;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.SneakyThrows;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

public class MoviesController extends MainController
{
    @FXML
    private TableView<Movies> moviesTable;
    @FXML
    private ImageView moviePoster;
    @FXML
    private Label movieTitle, movieDuration, movieGenre, movieDirector, movieRating;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<MoviesRating> filterComboBox;
    @FXML
    private Button addButton, refreshButton, editButton, deleteButton;

    private Movies selectedMovie;
    private final ObservableList<Movies> movies = FXCollections.observableArrayList();

    public void initialize() {
        setupTable();
        setupFilters();
        setupButtons();
    }

    private void setupTable() {
        var columns = moviesTable.getColumns();
        columns.get(0).setCellValueFactory(new PropertyValueFactory<>("title"));
        columns.get(1).setCellValueFactory(new PropertyValueFactory<>("duration"));
        columns.get(2).setCellValueFactory(new PropertyValueFactory<>("rating"));
        columns.get(3).setCellValueFactory(new PropertyValueFactory<>("releaseDate"));
        columns.get(4).setCellValueFactory(new PropertyValueFactory<>("endDate"));

        moviesTable.getSelectionModel().selectedItemProperty().addListener((_, _, newVal) -> {
            editButton.setDisable(newVal == null);
            deleteButton.setDisable(newVal == null);

            selectedMovie = newVal;
            displayMovieDetails(newVal);
        });

        movies.addListener((ListChangeListener<? super Movies>) _ -> applyFilters());

        moviesTable.setRowFactory(_ -> {
            TableRow<Movies> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && !row.isEmpty()) {
                    if (event.getClickCount() == 2) {
                        Movies movie = row.getItem();
                        if (movie.getId() == null) {
                            openMovieDialog(null);
                        } else {
                            openMovieDialog(movie);
                        }
                        fetchMovies();
                    }
                }
            });
            return row;
        });
    }

    private void setupFilters() {
        // Setup rating filter
        filterComboBox.setItems(FXCollections.observableArrayList(MoviesRating.values()));
        filterComboBox.setConverter(ComboBoxHelper.getStringConverter(MoviesRating::getLiteral, "All Ratings"));

        // Add "All ratings" option
        filterComboBox.getItems().addFirst(null);
        filterComboBox.getSelectionModel().selectFirst();

        // Setup search and filter predicates
        filterComboBox.valueProperty().addListener(_ -> applyFilters());
        searchField.textProperty().addListener(_ -> applyFilters());
    }

    private void setupButtons() {
        editButton.setDisable(true);
        deleteButton.setDisable(true);

        addButton.getStyleClass().add(Styles.SUCCESS);
        refreshButton.getStyleClass().addAll(Styles.ACCENT, Styles.FLAT);
        editButton.getStyleClass().add(Styles.ACCENT);
        deleteButton.getStyleClass().add(Styles.DANGER);
    }

    private void applyFilters() {
        moviesTable.setItems(movies.filtered(createFilterPredicate()));
    }

    private Predicate<Movies> createFilterPredicate() {
        String searchText = searchField.getText().toLowerCase();
        MoviesRating selectedRating = filterComboBox.getValue();

        return movie -> {
            boolean matchesSearch = StringUtils.isEmpty(searchText) ||
                    (movie.getTitle() != null && movie.getTitle().toLowerCase().contains(searchText)) ||
                    (movie.getGenre() != null && movie.getGenre().toLowerCase().contains(searchText)) ||
                    (movie.getDirector() != null && movie.getDirector().toLowerCase().contains(searchText));

            boolean matchesRating = selectedRating == null || selectedRating.equals(movie.getRating());

            return matchesSearch && matchesRating;
        };
    }

    private void displayMovieDetails(Movies movie) {
        if (movie == null) {
            movie = new Movies();
        }

        if (movie.getPoster() != null) {
            moviePoster.setImage(ImageHelper.byteArrayToImage(movie.getPoster()));
        } else {
            moviePoster.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/image/default-poster.png"))));
        }

        movieTitle.setText(movie.getTitle());
        movieDuration.setText(String.format("Duration: %s minutes",
                movie.getDuration() == null ? "???" : movie.getDuration().toString()));
        movieGenre.setText(String.format("Genre: %s",
                StringUtils.isNotBlank(movie.getGenre()) ? movie.getGenre() : "???"));
        movieDirector.setText(String.format("Director: %s",
                StringUtils.isNotBlank(movie.getDirector()) ? movie.getDirector() : "???"));
        movieRating.setText(String.format("Rating: %s",
                movie.getRating() == null ? "???" : movie.getRating().getLiteral()));
    }

    @Override
    public void ready(Object params) {
        fetchMovies();
    }

    @FXML
    public void fetchMovies() {
        // Update filtered list source
        movies.setAll(DbSet.movies().findAll());

        // Clear selection and details panel
        moviesTable.getSelectionModel().clearSelection();
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    @FXML
    @SneakyThrows
    public void openMovieDialog(Movies movie) {
        Stage newWindow = new Stage();
        newWindow.initModality(Modality.APPLICATION_MODAL);
        var loader = new FXMLLoader(getClass().getResource("/com/elite/cinema/test.fxml"));
        Pane movieDialog = loader.load();

        Scene scene = new Scene(movieDialog, 650, 500);
        newWindow.setScene(scene);
        newWindow.setTitle(movie == null ? "Add New Movie" : "Edit Movie");

        if (movie != null && movie.getId() != null) {
            MovieDialogController controller = loader.getController();
            controller.setMovieRecord(movie);
        }

        newWindow.showAndWait();
        fetchMovies();
    }

    @FXML
    public void onEditMovie() {
        if (selectedMovie != null && selectedMovie.getId() != null) {
            openMovieDialog(selectedMovie);
            fetchMovies();
        }
    }

    @FXML
    public void onDeleteMovie() {
        if (selectedMovie != null && selectedMovie.getId() != null) {
            Alert confirmDelete = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDelete.setTitle("Confirm Delete");
            confirmDelete.setHeaderText(null);
            confirmDelete.setContentText("Are you sure you want to delete the movie: " + selectedMovie.getTitle() + "?");

            if (confirmDelete.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                DbSet.movies().delete(selectedMovie);
                fetchMovies();
            }
        }
    }

    public void addNewMovie()
    {
        openMovieDialog(null);
    }
}