package com.elite.cinema;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        UserAgentBuilder.builder()
        .themes(JavaFXThemes.MODENA) // Optional if you don't need JavaFX's default theme, still recommended though
        .themes(MaterialFXStylesheets.forAssemble(true)) // Adds the MaterialFX's default theme. The boolean argument is to include legacy controls
        .setDeploy(true) // Whether to deploy each theme's assets on a temporary dir on the disk
        .setResolveAssets(true) // Whether to try resolving @import statements and resources urls
        .build() // Assembles all the added themes into a single CSSFragment (very powerful class check its documentation)
        .setGlobal(); // Finally, sets the produced stylesheet as the global User-Agent stylesheet
        
        scene = new Scene(loadFXML("login"), 800, 400);

        stage.setScene(scene);
        stage.setTitle("Cinema Application");

        // Tắt phóng to
        stage.setResizable(false);

        stage.show();
    }

    static void setRoot(String fxml) throws IOException { scene.setRoot(loadFXML(fxml)); }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/elite/cinema/" + fxml + ".fxml"));

        if (fxmlLoader.getLocation() == null) {
            throw new IOException("Could not find FXML file: " + fxml + ".fxml");
        }
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
