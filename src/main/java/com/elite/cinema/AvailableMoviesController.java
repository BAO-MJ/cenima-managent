package com.elite.cinema;

import javafx.fxml.FXML;
import javafx.scene.control.Label;


public class AvailableMoviesController {

    @FXML
    private Label fullNameLabel;

    private static String fullName;

    @FXML
    public void initialize() {
        if (fullNameLabel != null && fullName != null) {
            fullNameLabel.setText(fullName);
        }
     

    }

    public static void setFullName(String name) {
        fullName = name;
    }
}
