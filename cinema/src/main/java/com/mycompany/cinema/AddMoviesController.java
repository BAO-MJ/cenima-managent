/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cinema;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.AnchorPane;

/**
 *
 * @author ACER
 */
public class AddMoviesController {
   @FXML
    private JFXButton as_clear;

    @FXML
    private DatePicker as_date;

    @FXML
    private JFXButton as_delete;

    @FXML
    private TextField as_duration;

    @FXML
    private AnchorPane as_form;

    @FXML
    private AnchorPane as_formtable;

    @FXML
    private TextField as_genres;

    @FXML
    private JFXButton as_import;

    @FXML
    private AnchorPane as_importimg;

    @FXML
    private JFXButton as_insert;

    @FXML
    private TextField as_search;

    @FXML
    private TextField as_title;

    @FXML
    private JFXButton as_update;

    @FXML
    private Label fullNameLabel;

    @FXML
    private JFXTreeTableView<Movie> tableview;


    private static String fullName; 
    
     public static class Movie extends RecursiveTreeObject<Movie> {
        StringProperty title;
        StringProperty genre;
        StringProperty duration;
        StringProperty releaseDate;

        public Movie(String title, String genre, String duration, String releaseDate) {
            this.title = new SimpleStringProperty(title);
            this.genre = new SimpleStringProperty(genre);
            this.duration = new SimpleStringProperty(duration);
            this.releaseDate = new SimpleStringProperty(releaseDate);
        }
    }

    @FXML
    public void initialize() {
           if (fullNameLabel != null && fullName != null) {
            fullNameLabel.setText( fullName);
        }
        setupTable();
    }
private void setupTable() {
    double tableWidth = as_formtable.getPrefWidth(); // Lấy chiều rộng của bảng chứa TableView
    double columnWidth = tableWidth / 4; // Chia đều 4 cột

    // Tạo cột
    JFXTreeTableColumn<Movie, String> titleCol = new JFXTreeTableColumn<>("Movie Title");
    titleCol.setPrefWidth(columnWidth);
    titleCol.setCellValueFactory(param -> param.getValue().getValue().title);

    JFXTreeTableColumn<Movie, String> genreCol = new JFXTreeTableColumn<>("Genre");
    genreCol.setPrefWidth(columnWidth);
    genreCol.setCellValueFactory(param -> param.getValue().getValue().genre);

    JFXTreeTableColumn<Movie, String> durationCol = new JFXTreeTableColumn<>("Duration");
    durationCol.setPrefWidth(columnWidth);
    durationCol.setCellValueFactory(param -> param.getValue().getValue().duration);

    JFXTreeTableColumn<Movie, String> releaseDateCol = new JFXTreeTableColumn<>("Published Date");
    releaseDateCol.setPrefWidth(columnWidth);
    releaseDateCol.setCellValueFactory(param -> param.getValue().getValue().releaseDate);

    // Dữ liệu mẫu
    ObservableList<Movie> movies = FXCollections.observableArrayList();
    movies.add(new Movie("Avengers: Endgame", "Hành động", "3h 2m", "26-04-2019"));
    movies.add(new Movie("Inception", "Khoa học viễn tưởng", "2h 28m", "16-07-2010"));
    movies.add(new Movie("The Dark Knight", "Hành động", "2h 32m", "18-07-2008"));

    // Tạo gốc của TreeTableView
    TreeItem<Movie> root = new RecursiveTreeItem<>(movies, RecursiveTreeObject::getChildren);
    tableview.setRoot(root);
    tableview.setShowRoot(false);
    tableview.getColumns().setAll(titleCol, genreCol, durationCol, releaseDateCol);
}


    public static void setFullName(String name) {
        fullName = name;
    }

    
    
}
