package com.elite.cinema.controllers;

import com.elite.cinema.db.DbSet;
import com.elite.cinema.models.enums.MoviesRating;
import com.elite.cinema.models.tables.pojos.Movies;
import com.elite.cinema.utils.DateHelper;
import com.elite.cinema.utils.ImageHelper;

import atlantafx.base.theme.Styles;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDate;

import org.jooq.types.UShort;

public class MovieDialogController {

    @FXML
    private ImageView posterImageView;

    @FXML
    private Button uploadPosterButton;

    @FXML
    private TextField titleField;

    @FXML
    private Spinner<Integer> durationField;

    @FXML
    private ComboBox<MoviesRating> ratingComboBox;

    @FXML
    private TextField genreField;

    @FXML
    private TextField directorField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ToggleButton foreignLanguageButton;

    @FXML
    private ToggleButton nativeLanguageButton;

    @FXML
    private DatePicker releaseDatePicker, endDatePicker;

    private final ToggleGroup languageGroup = new ToggleGroup();

    private File selectedPosterFile;

    private Movies movieRecord = new Movies();

    @FXML
    private void initialize() {
        ratingComboBox.getItems().addAll(MoviesRating.values());
        ratingComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(MoviesRating rating) { return rating != null ? rating.getLiteral() : ""; }

            @Override
            public MoviesRating fromString(String string) { return MoviesRating.lookupLiteral(string); }
        });
        ratingComboBox.setValue(MoviesRating.P);

        durationField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Short.MAX_VALUE));
        durationField.getEditor().textProperty().addListener((_, _, newValue) -> {
            if (!newValue.matches("\\d*")) {
                durationField.getEditor().setText(newValue.replaceAll("\\D", ""));
            }
        });

        posterImageView.imageProperty().addListener((_, _, _) -> ImageHelper.centerImage(posterImageView));

        foreignLanguageButton.getStyleClass().add(Styles.LEFT_PILL);
        nativeLanguageButton.getStyleClass().add(Styles.RIGHT_PILL);

        foreignLanguageButton.setToggleGroup(languageGroup);
        nativeLanguageButton.setToggleGroup(languageGroup);
        languageGroup.selectToggle(foreignLanguageButton);

        languageGroup.selectedToggleProperty().addListener((_, oldVal, newVal) -> {
            if (newVal == null)
                oldVal.setSelected(true);
        });

        foreignLanguageButton.setUserData(false);
        nativeLanguageButton.setUserData(true);

        releaseDatePicker.setDayCellFactory(_ -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isBefore(LocalDate.now().plusDays(1)));
            }
        });

        releaseDatePicker.setValue(LocalDate.now().plusDays(1));
        endDatePicker.setValue(releaseDatePicker.getValue());

        releaseDatePicker.setOnAction(_ -> {
            if (releaseDatePicker.getValue() != null && releaseDatePicker.getValue().isAfter(endDatePicker.getValue())) {
                endDatePicker.setValue(releaseDatePicker.getValue());
            }
            endDatePicker.setDayCellFactory(_ -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(date.isBefore(DateHelper.maxDate(LocalDate.now(), releaseDatePicker.getValue())));
                }
            });
        });

        endDatePicker.setDayCellFactory(_ -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isBefore(DateHelper.maxDate(LocalDate.now(), releaseDatePicker.getValue())));
            }
        });
    }

    @SneakyThrows
    public void setMovieRecord(Movies movie) {
        movieRecord = new Movies(movie);

        titleField.setText(movieRecord.getTitle());
        durationField.getValueFactory().setValue(movieRecord.getDuration().intValue());
        ratingComboBox.setValue(movieRecord.getRating());
        languageGroup.selectToggle(movieRecord.getNativeLanguage() ? nativeLanguageButton : foreignLanguageButton);

        if (movieRecord.getGenre() != null) {
            genreField.setText(movieRecord.getGenre());
        }

        if (movieRecord.getDirector() != null) {
            directorField.setText(movieRecord.getDirector());
        }

        if (movieRecord.getDescription() != null) {
            descriptionArea.setText(movieRecord.getDescription());
        }

        if (movieRecord.getPoster() != null) {
            posterImageView.setImage(ImageHelper.byteArrayToImage(movieRecord.getPoster()));
        }

        releaseDatePicker.setValue(movieRecord.getReleaseDate());
        releaseDatePicker.setDisable(Duration.between(LocalDate.now().atStartOfDay(), movieRecord.getReleaseDate().atStartOfDay()).toDays() < 14);
        endDatePicker.setValue(movieRecord.getEndDate());
        endDatePicker.setDayCellFactory(_ -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isBefore(DateHelper.maxDate(LocalDate.now().plusDays(14), releaseDatePicker.getValue())));
            }
        });
    }

    public void handlePosterUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Movie Poster");
        fileChooser.getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        Stage stage = (Stage)uploadPosterButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());
                posterImageView.setImage(image);
                selectedPosterFile = selectedFile;
            }
            catch (Exception e) {
                showError("Error loading image: " + e.getMessage());
            }
        }
    }

    public void updateMovie() {
        movieRecord.setTitle(titleField.getText());
        movieRecord.setDuration(UShort.valueOf(durationField.getValue()));
        movieRecord.setRating(ratingComboBox.getValue());
        movieRecord.setNativeLanguage((Boolean)languageGroup.getSelectedToggle().getUserData());
        movieRecord.setGenre(genreField.getText());
        movieRecord.setDirector(directorField.getText());
        movieRecord.setDescription(descriptionArea.getText());
        movieRecord.setReleaseDate(releaseDatePicker.getValue());
        movieRecord.setEndDate(endDatePicker.getValue());

        if (selectedPosterFile != null) {
            try {
                byte[] poster = Files.readAllBytes(selectedPosterFile.toPath());
                movieRecord.setPoster(poster);
            }
            catch (Exception e) {
                showError("Error reading image: " + e.getMessage());
            }
        }
    }

    private boolean validateInput() {
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            showError("Title must be set.");
            return false;
        }
        if (durationField.getValue() == null || durationField.getValue() <= 0) {
            showError("Duration must be set.");
            return false;
        }
        if (ratingComboBox.getValue() == null) {
            showError("Rating must be set.");
            return false;
        }
        if (releaseDatePicker.getValue() == null) {
            showError("Release date must be set.");
            return false;
        }
        if (endDatePicker.getValue() == null) {
            showError("End date must be set.");
            return false;
        }
        return true;
    }

    public void onCancel() {
        var stage = (Stage)uploadPosterButton.getScene().getWindow();
        stage.close();
    }

    public void onSave() {
        if (!validateInput()) {
            return;
        }

        updateMovie();
        DbSet.movies().merge(movieRecord);

        var stage = (Stage)uploadPosterButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}