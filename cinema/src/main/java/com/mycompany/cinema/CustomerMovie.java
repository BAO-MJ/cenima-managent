package com.mycompany.cinema;

import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class CustomerMovie extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Tạo các cột mới
        JFXTreeTableColumn<Ticket, String> ticketNumberCol = createColumn("Ticket Number", Ticket::ticketNumberProperty);
        JFXTreeTableColumn<Ticket, String> movieTitleCol = createColumn("Movie Title", Ticket::movieTitleProperty);
        JFXTreeTableColumn<Ticket, String> dateCheckedCol = createColumn("Date Checked", Ticket::dateCheckedProperty);
        JFXTreeTableColumn<Ticket, String> timeCheckedCol = createColumn("Time Checked", Ticket::timeCheckedProperty);

        // Tạo danh sách dữ liệu
        ObservableList<Ticket> tickets = FXCollections.observableArrayList(
                new Ticket("T001", "Inception", "2024-02-26", "18:30"),
                new Ticket("T002", "Titanic", "2024-02-26", "20:00"),
                new Ticket("T003", "Avatar", "2024-02-27", "19:45"),
                new Ticket("T004", "The Dark Knight", "2024-02-27", "21:15")
        );

        // Gốc của TreeTableView
        TreeItem<Ticket> root = new RecursiveTreeItem<>(tickets, RecursiveTreeObject::getChildren);

        // Khởi tạo JFXTreeTableView
        JFXTreeTableView<Ticket> treeTableView = new JFXTreeTableView<>(root);
        treeTableView.getColumns().addAll(ticketNumberCol, movieTitleCol, dateCheckedCol, timeCheckedCol);
        treeTableView.setShowRoot(false);

        // Đặt vào giao diện
        StackPane rootPane = new StackPane(treeTableView);
        Scene scene = new Scene(rootPane, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Ticket List");
        primaryStage.show();
    }

    // Hàm tạo cột với dữ liệu từ property
    private JFXTreeTableColumn<Ticket, String> createColumn(String title,
        java.util.function.Function<Ticket, ObservableValue<String>> propertyGetter) {
        JFXTreeTableColumn<Ticket, String> column = new JFXTreeTableColumn<>(title);
        column.setPrefWidth(150);
        column.setCellValueFactory(param -> propertyGetter.apply(param.getValue().getValue()));
        return column;
    }

    // Lớp Ticket với các thuộc tính mới
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
    
    public static void main(String[] args) {
        launch(args);
    }
}
