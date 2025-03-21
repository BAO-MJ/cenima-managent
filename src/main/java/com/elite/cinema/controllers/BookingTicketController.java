package com.elite.cinema.controllers;

import com.elite.cinema.db.DbSet;
import com.elite.cinema.models.tables.pojos.*;
import com.elite.cinema.models.tables.records.ReservationsRecord;
import javafx.application.Platform;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.control.RadioButton;
import org.jooq.types.UByte;

import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.elite.cinema.models.Tables.RESERVATIONS;
import static com.elite.cinema.models.Tables.RESERVED_SEATS;

public class BookingTicketController extends MainController implements Initializable
{
    @FXML
    private Label roomName;
    @FXML
    private Label screeningTime;
    @FXML
    private Label movieTitle;
    @FXML
    private Label movieDisplay;
    @FXML
    private Label movieTranslation;
    @FXML
    private Label regularPrice;
    @FXML
    private Label vipPrice;
    @FXML
    private Label reclinerPrice;
    @FXML
    private Label subtotal;
    @FXML
    private Label tax;
    @FXML
    private Label orderId;
    @FXML
    private Label grandTotal;
    @FXML
    private GridPane seatsGrid;
    @FXML
    private FlowPane regularSeats;
    @FXML
    private FlowPane vipSeats;
    @FXML
    private FlowPane reclinerSeats;
    @FXML
    private ToggleGroup paymentGroup;

    private Seats regularSeatModel;
    private Seats vipSeatModel;
    private Seats reclinerSeatModel;

    private ReservationsRecord reservation;
    private Screenings screening;

    private final ImageView[][] seatViews = new ImageView[ROWS][COLUMNS];

    private static final int COLUMNS = 14;
    private static final int ROWS = 9;

    private static final NumberFormat CurrencyFormatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    private final ObservableSet<Seat> selectedRegularSeats = new SimpleSetProperty<>();
    private final ObservableSet<Seat> selectedVipSeats = new SimpleSetProperty<>();
    private final ObservableSet<Seat> selectedReclinerSeats = new SimpleSetProperty<>();

    private final Image availableSeatImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/seat-available.png")));
    private final Image selectedSeatImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/seat-selected.png")));
    private final Image reservedSeatImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/seat-reserved.png")));

    private ScheduledExecutorService refreshService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        // Set up mock data for testing
        setupMockData();

        // Set up seat grid event handlers
        setupSeatGrid();

        selectedRegularSeats.addListener((SetChangeListener<? super Seat>) _ -> {
            regularPrice.setText(String.format("%d x %s", selectedRegularSeats.size(), toMoneyValue(reclinerSeatModel.getPrice().longValue())));
            updatePricing();
        });

        selectedVipSeats.addListener((SetChangeListener<? super Seat>) _ -> {
            vipPrice.setText(String.format("%d x %s", selectedVipSeats.size(), toMoneyValue(reclinerSeatModel.getPrice().longValue())));
            updatePricing();
        });

