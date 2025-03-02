package com.elite.cinema;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;


public class AvailableMoviesController {

    @FXML
    private JFXButton am_clear, am_pay, am_receipt, am_selectmoviebutton;

    @FXML
    private TextField am_date, am_genre, am_title;

    @FXML
    private AnchorPane am_form, am_form1, am_img, form_selectmovie;

    @FXML
    private Spinner<?> am_quantity, am_quantity1;

    @FXML
    private JFXTreeTableView<AvailableMovie> am_tableview;
    @FXML
    private TextField am_search;
    @FXML
    private Label fullNameLabel; 

    private static String fullName; 

   
    @FXML
    public void initialize() {
        if (fullNameLabel != null && fullName != null) {
            fullNameLabel.setText(fullName);
        }
        setupTable();
    }

  
    @FXML
    private void setupTable() {
      
        am_tableview.setPrefWidth(500);
        am_tableview.setMinWidth(500);
        am_tableview.setMaxWidth(800);

       
        JFXTreeTableColumn<AvailableMovie, String> titleColumn = new JFXTreeTableColumn<>("Movie Title");
        titleColumn.setPrefWidth(200); 
        titleColumn.setCellValueFactory(param -> param.getValue().getValue().titleProperty());

        // Cột Genre
        JFXTreeTableColumn<AvailableMovie, String> genreColumn = new JFXTreeTableColumn<>("Genre");
        genreColumn.setPrefWidth(150);
        genreColumn.setCellValueFactory(param -> param.getValue().getValue().genreProperty());

        // Cột Showing Date
        JFXTreeTableColumn<AvailableMovie, String> dateColumn = new JFXTreeTableColumn<>("Showing Date");
        dateColumn.setPrefWidth(150);
        dateColumn.setCellValueFactory(param -> param.getValue().getValue().showingDateProperty());

       
        ObservableList<AvailableMovie> movies = FXCollections.observableArrayList(
                new AvailableMovie("Inception", "Sci-Fi", "2025-02-25"),
                new AvailableMovie("Titanic", "Romance", "2025-02-26"),
                new AvailableMovie("Avatar", "Action", "2025-02-27")
        );

        TreeItem<AvailableMovie> root = new RecursiveTreeItem<>(movies, RecursiveTreeObject::getChildren);
        am_tableview.getColumns().setAll(titleColumn, genreColumn, dateColumn);
        am_tableview.setRoot(root);
        am_tableview.setShowRoot(false); // Ẩn gốc của cây

      
        titleColumn.getStyleClass().add("table-header");
        genreColumn.getStyleClass().add("table-header");
        dateColumn.getStyleClass().add("table-header");
    }

    public static void setFullName(String name) {
        fullName = name;
    }
}
