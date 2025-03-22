package com.elite.cinema;

import com.elite.cinema.models.tables.pojos.Users;
import javafx.application.Application;
import javafx.stage.Stage;

import com.elite.cinema.utils.SceneManager;
import com.elite.cinema.db.DbSet;

import atlantafx.base.theme.PrimerDark;

public class App extends Application {

    public static Users user;

    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        var sceneManager = new SceneManager(stage);
        sceneManager.switchScene("/com/elite/cinema/login.fxml");

        stage.show();
    }

    public static void main(String[] args) {
        DbSet.initialize();
        launch();
    }
}
