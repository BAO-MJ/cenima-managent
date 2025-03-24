package com.elite.cinema.controllers.admin;

import com.elite.cinema.controllers.MainController;
import com.elite.cinema.db.DbSet;
import com.elite.cinema.utils.ImageHelper;
import com.elite.cinema.utils.PriceFormatter;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.Objects;

import static com.elite.cinema.models.Tables.*;
import static org.jooq.impl.DSL.sum;

public class AdminHomeController extends MainController
{
    @FXML
    private Label todayTotalShow;

    @FXML
    private Label totalBooking;

    @FXML
    private Label totalSeats;

    @FXML
    private Label totalRevenue;

    @FXML
    private VBox todaySchedule;

    @Override
    public void ready(Object params)
    {
        totalRevenue.setText(PriceFormatter.format(DbSet.getContext().select(sum(REVENUES.TOTAL_REVENUE)).from(REVENUES).fetchOneInto(Long.class)));
        totalBooking.setText(Objects.requireNonNull(DbSet.getContext().selectCount().from(RESERVATIONS).where(RESERVATIONS.PAID.isTrue()).fetchOneInto(Long.class)).toString());
        totalSeats.setText(Objects.requireNonNull(DbSet.getContext().select(sum(REVENUES.TOTAL_TICKETS)).from(REVENUES).fetchOneInto(Long.class)).toString());
        todayTotalShow.setText(Objects.requireNonNull(DbSet.getContext().selectCount().from(SCREENINGS).where(SCREENINGS.SCREENING_TIME.cast(LocalDate.class).eq(LocalDate.now())).fetchOneInto(Long.class)).toString());

        // Load today's screenings
        var todayScreenings = DbSet.getContext()
                .select(SCREENINGS.asterisk())
                .from(SCREENINGS)
                .where(SCREENINGS.SCREENING_TIME.cast(LocalDate.class).eq(LocalDate.now()))
                .fetch();

        // Clear any existing children
        todaySchedule.getChildren().clear();

        // Add a card for each screening
        todayScreenings.forEach(screening -> {
            var movieId = screening.get(SCREENINGS.MOVIE_ID);
            var movie = DbSet.movies().findById(movieId);
            var screeningTime = screening.get(SCREENINGS.SCREENING_TIME);

            // Create card for the screening
            var card = new javafx.scene.layout.HBox(10);
            card.setPadding(new javafx.geometry.Insets(10));
            card.setStyle("-fx-background-color: #555555; -fx-background-radius: 5;");
            card.setPrefWidth(200);
            card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            // Movie poster image
            var imageView = new javafx.scene.image.ImageView();
            imageView.setFitHeight(40);
            imageView.setFitWidth(30);
            imageView.setPreserveRatio(true);

            // Try to load movie image or use placeholder
            try {
                var image = movie != null && movie.getPoster() != null
                        ? ImageHelper.byteArrayToImage(movie.getPoster())
                        : new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/image/default-poster.png")));
                imageView.setImage(image);
            } catch (Exception e) {
                // Use a rectangle as fallback if image loading fails
                var placeholder = new javafx.scene.shape.Rectangle(30, 40, javafx.scene.paint.Color.DARKGRAY);
                card.getChildren().add(placeholder);
            }

            // Title in the middle with container to center it
            var titleContainer = new javafx.scene.layout.VBox();
            titleContainer.setAlignment(javafx.geometry.Pos.CENTER);
            HBox.setHgrow(titleContainer, javafx.scene.layout.Priority.ALWAYS);
            var title = new Label(movie != null ? movie.getTitle() : "Unknown Movie");
            title.setStyle("-fx-font-weight: bold;");
            title.setWrapText(true);
            titleContainer.getChildren().add(title);

            // Time on the right
            var time = new Label(screeningTime.toLocalTime().toString());
            time.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

            card.getChildren().addAll(imageView, titleContainer, time);

            // Add the card to the schedule
            todaySchedule.getChildren().add(card);
        });
    }
}
