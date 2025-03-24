package com.elite.cinema.controllers.user;

import com.elite.cinema.controllers.MainController;
import com.elite.cinema.db.DbSet;
import com.elite.cinema.models.tables.pojos.*;
import com.elite.cinema.models.tables.records.ReservationsRecord;
import com.elite.cinema.utils.PriceFormatter;
import com.elite.cinema.utils.TicketPrinter;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import org.jooq.types.UByte;
import org.jooq.types.UInteger;

import java.net.URL;
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

    private boolean saved;

    private final ObservableList<Seat> selectedRegularSeats = FXCollections.observableArrayList();
    private final ObservableList<Seat> selectedVipSeats = FXCollections.observableArrayList();
    private final ObservableList<Seat> selectedReclinerSeats = FXCollections.observableArrayList();

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

        selectedRegularSeats.addListener((ListChangeListener<? super Seat>) _ -> {
            regularPrice.setText(String.format("%d x %s", selectedRegularSeats.size(), PriceFormatter.format(regularSeatModel.getPrice().longValue())));
            updatePricing();
        });

        selectedVipSeats.addListener((ListChangeListener<? super Seat>) _ -> {
            vipPrice.setText(String.format("%d x %s", selectedVipSeats.size(), PriceFormatter.format(vipSeatModel.getPrice().longValue())));
            updatePricing();
        });

        selectedReclinerSeats.addListener((ListChangeListener<? super Seat>) _ -> {
            reclinerPrice.setText(String.format("%d x %s", selectedReclinerSeats.size(), PriceFormatter.format(reclinerSeatModel.getPrice().longValue())));
            updatePricing();
        });
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
        saved = false;
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

        selectedRegularSeats.clear();
        selectedVipSeats.clear();
        selectedReclinerSeats.clear();

        regularPrice.setText(String.format("%d x %s", selectedRegularSeats.size(), PriceFormatter.format(regularSeatModel.getPrice().longValue())));
        vipPrice.setText(String.format("%d x %s", selectedVipSeats.size(), PriceFormatter.format(vipSeatModel.getPrice().longValue())));
        reclinerPrice.setText(String.format("%d x %s", selectedReclinerSeats.size(), PriceFormatter.format(reclinerSeatModel.getPrice().longValue())));

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

        for (ReservedSeats seat: seats)
        {
            int row = seat.getRow().intValue();
            int col = seat.getColumn().intValue();

            seatViews[row][col].setUserData(seat.getReservationId().equals(reservation.getId()) ? SeatStatus.SELECTED : SeatStatus.RESERVED);
        }

        selectedRegularSeats.clear();
        selectedVipSeats.clear();
        selectedReclinerSeats.clear();

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

                if (seat.getUserData() == SeatStatus.SELECTED)
                {
                    if (i <= 3)
                    {
                        selectedRegularSeats.add(new Seat(i, j));
                    }
                    else if (i <= 7)
                    {
                        selectedVipSeats.add(new Seat(i, j));
                    }
                    else
                    {
                        selectedReclinerSeats.add(new Seat(i, j));
                    }
                }
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

        reservation = DbSet.getContext().newRecord(RESERVATIONS, new Reservations(null, screening.getId(), null, null, null));
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
        seatLabel.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/elite/cinema/user/booking-ticket.css")).toExternalForm());
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
        subtotal.setText(PriceFormatter.format(subtotalAmount));
        tax.setText(PriceFormatter.format(taxAmount));
        grandTotal.setText(PriceFormatter.format(grandTotalAmount));
    }

    private long calculateTotalValue()
    {
        long regularTotal = selectedRegularSeats.size() * regularSeatModel.getPrice().longValue();
        long vipTotal = selectedVipSeats.size() * vipSeatModel.getPrice().longValue();
        long reclinerTotal = selectedReclinerSeats.size() * reclinerSeatModel.getPrice().longValue();

        // Calculate totals
        long subtotalAmount = regularTotal + vipTotal + reclinerTotal;
        long taxAmount = subtotalAmount / 10;
        return subtotalAmount + taxAmount;
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
        var movie = Objects.requireNonNull(DbSet.movies().findById(screening.getMovieId()));
        if (!saved)
        {
            reservation.delete();
        }
        changeScene.accept("user/movie-details.fxml", movie);
    }

    @FXML
    private void onConfirmPressed() {
        // Check if any seats are selected
        if (selectedRegularSeats.isEmpty() && selectedVipSeats.isEmpty() && selectedReclinerSeats.isEmpty()) {
            var alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Seats Selected");
            alert.setHeaderText("Please select at least one seat.");
            alert.setContentText("You need to select at least one seat to proceed with the booking.");
            alert.showAndWait();
            return;
        }

        // Get selected payment method
//        RadioButton selectedPayment = (RadioButton)paymentGroup.getSelectedToggle();
//        String paymentMethod = selectedPayment != null ? selectedPayment.getText() : "Cash";

        // Mark reservation as paid
        reservation.setPaid(true);
        reservation.setTotal(UInteger.valueOf(calculateTotalValue()));
        reservation.update();
        saved = true;

        // Get all reserved seats for this reservation
        List<ReservedSeats> seats = DbSet.getContext()
                .selectFrom(RESERVED_SEATS)
                .where(RESERVED_SEATS.RESERVATION_ID.eq(reservation.getId()))
                .fetchInto(ReservedSeats.class);

        // Get movie and room details
        Movies movie = DbSet.movies().findById(screening.getMovieId());
        ScreeningRooms room = DbSet.screeningRooms().findById(screening.getRoomId());

        // Print individual tickets for each seat
        TicketPrinter.printTickets(reservation.getId().longValue(), movie != null ? movie.getTitle() : "", screening.getScreeningTime(), room != null ? room.getName() : "", seats);

        // Continue to confirmation page
        changeScene.accept("user/booking-confirmation.fxml", reservation);
    }

    @Override
    public void dispose() {
        refreshService.shutdown();
    }
}