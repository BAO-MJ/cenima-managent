package com.mycompany.cinema;

import java.io.IOException;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class HeaderController {

    @FXML
    private Label menu_addmovies;

    @FXML
    private Label menu_availablemovies;

    @FXML
    private Label menu_customer;

    @FXML
    private Label menu_editscreening;

    @FXML
    private Label menu_home;

    @FXML
    private Label menu_signout;

    @FXML
    private Label usernameLabel;

    @FXML
    void switchToHome(MouseEvent event) {
        switchScene(event, "home.fxml");
    }

    @FXML
    void switchToaddmovies(MouseEvent event) {
        switchScene(event, "addMovies.fxml");
    }

    @FXML
    void switchToavailabel(MouseEvent event) {
        switchScene(event, "availableMovies.fxml");
    }

    @FXML
    void switchToeditsreening(MouseEvent event) {
        switchScene(event, "editScreening.fxml");
    }

    @FXML
    void switchTocustomer(MouseEvent event) {
        switchScene(event, "customer.fxml");
    }

    @FXML
    private void handleLogout() throws IOException {
        // Hiển thị hộp thoại xác nhận
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm logout");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to log out?");

        // Lấy kết quả từ hộp thoại
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Đóng cửa sổ hiện tại nếu người dùng chọn OK
            Stage stage = (Stage) menu_signout.getScene().getWindow();
            stage.close();

            // Mở lại màn hình đăng nhập
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage loginStage = new Stage();
            loginStage.setScene(scene);
            loginStage.setTitle("Login");
            loginStage.show();
        }
    }

    private void switchScene(MouseEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/cinema/" + fxmlFile));
            Parent root = loader.load();

            // Lấy Stage từ sự kiện chuột và thay đổi Scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFullName(String fullName) {
        if (usernameLabel != null) {
            usernameLabel.setText(fullName);
        }
    }
}
