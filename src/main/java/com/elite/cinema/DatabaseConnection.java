/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.elite.cinema;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:database.db";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL);
            System.out.println("Kết nối thành công với SQLite!");
        } catch (SQLException e) {
            System.out.println("Kết nối thất bại: " + e.getMessage());
        }
        return connection;
    }

    public static void createSignupTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS signup ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "full_name TEXT NOT NULL, "
                + "phone_number TEXT NOT NULL, "
                + "email TEXT UNIQUE NOT NULL, "
                + "password TEXT NOT NULL"
                + ");";

        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);
            System.out.println("Bảng 'signup' đã được tạo (nếu chưa tồn tại).");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
