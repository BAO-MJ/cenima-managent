package com.elite.cinema;

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

public class EditMovie extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Tạo các cột
        JFXTreeTableColumn<Movie, String> titleCol = createColumn("Movie Title", Movie::titleProperty);
        JFXTreeTableColumn<Movie, String> genreCol = createColumn("Genre", Movie::genreProperty);
        JFXTreeTableColumn<Movie, String> durationCol = createColumn("Duration", Movie::durationProperty);
        JFXTreeTableColumn<Movie, String> currentCol = createColumn("Current", Movie::currentProperty);

        // Tạo danh sách dữ liệu
        ObservableList<Movie> movies = FXCollections.observableArrayList(
                new Movie("Inception", "Sci-Fi", "148 min", "Yes"),
                new Movie("Titanic", "Romance", "195 min", "No"),
                new Movie("Avatar", "Action", "162 min", "Yes"),
                new Movie("The Dark Knight", "Action", "152 min", "No")
        );

        // Gốc của TreeTableView
        TreeItem<Movie> root = new RecursiveTreeItem<>(movies, RecursiveTreeObject::getChildren);

        // Khởi tạo JFXTreeTableView
        JFXTreeTableView<Movie> treeTableView = new JFXTreeTableView<>(root);
        treeTableView.getColumns().addAll(titleCol, genreCol, durationCol, currentCol);
        treeTableView.setShowRoot(false);

        // Đặt vào giao diện
        StackPane rootPane = new StackPane(treeTableView);
        Scene scene = new Scene(rootPane, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Movie List");
        primaryStage.show();
    }

    // Hàm tạo cột với dữ liệu từ property
    private JFXTreeTableColumn<Movie, String> createColumn(String title,
        java.util.function.Function<Movie, ObservableValue<String>> propertyGetter) {
        JFXTreeTableColumn<Movie, String> column = new JFXTreeTableColumn<>(title);
        column.setPrefWidth(150);
        column.setCellValueFactory(param -> propertyGetter.apply(param.getValue().getValue()));
        return column;
    }

    // Lớp Movie với các thuộc tính
    public static class Movie extends RecursiveTreeObject<Movie> {
        private final SimpleStringProperty title;
        private final SimpleStringProperty genre;
        private final SimpleStringProperty duration;
        private final SimpleStringProperty current;

        public Movie(String title, String genre, String duration, String current) {
            this.title = new SimpleStringProperty(title);
            this.genre = new SimpleStringProperty(genre);
            this.duration = new SimpleStringProperty(duration);
            this.current = new SimpleStringProperty(current);
        }

        public ObservableValue<String> titleProperty() { return title; }
        public ObservableValue<String> genreProperty() { return genre; }
        public ObservableValue<String> durationProperty() { return duration; }
        public ObservableValue<String> currentProperty() { return current; }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
