package com.elite.cinema.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.util.Pair;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LayoutController extends BaseController {
    @FXML
    private StackPane content;
    private final Map<String, Pair<Pane, MainController>> scenes = new HashMap<>();

    private MainController mainController;
    private String path = "";

    public LayoutController()
    {
        preloadScenes();
    }

    public void preloadScenes() {
        var urls = new String[] { "user/available-movies.fxml", "admin-home.fxml", "admin/movies.fxml", "admin/screening.fxml", "admin/refreshments.fxml" };
        for (String url : urls) {
            scenes.put(url, loadScene(url));
        }
    }

    public Pair<Pane, MainController> loadScene(String url)
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/elite/cinema/" + url));
        try
        {
            Pane pane = loader.load();
            MainController controller = loader.getController();
            if (controller != null)
            {
                controller.changeScene = this::switchScene;
            }

            return new Pair<>(pane, controller);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public void switchScene(String path) {
        switchScene(path, null);
    }

    @SneakyThrows
    public void switchScene(String path, Object params) {
        if (this.path.equals(path)) return;
        this.path = path;

        if (mainController != null)
        {
            mainController.dispose();
        }

        var pair = scenes.computeIfAbsent(path, this::loadScene);

        content.getChildren().setAll(pair.getKey());
        mainController = pair.getValue();
        if (mainController != null)
        {
            mainController.ready(params);
        }
    }

    public void initialize() {
        Platform.runLater(() -> {
            var root = getSceneManager().rootStage;
            root.setWidth(1280);
            root.setHeight(800);

            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            root.setX((bounds.getWidth() - root.getWidth()) / 2);
            root.setY((bounds.getHeight() - root.getHeight()) / 2);
        });
    }

    public void onHomeClicked() { switchScene("admin-home.fxml"); }

    public void onMoviesClicked() { switchScene("admin/movies.fxml"); }

    public void onTicketsClicked() { switchScene("user/available-movies.fxml"); }

    public void onScreeningClicked() { switchScene("admin/screening.fxml"); }

    public void onRefreshmentsClicked() { switchScene("admin/refreshments.fxml"); }

    public void onUserClicked() {

    }
}
