package com.elite.cinema.controllers;

import com.elite.cinema.App;
import com.elite.cinema.models.enums.UsersType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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

    @FXML
    private Button adminHome;

    @FXML
    private Button adminMovies;

    @FXML
    private Button staffMovies;

    @FXML
    private Button adminScreenings;

    @FXML
    private Button adminRefreshments;

    @FXML
    private Button users;

    @FXML
    private Button adminRevenueReports;

    @FXML
    private Button staffRefreshments;

    @FXML
    private VBox menuButtons;

    @FXML
    private Label userName;

    private MainController mainController;
    private String path = "";

    public LayoutController()
    {
        preloadScenes();
    }

    public void ready() {
        var root = getSceneManager().rootStage;
        root.setWidth(1280);
        root.setHeight(800);

        userName.setText(App.user != null ? App.user.getName() : "");

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        root.setX((bounds.getWidth() - root.getWidth()) / 2);
        root.setY((bounds.getHeight() - root.getHeight()) / 2);

        if (App.user != null && App.user.getType() == UsersType.Admin)
        {
            menuButtons.getChildren().setAll(adminHome, adminMovies, adminScreenings, adminRefreshments, users, adminRevenueReports);
            onHomeClicked();
        }
        else
        {
            menuButtons.getChildren().setAll(staffMovies, users, staffRefreshments);
            onStaffMoviesClicked();
        }
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

    public void onHomeClicked() { switchScene("admin-home.fxml"); }

    public void onMoviesClicked() { switchScene("admin/movies.fxml"); }

    public void onStaffMoviesClicked() { switchScene("user/available-movies.fxml"); }

    public void onScreeningClicked() { switchScene("admin/screening.fxml"); }

    public void onRefreshmentsClicked() { switchScene("admin/refreshments.fxml"); }

    public void onUserClicked() { switchScene("admin/members.fxml"); }

    public void onRevenueReportsClicked() { switchScene("admin/revenue-report.fxml"); }

    public void onStaffRefreshmentsClicked() { switchScene("user/refreshments-order.fxml"); }

    public void onBookingHistoryClicked() { switchScene("user/customer.fxml"); }

    public void onRefreshmentsHistoryClicked() { switchScene("user/customerfood.fxml"); }

    public void onSignOutClicked() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Sign Out");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to sign out?");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.YES) {
                App.user = null;
                getSceneManager().switchScene("/com/elite/cinema/login.fxml");
            }
        });
    }
}
