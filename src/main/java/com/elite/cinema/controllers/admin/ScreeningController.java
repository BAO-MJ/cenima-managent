package com.elite.cinema.controllers.admin;

import com.elite.cinema.ScreeningInfo;
import com.elite.cinema.controllers.MainController;
import com.elite.cinema.db.DbSet;
import com.elite.cinema.models.enums.ScreeningsDisplayType;
import com.elite.cinema.models.enums.ScreeningsTranslationType;
import com.elite.cinema.models.tables.pojos.Movies;
import com.elite.cinema.schedule.ScreeningRoomSchedule;
import com.elite.cinema.schedule.ScreeningTime;

import com.elite.cinema.ui.ScreeningHourPanel;
import com.elite.cinema.utils.ComboBoxHelper;
import com.elite.cinema.utils.DateHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import org.jooq.DatePart;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.types.ULong;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.elite.cinema.models.Tables.MOVIES;
import static com.elite.cinema.models.Tables.SCREENINGS;
import static com.elite.cinema.models.Tables.SCREENING_ROOMS;

public class ScreeningController extends MainController implements Initializable {

    // FXML UI components
    @FXML
    private ComboBox<Movies> movieComboBox;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Label durationLabel;
    @FXML
    private ComboBox<ScreeningsDisplayType> displayTypeComboBox;
    @FXML
    private ComboBox<ScreeningsTranslationType> translationTypeComboBox;
    @FXML
    private GridPane screeningHoursGrid;

    private ScheduledExecutorService refreshService;

    // Data
    private final ObservableList<ScreeningInfo> screenings = FXCollections.observableArrayList();
    private final ScreeningHourPanel[] hourSlots = new ScreeningHourPanel[24];

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup UI components
        setupComboBoxes();
        setupEventHandlers();