        selectedReclinerSeats.addListener((SetChangeListener<? super Seat>) _ -> {
            reclinerPrice.setText(String.format("%d x %s", selectedReclinerSeats.size(), toMoneyValue(reclinerSeatModel.getPrice().longValue())));
            updatePricing();
        });
    }

    private String toMoneyValue(long amount)
    {
        return CurrencyFormatter.format(amount) + " VNĐ";
    }

    private record Seat(int row, int col) {

        @Override
        public String toString()
        {
            return String.format("%c-%d", (char)('A' + row), col);
        }
    }

    @Override
    public void ready(Object params)
    {
        var seatModels = DbSet.seats().findAll();
        for (Seats seat: seatModels)
        {
            switch (seat.getName())
            {
                case "Regular" -> regularSeatModel = seat;
                case "VIP" -> vipSeatModel = seat;
                case "Recliner" -> reclinerSeatModel = seat;
            }
        }
        if (regularSeatModel == null || vipSeatModel == null || reclinerSeatModel == null)
        {
            throw new IllegalStateException("Seat models not found");
        }

        setScreening((Screenings)params);
        refreshService = Executors.newSingleThreadScheduledExecutor();
        refreshService.scheduleAtFixedRate(() -> Platform.runLater(() -> {
            try {
                updateSeatStatus();
            } catch (Exception e) {
                refreshService.shutdown();
            }
        }), 0, 1, java.util.concurrent.TimeUnit.SECONDS);
    }

    private enum SeatStatus
    {
        AVAILABLE,
        SELECTED,
        RESERVED
    }

    private void updateSeatStatus()
    {
        if (screening == null) return;

        clearSeatSelections();

        var seats = DbSet.getContext().select(RESERVED_SEATS.asterisk()).from(RESERVED_SEATS)
                .where(RESERVED_SEATS.reservations().screenings().ID.eq(screening.getId()))
                .fetchInto(ReservedSeats.class);

        for (int i = 0; i < ROWS; i++)
        {
            for (int j = 0; j < COLUMNS; j++)
            {
                if (seatViews[i][j] == null) continue;
                seatViews[i][j].setUserData(SeatStatus.AVAILABLE);
            }
        }

        for (ReservedSeats seat : seats)
        {
            int row = seat.getRow().intValue();
            int col = seat.getColumn().intValue();

            seatViews[row][col].setUserData(seat.getReservationId().equals(reservation.getId()) ? SeatStatus.SELECTED : SeatStatus.RESERVED);
        }

        for (int i = 0; i < ROWS; i++)
        {
            for (int j = 0; j < COLUMNS; j++)
            {
                var seat = seatViews[i][j];
                if (seat == null) continue;

                seat.setImage(switch ((SeatStatus)seat.getUserData())
                {
                    case AVAILABLE -> availableSeatImage;
                    case SELECTED -> selectedSeatImage;
                    case RESERVED -> reservedSeatImage;
                });
            }
        }

        updateSeatDisplays();
        updatePricing();
    }

    public void setScreening(Screenings screening)
    {
        this.screening = screening;
        Movies movie = Objects.requireNonNull(DbSet.movies().findById(screening.getMovieId()));
        ScreeningRooms room = Objects.requireNonNull(DbSet.screeningRooms().findById(screening.getRoomId()));

        reservation = DbSet.getContext().newRecord(RESERVATIONS, new Reservations(null, screening.getId(), null, null));
        reservation.store();

        orderId.setText("#" + reservation.getId());
        movieTitle.setText(movie.getTitle());
        movieDisplay.setText("Display: " + screening.getDisplayType().getLiteral());
        movieTranslation.setText("Translation: " + screening.getTranslationType().getLiteral());
        roomName.setText(room.getName());
        screeningTime.setText(DateTimeFormatter.ofPattern("hh:mm a (dd MMM yyyy)").format(screening.getScreeningTime()));
    }

    private void setupMockData()
    {
        orderId.setText("#123456789");
        movieTitle.setText("Avengers: Endgame");
        movieDisplay.setText("Display: 3D");
        movieTranslation.setText("Translation: Subtitle");
        roomName.setText("Screen 4");
        screeningTime.setText("02:00 PM (18 Feb 2025)");
    }

    private void setupSeatGrid()
    {
        for (Node node: seatsGrid.getChildren())
        {
            if (node instanceof ImageView seatView)
            {
                seatView.setImage(availableSeatImage);

                int col = GridPane.getColumnIndex(node) == null ? 0 : GridPane.getColumnIndex(node);
                int row = GridPane.getRowIndex(node);

                int actualRow = switch (row)
                {
                    case 11 -> 0;
                    case 10 -> 1;
                    case 9 -> 2;
                    case 8 -> 3;
                    case 6 -> 4;
                    case 5 -> 5;
                    case 4 -> 6;
                    case 3 -> 7;
                    case 1 -> 8;
                    default -> throw new IllegalStateException("Unexpected value: " + row);
                };

                seatViews[actualRow][COLUMNS - 1 - col] = seatView;

                seatView.setCursor(Cursor.HAND);
                seatView.setOnMouseClicked(_ -> toggleSeatSelection(seatView, actualRow, COLUMNS - 1 - col));
            }
        }
    }

    private void toggleSeatSelection(ImageView seatView, int row, int col)
    {
        if (reservation == null) return;
        var status = (SeatStatus)seatView.getUserData();

        try {
            switch (status)
            {
                case SELECTED -> DbSet.reservedSeats().delete(new ReservedSeats(reservation.getId(), UByte.valueOf(row), UByte.valueOf(col)));
                case AVAILABLE -> DbSet.reservedSeats().insert(new ReservedSeats(reservation.getId(), UByte.valueOf(row), UByte.valueOf(col)));
                case RESERVED -> { return; }
            }
        } catch (Exception e)
        {
            return;
        }

        // Update seat displays and pricing
        updateSeatStatus();
    }

    private void updateSeatDisplays()
    {
        // Add selected regular seats
        regularSeats.getChildren().setAll(selectedRegularSeats.stream().map(this::createSeatLabel).toList());

        // Add selected VIP seats
        vipSeats.getChildren().setAll(selectedVipSeats.stream().map(this::createSeatLabel).toList());

        // Add selected recliner seats
        reclinerSeats.getChildren().setAll(selectedReclinerSeats.stream().map(this::createSeatLabel).toList());
    }

    private Label createSeatLabel(Seat seat)
    {
        Label seatLabel = new Label(seat.toString());
        seatLabel.getStyleClass().add("seat-label");
        seatLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        return seatLabel;
    }

    private void updatePricing()
    {
        // Calculate prices
        long regularTotal = selectedRegularSeats.size() * regularSeatModel.getPrice().longValue();
        long vipTotal = selectedVipSeats.size() * vipSeatModel.getPrice().longValue();
        long reclinerTotal = selectedReclinerSeats.size() * reclinerSeatModel.getPrice().longValue();

        // Calculate totals
        long subtotalAmount = regularTotal + vipTotal + reclinerTotal;
        long taxAmount = subtotalAmount / 10;
        long grandTotalAmount = subtotalAmount + taxAmount;

        // Update total labels
        subtotal.setText(toMoneyValue(subtotalAmount));
        tax.setText(toMoneyValue(taxAmount));
        grandTotal.setText(toMoneyValue(grandTotalAmount));
    }

    private void clearSeatSelections()
    {
        selectedRegularSeats.clear();
        selectedVipSeats.clear();
        selectedReclinerSeats.clear();
        regularSeats.getChildren().clear();
        vipSeats.getChildren().clear();
        reclinerSeats.getChildren().clear();

        for (int i = 0; i < ROWS; i++)
        {
            for (int j = 0; j < COLUMNS; j++)
            {
                if (seatViews[i][j] == null) continue;
                seatViews[i][j].setUserData(SeatStatus.AVAILABLE);
            }
        }
    }

    @FXML
    private void onCancelPressed()
    {
        // Handle cancellation (e.g., go back to previous page)
        System.out.println("Booking cancelled");
        // Implementation could navigate back to previous screen
    }

    @FXML
    private void onConfirmPressed()
    {
        // Get selected payment method
        RadioButton selectedPayment = (RadioButton)paymentGroup.getSelectedToggle();
        if (selectedPayment == null)
        {
            // Show error - payment method required
            System.out.println("Please select a payment method");
            return;
        }

        // Check if any seats are selected
        if (selectedRegularSeats.isEmpty() && selectedVipSeats.isEmpty() && selectedReclinerSeats.isEmpty())
        {
            System.out.println("Please select at least one seat");
            return;
        }

        // Proceed with booking
        System.out.println("Processing booking with " + selectedPayment.getText() + " payment");
        // Implementation would save the booking to database
    }

    @Override
    public void dispose() {
        refreshService.shutdown();
    }
}