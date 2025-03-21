package com.elite.cinema.ui;

import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import lombok.Getter;

import java.time.LocalDate;

import org.jooq.types.ULong;

import com.elite.cinema.db.DbSet;
import com.elite.cinema.models.enums.ScreeningsDisplayType;
import com.elite.cinema.models.enums.ScreeningsTranslationType;
import com.elite.cinema.models.tables.pojos.Screenings;

import static com.elite.cinema.models.Tables.RESERVATIONS;

/**
 * Represents a panel showing one hour of screening times with buttons for every
 * 5-minute interval
 */
public class ScreeningHourPanel extends VBox {

    /**
     * -- GETTER -- Gets the hour represented by this panel
     */
    @Getter
    private final int hour;
    private Runnable action;
    /**
     * -- GETTER -- Gets all time buttons in this panel
     */
    @Getter
    private final Button[] timeButtons = new Button[60 / 5];

    /**
     * Creates a screening hour panel for the specified hour
     * 
     * @param hour The hour (0-23) to display
     */
    public ScreeningHourPanel(int hour) {
        this.hour = hour;

        // Configure VBox
        setPrefWidth(270.0);
        setSpacing(5.0);

        // Create title label
        Label titleLabel = new Label(String.format("%1$02d:00 - %1$02d:55", hour));
        titleLabel.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 18.0));

        // Create separator
        Separator separator = new Separator();

        // Create button container
        FlowPane buttonContainer = new FlowPane();
        buttonContainer.setPrefWrapLength(270.0);
        buttonContainer.setHgap(10.0);
        buttonContainer.setVgap(5.0);

        // Create buttons for each 5-minute interval
        for (int minute = 0; minute < 60; minute += 5) {
            String timeText = String.format("%02d:%02d", hour, minute);
            Button timeButton = new Button(timeText);
            timeButtons[minute / 5] = timeButton;
            setUnavailable(minute);
        }

        buttonContainer.getChildren().setAll(timeButtons);

        // Add all components to this VBox
        getChildren().setAll(titleLabel, separator, buttonContainer);
    }

    public void reset() {
        for (int minute = 0; minute < 60; minute += 5) {
            setUnavailable(minute);
        }
    }

    /**
     * Gets the button for a specific time
     * 
     * @param minute The minute (must be a multiple of 5)
     * @return The button for the specified time
     */
    public Button getButtonForTime(int minute) {
        if (minute < 0 || minute >= 60 || minute % 5 != 0) {
            throw new IllegalArgumentException("Minute must be a multiple of 5 between 0 and 55");
        }
        return timeButtons[minute / 5];
    }

    public void setAvailable(int minute, ULong movieId, ULong roomId, LocalDate screeningDate, ScreeningsDisplayType displayType, ScreeningsTranslationType translationType) {
        Button button = getButtonForTime(minute);
        button.setDisable(false);
        button.setStyle("-fx-background-color: #00a000");
        button.setOnAction(_ -> {
            DbSet.screenings().insert(new Screenings(null, movieId, roomId, screeningDate.atTime(hour, minute), displayType, translationType));
            var movieName = DbSet.movies().fetchOneById(movieId).getTitle();
            var roomName = DbSet.screeningRooms().fetchOneById(roomId).getName();
            if (action != null) {
                action.run();
            }
            showAlert("Screening Created", String.format("New screening created successfully for movie \"%s\" in %s at %02d:%02d", movieName, roomName, hour, minute));
        });
    }

    public void setSelected(int minute, ULong screeningId) {
        Button button = getButtonForTime(minute);
        button.setDisable(false);
        button.setStyle("-fx-background-color: #FFA500");
        button.setOnAction(_ -> {
            if (DbSet.getContext().fetchExists(RESERVATIONS, RESERVATIONS.SCREENING_ID.eq(screeningId)))
            {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Reservation Exists");
                alert.setHeaderText(null);
                alert.setContentText("There is already at least a reservation with this screening, deleting this screening time would delete those reservations. Do you want to continue?");

                ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
                ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);

                alert.getButtonTypes().setAll(noButton, yesButton);
                alert.initOwner(this.getScene().getWindow());
                alert.showAndWait();

                if (alert.getResult().getButtonData() == ButtonBar.ButtonData.NO) {
                    return;
                }
            }

            var movieName = DbSet.movies().fetchOneById(DbSet.screenings().fetchOneById(screeningId).getMovieId()).getTitle();
            DbSet.screenings().deleteById(screeningId);
            if (action != null) {
                action.run();
            }
            showAlert("Screening Removed", String.format("Removed screening of %s at %02d:%02d", movieName, hour, minute));
        });
    }

    public void setUnavailable(int minute) {
        Button button = getButtonForTime(minute);
        button.setDisable(true);
        button.setStyle("-fx-background-color: #808080");
        button.setOnAction(null);
    }

    public void setOnAction(Runnable action) {
        this.action = action;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}