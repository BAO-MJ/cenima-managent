package com.elite.cinema.controllers.user;

import atlantafx.base.theme.Styles;
import com.elite.cinema.controllers.MainController;
import com.elite.cinema.db.DbSet;
import com.elite.cinema.models.tables.pojos.*;
import com.elite.cinema.models.tables.records.ReservationsRecord;
import com.elite.cinema.utils.DateHelper;
import com.elite.cinema.utils.PriceFormatter;
import com.elite.cinema.utils.TicketPrinter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static com.elite.cinema.models.Tables.RESERVED_SEATS;

public class BookingConfirmationController extends MainController {

    @FXML private Label movieTitleLabel;
    @FXML private Label screeningDateLabel;
    @FXML private Label screeningTimeLabel;
    @FXML private Label roomLabel;
    @FXML private Label orderIdLabel;
    @FXML private Label seatsLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Label paymentMethodLabel;
    @FXML private Label paymentStatusLabel;
    @FXML private Button printTicketsButton;
    @FXML private Button homeButton;

    private ReservationsRecord reservation;

    private Movies movie;
    private Screenings screening;
    private ScreeningRooms room;
    private List<ReservedSeats> seats;

    public void initialize()
    {
        printTicketsButton.getStyleClass().add(Styles.ACCENT);
        homeButton.getStyleClass().add(Styles.SUCCESS);
    }

    public void ready(Object params) {
        this.reservation = (ReservationsRecord)params;

        loadData();
        displayData();
    }

    private void loadData() {
        // Load related data from database
        screening = DbSet.screenings().findById(reservation.getScreeningId());
        movie = DbSet.movies().findById(screening.getMovieId());
        room = DbSet.screeningRooms().findById(screening.getRoomId());

        // Get reserved seats
        seats = DbSet.getContext()
                .selectFrom(RESERVED_SEATS)
                .where(RESERVED_SEATS.RESERVATION_ID.eq(reservation.getId()))
                .fetchInto(ReservedSeats.class);
    }

    private void displayData() {
        // Set movie details
        movieTitleLabel.setText(movie.getTitle());

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        screeningDateLabel.setText(DateHelper.formatDate(screening.getScreeningTime().toLocalDate()));
        screeningTimeLabel.setText(screening.getScreeningTime().format(timeFormatter));
        roomLabel.setText(room.getName());

        orderIdLabel.setText(reservation.getId().toString());

        String seatsText = seats.stream()
                .map(seat -> String.format("%c%d",
                        (char)('A' + seat.getRow().intValue()),
                        seat.getColumn().intValue() + 1))
                .collect(Collectors.joining(", "));
        seatsLabel.setText(seatsText);

        totalAmountLabel.setText(PriceFormatter.format(reservation.getTotal().longValue()));

        // TODO: Set payment method
        paymentMethodLabel.setText("Cash"); // Or get from reservation if stored

        // Payment status
        paymentStatusLabel.setText("PAID");
    }

    @FXML
    private void onPrintTicketsAgain() {
        TicketPrinter.printTickets(reservation.getId().longValue(), movie.getTitle(), screening.getScreeningTime(), room.getName(), seats);
    }

    @FXML
    private void onHomeButtonPressed() {
        changeScene.accept("user/available-movies.fxml", null);
    }
}