package com.elite.cinema.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class SceneManager
{
    final Stage rootStage;
    private String currentUrl = "";

    public SceneManager(Stage rootStage)
    {
        if (rootStage == null)
        {
            throw new IllegalArgumentException();
        }
        this.rootStage = rootStage;
    }

    public void setTitle(String title)
    {
        rootStage.setTitle(title);
    }

    private final Map<String, Scene> scenes = new HashMap<>();

    public void switchScene(String url)
    {
        switchScene(url, true);
    }

    private Scene loadScene(String url)
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(url));
        try
        {
            Pane p = loader.load();
            BaseController controller = loader.getController();
            controller.setSceneManager(this);
            return new Scene(p);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public void switchScene(String url, boolean cache)
    {
        if (currentUrl.equals(url)) return;
        currentUrl = url;

        Scene scene = cache ? scenes.computeIfAbsent(url, this::loadScene) : loadScene(url);
        rootStage.setScene(scene);
    }
}