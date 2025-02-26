package com.mycompany.cinema;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;

/**
 *
 * @author ACER
 */
public class EditScreeningController {
    @FXML
    private JFXComboBox<?> edit_combobox;

    @FXML
    private JFXButton edit_delete;

    @FXML
    private AnchorPane edit_form;

    @FXML
    private AnchorPane edit_form1;

    @FXML
    private AnchorPane edit_img;

    @FXML
    private JFXTreeTableView<Movie> edit_tableview;

    @FXML
    private JFXButton edit_up;

    @FXML
    private Label fullNameLabel; // Nhận Label từ FXML

    private static String fullName; // Biến lưu tên người dùng

    public static void setFullName(String name) {
        fullName = name;
    }
    
    @FXML
    public void initialize() {
        if (fullNameLabel != null && fullName != null) {
            fullNameLabel.setText(fullName);
        }
        
       JFXTreeTableColumn<Movie, String> titleCol = createColumn("Movie Title", Movie::titleProperty);
    titleCol.setPrefWidth(135);

    JFXTreeTableColumn<Movie, String> genreCol = createColumn("Genre", Movie::genreProperty);
    genreCol.setPrefWidth(130);

    JFXTreeTableColumn<Movie, String> durationCol = createColumn("Duration", Movie::durationProperty);
    durationCol.setPrefWidth(130);

    JFXTreeTableColumn<Movie, String> currentCol = createColumn("Current", Movie::currentProperty);
    currentCol.setPrefWidth(140);

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
    edit_tableview.setRoot(root);
    edit_tableview.getColumns().setAll(titleCol, genreCol, durationCol, currentCol);
    edit_tableview.setShowRoot(false);
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
}