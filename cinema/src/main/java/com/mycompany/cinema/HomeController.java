package com.mycompany.cinema;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomeController {

    @FXML
    private Label fullNameLabel; // Nhận Label từ FXML

    private static String fullName; // Biến lưu tên người dùng

    public static void setFullName(String name) {
        fullName = name;
    }

    @FXML
    public void initialize() {
        if (fullNameLabel != null && fullName != null) {
            fullNameLabel.setText( fullName);
        }
    }
}
