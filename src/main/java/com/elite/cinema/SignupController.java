/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.elite.cinema;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class SignupController {

    public void addUser(String fullName, String phoneNumber, String email, String password) {
        String insertSQL = "INSERT INTO signup (full_name, phone_number, email, password) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            preparedStatement.setString(1, fullName);
            preparedStatement.setString(2, phoneNumber);
            preparedStatement.setString(3, email);
            preparedStatement.setString(4, password);

            preparedStatement.executeUpdate();
            System.out.println("User đã được thêm thành công!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
 