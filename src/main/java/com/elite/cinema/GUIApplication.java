package com.elite.cinema;

import atlantafx.base.theme.PrimerDark;
import com.elite.cinema.utils.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class GUIApplication extends Application
{

    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        var sceneManager = new SceneManager(stage);
        sceneManager.switchScene("/com/elite/cinema/login.fxml");

        stage.show();
    }

}
