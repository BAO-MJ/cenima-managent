/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.elite.cinema;


import javafx.fxml.FXML;

import javafx.scene.control.Label;



/**
 *
 * @author ACER
 */
public class AddMoviesController {

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
