package com.elite.cinema;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;

public class CustomerController {
    @FXML
    private JFXButton cus_clear;

    @FXML
    private JFXButton cus_delete;

    @FXML
    private AnchorPane cus_form;

    @FXML
    private AnchorPane cus_form1;

    @FXML
    private AnchorPane cus_form2;

    @FXML
    private JFXTreeTableView<Ticket> cus_tableview;

    @FXML
    private Label fullNameLabel;

    private static String fullName;

    public static void setFullName(String name) {
        fullName = name;
    }

    @FXML
    public void initialize() {
        if (fullNameLabel != null && fullName != null) {
            fullNameLabel.setText(fullName);
        }

        setupTicketTable(); // Gọi phương thức để hiển thị dữ liệu
    }

    // 🔹 Phương thức tạo bảng dữ liệu vé
   private void setupTicketTable() {
    // Lấy chiều rộng của bảng
    double tableWidth = cus_tableview.getPrefWidth();

    // Tạo các cột với tỷ lệ chiều rộng tự động
    JFXTreeTableColumn<Ticket, String> ticketNumberCol = createColumn("Ticket Number", Ticket::ticketNumberProperty, tableWidth * 0.25);
    JFXTreeTableColumn<Ticket, String> movieTitleCol = createColumn("Movie Title", Ticket::movieTitleProperty, tableWidth * 0.35);
    JFXTreeTableColumn<Ticket, String> dateCheckedCol = createColumn("Date Checked", Ticket::dateCheckedProperty, tableWidth * 0.20);
    JFXTreeTableColumn<Ticket, String> timeCheckedCol = createColumn("Time", Ticket::timeCheckedProperty, tableWidth * 0.20);

    // Danh sách dữ liệu
    ObservableList<Ticket> tickets = FXCollections.observableArrayList(
            new Ticket("T001", "Inception", "2024-02-26", "18:30"),
            new Ticket("T002", "Titanic", "2024-02-26", "20:00"),
            new Ticket("T003", "Avatar", "2024-02-27", "19:45"),
            new Ticket("T004", "The Dark Knight", "2024-02-27", "21:15")
    );

    // Gốc của TreeTableView
    TreeItem<Ticket> root = new RecursiveTreeItem<>(tickets, RecursiveTreeObject::getChildren);

    // Gán dữ liệu vào bảng
    cus_tableview.getColumns().setAll(ticketNumberCol, movieTitleCol, dateCheckedCol, timeCheckedCol);
    cus_tableview.setRoot(root);
    cus_tableview.setShowRoot(false);
}


    // 🔹 Phương thức tạo cột cho bảng
  private JFXTreeTableColumn<Ticket, String> createColumn(String title,
    java.util.function.Function<Ticket, ObservableValue<String>> propertyGetter, double width) {
    JFXTreeTableColumn<Ticket, String> column = new JFXTreeTableColumn<>(title);
    column.setPrefWidth(width);
    column.setCellValueFactory(param -> propertyGetter.apply(param.getValue().getValue()));

    // Căn giữa nội dung cột
    column.setStyle("-fx-alignment: CENTER;");

    return column;
}


    // 🔹 Lớp dữ liệu Ticket
    public static class Ticket extends RecursiveTreeObject<Ticket> {
        private final SimpleStringProperty ticketNumber;
        private final SimpleStringProperty movieTitle;
        private final SimpleStringProperty dateChecked;
        private final SimpleStringProperty timeChecked;

        public Ticket(String ticketNumber, String movieTitle, String dateChecked, String timeChecked) {
            this.ticketNumber = new SimpleStringProperty(ticketNumber);
            this.movieTitle = new SimpleStringProperty(movieTitle);
            this.dateChecked = new SimpleStringProperty(dateChecked);
            this.timeChecked = new SimpleStringProperty(timeChecked);
        }

        public ObservableValue<String> ticketNumberProperty() { return ticketNumber; }
        public ObservableValue<String> movieTitleProperty() { return movieTitle; }
        public ObservableValue<String> dateCheckedProperty() { return dateChecked; }
        public ObservableValue<String> timeCheckedProperty() { return timeChecked; }
    }
}
