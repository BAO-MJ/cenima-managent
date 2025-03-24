package com.elite.cinema.controllers.user;

import com.elite.cinema.controllers.MainController;
import com.elite.cinema.db.Booking;
import com.elite.cinema.db.DbSet;
import com.elite.cinema.utils.DateHelper;

import com.elite.cinema.utils.TicketPrinter;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.jooq.types.ULong;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.elite.cinema.models.Tables.RESERVATIONS;

public class CustomerController extends MainController
{
    @FXML
    private DatePicker dateFilter;

    @FXML
    private TableView<Booking> historyTable;

    @FXML
    private TableColumn<Booking, String> orderIdColumn;

    @FXML
    private TableColumn<Booking, String> movieColumn;

    @FXML
    private TableColumn<Booking, String> roomColumn;

    @FXML
    private TableColumn<Booking, String> bookingTimeColumn;

    @FXML
    private TableColumn<Booking, String> showTimeColumn;

    @FXML
    private TableColumn<Booking, String> seatsColumn;

    @FXML
    private TableColumn<Booking, HBox> actionColumn;

    private ScheduledExecutorService refreshService;

    @FXML
    public void initialize() {
        setupTable();
        dateFilter.setOnAction(_ -> loadData());
    }

    @Override
    public void ready(Object params) {
        dateFilter.setValue(LocalDate.now());
        refreshService = Executors.newSingleThreadScheduledExecutor();
        refreshService.scheduleAtFixedRate(this::loadData, 0, 5, TimeUnit.SECONDS);
    }

    private void loadData() {
        ObservableList<Booking> bookings = FXCollections.observableArrayList(
                DbSet.getContext().select(RESERVATIONS.ID.as("order_id"),
                        RESERVATIONS.screenings().movies().TITLE.as("movie_title"),
                        RESERVATIONS.screenings().screeningRooms().NAME.as("screening_room"),
                        RESERVATIONS.CREATED_AT.cast(LocalTime.class).as("booking_time"),
                        RESERVATIONS.screenings().SCREENING_TIME.as("show_time"))
                        .from(RESERVATIONS).join(RESERVATIONS.screenings()).join(RESERVATIONS.screenings().movies())
                        .join(RESERVATIONS.screenings().screeningRooms())
                        .where(RESERVATIONS.PAID.isTrue().and(RESERVATIONS.CREATED_AT.cast(LocalDate.class).eq(dateFilter.getValue())))
                .fetch().stream().map(
                        record -> new Booking(
                                record.get("order_id", ULong.class),
                                record.get("movie_title", String.class),
                                record.get("screening_room", String.class),
                                record.get("booking_time", LocalTime.class),
                                record.get("show_time", LocalDateTime.class),
                                DbSet.reservedSeats().fetchByReservationId(record.get("order_id", ULong.class))
                        )).toList()
        );

        historyTable.setItems(bookings);
        historyTable.setPlaceholder(new Label("No bookings found"));
    }

    private void setupTable()
    {
        orderIdColumn.setCellValueFactory(bk -> new SimpleStringProperty("#" + bk.getValue().orderId().toString()));
        movieColumn.setCellValueFactory(bk -> new SimpleStringProperty(bk.getValue().movieTitle()));
        roomColumn.setCellValueFactory(bk -> new SimpleStringProperty(bk.getValue().screeningRoom()));
        bookingTimeColumn.setCellValueFactory(bk -> new SimpleStringProperty(DateHelper.formatTime(bk.getValue().bookingTime())));
        showTimeColumn.setCellValueFactory(bk -> new SimpleStringProperty(DateHelper.formatDateTime(bk.getValue().showTime())));
        seatsColumn.setCellValueFactory(bk -> new SimpleStringProperty(String.join(", ", bk.getValue().seats().stream().map(s -> (char)('A' + s.getRow().intValue()) + String.valueOf(s.getColumn())).toList())));
        actionColumn.setCellValueFactory(bk -> {
            var booking = bk.getValue();

            Button viewButton = new Button("Print");
            viewButton.setOnAction(_ -> TicketPrinter.printTickets(booking.orderId().longValue(), booking.movieTitle(), booking.showTime(), booking.screeningRoom(), booking.seats()));

            Button cancelButton = new Button("Delete");
            cancelButton.setOnAction(_ -> {
                var alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this booking?", ButtonType.YES, ButtonType.NO);
                alert.setTitle("Delete Booking");
                alert.setHeaderText(null);
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        DbSet.reservations().deleteById(booking.orderId());

                        loadData();

                        var alertInfo = new Alert(Alert.AlertType.INFORMATION, "Booking deleted successfully", ButtonType.OK);
                        alertInfo.showAndWait();
                    }
                });
            });

            HBox hbox = new HBox(viewButton, cancelButton);
            hbox.setSpacing(10);
            return new SimpleObjectProperty<>(hbox);
        });
    }

    @Override
    public void dispose()
    {
        refreshService.shutdown();
    }
}
