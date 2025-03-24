package com.elite.cinema;

import com.elite.cinema.models.tables.pojos.Users;

import com.elite.cinema.db.DbSet;

public class App {
    public static Users user;

    public static void main(String[] args) {
        DbSet.initialize();
        GUIApplication.launch(GUIApplication.class, args);
    }
}