        for (int hour = 0; hour < 24; hour++) {
            ScreeningHourPanel hourPanel = new ScreeningHourPanel(hour);
            hourSlots[hour] = hourPanel;
            screeningHoursGrid.add(hourPanel, hour % 3, hour / 3);
            hourPanel.setOnAction(this::refreshData);
        }
    }

    @Override
    public void ready(Object params)
    {
        // Initialize with today's date + 1 day (typical earliest booking)
        datePicker.setValue(LocalDate.now().plusDays(1));

        // Set past dates as disabled
        datePicker.setDayCellFactory(_ -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isBefore(LocalDate.now().plusDays(1)));
            }
        });

        refreshData();
        refreshService = Executors.newSingleThreadScheduledExecutor();
        refreshService.scheduleAtFixedRate(() -> Platform.runLater(() -> {
            try {
                refreshHours();
            } catch (Exception e) {
                refreshService.shutdown();
            }
        }), 0, 1, java.util.concurrent.TimeUnit.SECONDS);
    }

    private <T> void initializeComboBox(ComboBox<T> cmb, T[] items, Function<T, String> displayFunction) {
        cmb.setItems(FXCollections.observableArrayList(items));
        cmb.setConverter(ComboBoxHelper.getStringConverter(displayFunction));
        cmb.getSelectionModel().selectFirst();
    }

    private void setupComboBoxes() {
        // Setup display type combo box
        initializeComboBox(displayTypeComboBox, ScreeningsDisplayType.values(), ScreeningsDisplayType::getLiteral);
        initializeComboBox(translationTypeComboBox, ScreeningsTranslationType.values(),
                ScreeningsTranslationType::getLiteral);

        // Setup movie combo box
        movieComboBox.setConverter(ComboBoxHelper.getStringConverter(Movies::getTitle));
    }

    private void setupEventHandlers() {
        // Update available time slots when parameters change
        movieComboBox.valueProperty().addListener((_, _, movie) -> {
            if (movie != null) {
                durationLabel.setText(movie.getDuration() + " min");

                var minDate = DateHelper.maxDate(movie.getReleaseDate(), LocalDate.now().plusDays(1));
                datePicker.setDayCellFactory(_ -> new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        setDisable(!DateHelper.between(date, minDate, movie.getEndDate()));
                    }
                });

                var clampedDate = DateHelper.clampDate(datePicker.getValue(), minDate, movie.getEndDate());
                datePicker.setValue(clampedDate);

                refreshHours();
            }
            else {
                durationLabel.setText("--- min");
            }
        });

        datePicker.valueProperty().addListener(_ -> refreshHours());
        displayTypeComboBox.valueProperty().addListener(_ -> refreshHours());
        translationTypeComboBox.valueProperty().addListener(_ -> refreshHours());
    }

    private void loadMovies() {
        // Load all movies
        List<Movies> movies = DbSet.getContext().selectFrom(MOVIES).where(MOVIES.END_DATE.ge(LocalDate.now())).fetchInto(Movies.class);

        movieComboBox.setItems(FXCollections.observableArrayList(movies));

        // Select first movie if available
        if (!movies.isEmpty()) {
            movieComboBox.getSelectionModel().selectFirst();
        }
    }

    private void loadScreenings() {
        // Load all screenings for today or future dates
        List<ScreeningInfo> allScreenings = DbSet.getContext()
                .select(SCREENINGS.ID, SCREENINGS.MOVIE_ID, MOVIES.TITLE.as("movie_title"),
                        MOVIES.DURATION.as("movie_duration"), SCREENINGS.ROOM_ID, SCREENING_ROOMS.NAME.as("room_name"),
                        SCREENINGS.SCREENING_TIME, SCREENINGS.DISPLAY_TYPE, SCREENINGS.TRANSLATION_TYPE)
                .from(SCREENINGS).join(MOVIES).on(MOVIES.ID.eq(SCREENINGS.MOVIE_ID)).join(SCREENING_ROOMS)
                .on(SCREENING_ROOMS.ID.eq(SCREENINGS.ROOM_ID))
                .where(SCREENINGS.SCREENING_TIME.greaterOrEqual(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT)))
                .orderBy(SCREENINGS.SCREENING_TIME).fetchInto(ScreeningInfo.class);

        screenings.setAll(allScreenings);
    }

    @FXML
    public void refreshData() {
        loadMovies();
        loadScreenings();
        refreshHours();
    }

    private void refreshHours() {
        Movies selectedMovie = movieComboBox.getSelectionModel().getSelectedItem();
        LocalDate selectedDate = datePicker.getValue();

        if (selectedMovie == null || selectedDate == null) {
            return;
        }

        var ctx = DbSet.getContext();



        // Get all screenings for the selected date
        Map<ULong, List<ScreeningTime>> screenings = ctx
                .select(SCREENINGS.ID, SCREENINGS.screeningRooms().ID, SCREENINGS.SCREENING_TIME, SCREENINGS.MOVIE_ID, SCREENINGS.movies().DURATION, SCREENINGS.DISPLAY_TYPE, SCREENINGS.TRANSLATION_TYPE)
                .from(SCREENINGS).where(DSL.cast(SCREENINGS.SCREENING_TIME, SQLDataType.LOCALDATE).eq(selectedDate)
                        .or(DSL.cast(
                                DSL.localDateTimeAdd(SCREENINGS.SCREENING_TIME, SCREENINGS.movies().DURATION, DatePart.MINUTE),
                                SQLDataType.LOCALDATE).eq(selectedDate))
                )
                .fetchGroups(SCREENINGS.screeningRooms().ID, ScreeningTime.class);

        var schedules = new HashMap<ULong, MovieScheduleParams>();
        var rooms = DbSet.screeningRooms().findAll();

        for (var room : rooms) {
            var schedule = new ScreeningRoomSchedule(selectedDate, screenings.getOrDefault(room.getId(), new ArrayList<>()));

            var params = new MovieScheduleParams(
                    schedule.getEmptyTimes().stream().flatMap(t -> IntStream.range(t.start(), t.end()).boxed()).toList(),
                    schedule.getScheduleByMovie(selectedMovie.getId(), displayTypeComboBox.getValue(), translationTypeComboBox.getValue())
            );

            schedules.put(room.getId(), params);
        }

        long[] schedule = new long[24 * 12]; // 5 minute intervals
        Arrays.fill(schedule, 0L); // 0 = unavailable, > 0 = available (room id), < 0 = selected (screening id)

        for (var entry : schedules.entrySet()) {
            for (int i : entry.getValue().emptyTime()) {
                if (schedule[i] == 0)
                {
                    schedule[i] = entry.getKey().longValue();
                }
            }
            for (var params : entry.getValue().existedTime()) {
                schedule[params.getValue()] = -params.getKey().longValue();
            }
        }

        for (int hour = 0; hour < 24; hour++) {
            hourSlots[hour].reset();
            for (int minute = 0; minute < 60; minute += 5) {
                long id = schedule[(hour * 60 + minute) / 5];

                if (id > 0) {
                    hourSlots[hour].setAvailable(minute, selectedMovie.getId(), ULong.valueOf(id), selectedDate, displayTypeComboBox.getValue(), translationTypeComboBox.getValue());
                }
                else if (id < 0) {
//                    if (LocalDate.now().until(selectedDate, ChronoUnit.DAYS) > 14)
//                    {
                        hourSlots[hour].setSelected(minute, ULong.valueOf(-id));
//                    }
//                    else
//                    {
//                        hourSlots[hour].setLocked(minute);
//                    }
                }
                else {
                    hourSlots[hour].setUnavailable(minute);
                }
            }
        }
    }

    private record MovieScheduleParams(List<Integer> emptyTime, List<Pair<ULong, Integer>> existedTime) {}

    @Override
    public void dispose()
    {
        refreshService.shutdown();
    }
}