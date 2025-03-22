package com.elite.cinema.controllers.user;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.elite.cinema.MovieScreening;
import com.elite.cinema.models.enums.ScreeningsDisplayType;
import com.elite.cinema.models.enums.ScreeningsTranslationType;
import com.elite.cinema.schedule.MovieSchedule;

import com.elite.cinema.ui.DateToggleButton;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import org.kordamp.ikonli.javafx.FontIcon;

public class ChooseDateController {
    @FXML
    private FontIcon closeButton;

    private LocalDate startDate;
    private boolean nextWeek;
    private boolean nextClicked;

    private final MovieSchedule[] schedule = new MovieSchedule[14];

    private final ToggleGroup typeGroup = new ToggleGroup();

    @FXML
    private ToggleButton subtitle2D;

    @FXML
    private ToggleButton subtitle3D;

    @FXML
    private ToggleButton dubbing2D;

    @FXML
    private ToggleButton dubbing3D;

    @FXML
    private GridPane dates;

    private final DateToggleButton[] dateButtons = new DateToggleButton[7];

    private final ToggleGroup dateGroup = new ToggleGroup();

    @FXML
    private FlowPane times;

    private final ToggleGroup timeGroup = new ToggleGroup();

    @FXML
    private Button nextButton;

    public void initialize() {
        startDate = LocalDate.now();
        nextClicked = false;

        closeButton.setIconSize(36);

        dates.getChildren().clear();
        for (int i = 0; i < 7; i++) {
            dateButtons[i] = new DateToggleButton();
            dateButtons[i].setOnAction(_ -> setTimes());

            dateButtons[i].setMinSize(60, 60);
            dateButtons[i].setMaxWidth(Double.MAX_VALUE);
            dates.add(dateButtons[i], i, 0);
        }

        dateGroup.getToggles().setAll(dateButtons);

        times.getChildren().clear();

        typeGroup.getToggles().setAll(subtitle2D, subtitle3D, dubbing2D, dubbing3D);
        typeGroup.selectToggle(subtitle2D);

        typeGroup.selectedToggleProperty().addListener((_, oldVal, newVal) -> {
            if (newVal == null) {
                oldVal.setSelected(true);
            }
            else {
                setTimes();
            }
        });

        dateGroup.selectedToggleProperty().addListener((_, oldVal, newVal) -> {
            if (newVal == null) {
                oldVal.setSelected(true);
            }
            else {
                setTimes();
            }
        });

        timeGroup.selectedToggleProperty().addListener((_, oldVal, newVal) -> {
            if (newVal == null)
                oldVal.setSelected(true);
        });

        subtitle2D.setUserData(new Pair<>(ScreeningsDisplayType._2D, ScreeningsTranslationType.Subtitle));
        dubbing2D.setUserData(new Pair<>(ScreeningsDisplayType._2D, ScreeningsTranslationType.Dubbing));
        subtitle3D.setUserData(new Pair<>(ScreeningsDisplayType._3D, ScreeningsTranslationType.Subtitle));
        dubbing3D.setUserData(new Pair<>(ScreeningsDisplayType._3D, ScreeningsTranslationType.Dubbing));

        nextButton.setOnAction(_ -> {
            nextClicked = true;
            onCloseClicked();
        });
    }

    public void setSchedule(SortedMap<LocalDate, MovieSchedule> schedule) {
        nextClicked = false;
        for (int i = 0; i < 14; i++) {
            this.schedule[i] = schedule.getOrDefault(startDate.plusDays(i), new MovieSchedule());
        }

        setDates(false);
    }

    public MovieScreening getResult() {
        if (!nextClicked) {
            return null;
        }

        var date = ((DateToggleButton)dateGroup.getSelectedToggle()).getDate();
        var time = (LocalTime)timeGroup.getSelectedToggle().getUserData();

        @SuppressWarnings("unchecked")
        var selectedType = (Pair<ScreeningsDisplayType, ScreeningsTranslationType>)typeGroup.getSelectedToggle()
                .getUserData();

        return new MovieScreening(LocalDateTime.of(date, time), selectedType.getKey(), selectedType.getValue());
    }

    public void setDates(boolean nextWeek) {
        this.nextWeek = nextWeek;
        int startOffset = nextWeek ? 7 : 0;

        for (int i = 0; i < 7; i++) {
            dateButtons[i].setDate(startDate.plusDays(startOffset + i));
        }

        dateGroup.selectToggle(dateButtons[0]);
    }

    @FXML
    private void setTimes() {
        var selectedType = (ToggleButton)typeGroup.getSelectedToggle();
        var selectedDate = ((DateToggleButton)dateGroup.getSelectedToggle()).getDate();
        var selectedSchedule = schedule[(int)Duration.between(startDate.atStartOfDay(), selectedDate.atStartOfDay()).toDays()];

        times.getChildren().clear();
        timeGroup.getToggles().clear();

        SortedSet<LocalTime> availableTimes;
        if (selectedType == subtitle2D) {
            availableTimes = selectedSchedule.subtitle2D;
        }
        else if (selectedType == subtitle3D) {
            availableTimes = selectedSchedule.subtitle3D;
        }
        else if (selectedType == dubbing2D) {
            availableTimes = selectedSchedule.dubbing2D;
        }
        else if (selectedType == dubbing3D) {
            availableTimes = selectedSchedule.dubbing3D;
        }
        else {
            throw new IllegalStateException("Unknown type selected");
        }

        var buttons = availableTimes.stream().map(this::createTime).toList();
        times.getChildren().addAll(buttons);
        timeGroup.getToggles().setAll(buttons);

        if (!buttons.isEmpty()) {
            timeGroup.selectToggle(buttons.getFirst());
        }

        nextButton.setDisable(buttons.isEmpty());
    }

    private ToggleButton createTime(LocalTime time) {
        var button = new ToggleButton(DateTimeFormatter.ofPattern("hh:mm a").format(time));
        button.setUserData(time);
        button.setToggleGroup(timeGroup);
        button.setPrefWidth(100);
        button.getStylesheets()
                .add(Objects.requireNonNull(getClass().getResource("/com/elite/cinema/user/choose-date.css")).toExternalForm());
        button.getStyleClass().add("colorful-pill");
        return button;
    }

    public void toNextWeek() {
        if (!nextWeek) setDates(true);
    }

    public void toPreviousWeek() {
        if (nextWeek) setDates(false);
    }

    public void onCloseClicked() {
        var stage = (javafx.stage.Stage)dates.getScene().getWindow();
        stage.close();
    }
}
