package com.elite.cinema.controllers.user;

import atlantafx.base.theme.Styles;
import com.elite.cinema.controllers.MainController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class OrderSuccessController extends MainController
{
    @FXML
    private Button continueButton;

    public void initialize()
    {
        continueButton.getStyleClass().add(Styles.SUCCESS);
    }

    public void onContinueClicked()
    {
        changeScene.accept("user/refreshments-order.fxml", null);
    }
}
