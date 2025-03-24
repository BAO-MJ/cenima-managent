package com.elite.cinema.controllers.user;

import com.elite.cinema.controllers.MainController;
import com.elite.cinema.db.DbSet;
import com.elite.cinema.models.tables.pojos.RefreshmentsOrders;
import com.elite.cinema.utils.DateHelper;

import com.elite.cinema.utils.PriceFormatter;
import com.elite.cinema.utils.RefreshmentReceiptPrinter;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Pair;

import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.elite.cinema.models.Tables.*;

public class CustomerFoodController extends MainController
{
    @FXML
    private DatePicker dateFilter;

    @FXML
    private TableView<RefreshmentsOrders> historyTable;

    @FXML
    private TableColumn<RefreshmentsOrders, String> orderIdColumn;

    @FXML
    private TableColumn<RefreshmentsOrders, String> orderTimeColumn;

    @FXML
    private TableColumn<RefreshmentsOrders, String> priceColumn;

    @FXML
    private TableColumn<RefreshmentsOrders, HBox> actionColumn;

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
        ObservableList<RefreshmentsOrders> bookings = FXCollections.observableArrayList(
                DbSet.getContext().selectFrom(REFRESHMENTS_ORDERS)
                        .where(REFRESHMENTS_ORDERS.CREATED_AT.cast(LocalDate.class).eq(dateFilter.getValue()))
                        .fetchInto(RefreshmentsOrders.class)
        );

        historyTable.setItems(bookings);
        historyTable.setPlaceholder(new Label("No bookings found"));
    }

    private void setupTable()
    {
        orderIdColumn.setCellValueFactory(order -> new SimpleStringProperty("#" + order.getValue().getId().toString()));
        orderTimeColumn.setCellValueFactory(order -> new SimpleStringProperty(DateHelper.formatTime(order.getValue().getCreatedAt().toLocalTime())));
        priceColumn.setCellValueFactory(order -> new SimpleStringProperty(PriceFormatter.format(order.getValue().getTotal().longValue())));
        actionColumn.setCellValueFactory(o -> {
            var order = o.getValue();

            Button viewButton = new Button("Print");
            viewButton.setOnAction(_ -> {
                var cart = DbSet.refreshmentsOrderDetails().fetchByOrderId(order.getId())
                                .stream().map(kv -> new Pair<>(kv.getRefreshmentId(), kv.getQuantity().intValue()))
                                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

                var refreshments = DbSet.refreshments().findAll();

                RefreshmentReceiptPrinter.printReceipt(order.getId(), cart, refreshments, order.getTotal().longValue() / 11 * 10);
            });

            Button cancelButton = new Button("Delete");
            cancelButton.setOnAction(_ -> {
                var alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this order?", ButtonType.YES, ButtonType.NO);
                alert.setTitle("Delete Order");
                alert.setHeaderText(null);
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        DbSet.reservations().deleteById(order.getId());

                        loadData();

                        var alertInfo = new Alert(Alert.AlertType.INFORMATION, "Order deleted successfully", ButtonType.OK);
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
