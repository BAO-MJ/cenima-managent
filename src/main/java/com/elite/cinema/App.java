package com.elite.cinema;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("login"), 800, 400);

        stage.setScene(scene);
        stage.setTitle("Cinema Application");

        // Tắt phóng to
        stage.setResizable(false);

        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/elite/cinema/" + fxml + ".fxml"));

        if (fxmlLoader.getLocation() == null) {
            throw new IOException("Could not find FXML file: " + fxml + ".fxml");
        }
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
        DatabaseConnection.createSignupTable();
        System.exit(0);
    }
}
