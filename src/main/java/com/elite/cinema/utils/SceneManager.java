package com.elite.cinema.utils;

import java.io.IOException;

import com.elite.cinema.controllers.BaseController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Pair;

public class SceneManager
{
    public final Stage rootStage;
    private String currentUrl = "";

    public SceneManager(Stage rootStage)
    {
        if (rootStage == null)
        {
            throw new IllegalArgumentException();
        }
        this.rootStage = rootStage;
        rootStage.setTitle("Cinema Manager");
    }

    public void setTitle(String title)
    {
        rootStage.setTitle(title);
    }

    private Pair<Scene, BaseController> loadScene(String url)
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(url));
        try
        {
            Pane p = loader.load();
            BaseController controller = loader.getController();
            controller.setSceneManager(this);
            return new Pair<>(new Scene(p), controller);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public void switchScene(String url)
    {
        if (currentUrl.equals(url)) return;
        currentUrl = url;

        Pair<Scene, BaseController> kv = loadScene(url);
        rootStage.setScene(kv.getKey());
        if (kv.getValue() != null) {
            kv.getValue().ready();
        }
    }
}