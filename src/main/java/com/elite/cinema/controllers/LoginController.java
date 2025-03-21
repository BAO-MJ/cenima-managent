package com.elite.cinema.controllers;

import com.elite.cinema.db.DbSet;
import com.elite.cinema.models.enums.UsersType;

import static com.elite.cinema.models.Tables.*;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;

public class LoginController extends BaseController {
    private static final String EMAIL_REGEX = "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$";
    private static final String PHONE_REGEX = "^0\\d{9}$";
    @FXML
    private VBox loginForm;

    @FXML
    private VBox signupForm;

    @FXML
    private VBox forgotPasswordForm;

    public void initialize() { toggleForm(true, false, false); }

    @FXML
    private TextField loginUsername;

    @FXML
    private PasswordField loginPassword;

    public void onLogin() {
        var context = DbSet.getContext();

        if (loginUsername.getText().isEmpty() || loginPassword.getText().isEmpty()) {
            showAlert("Login error", "Please fill in all fields", AlertType.ERROR);
            return;
        }

        if (!loginUsername.getText().matches(EMAIL_REGEX) && !loginUsername.getText().matches(PHONE_REGEX)) {
            showAlert("Login error", "Invalid email address or phone number", AlertType.ERROR);
            return;
        }

        if (loginPassword.getText().length() < 8 || loginPassword.getText().length() > 50) {
            showAlert("Login error", "Password must be between 8 to 50 characters long", AlertType.ERROR);
            return;
        }

        var user = context.selectFrom(USERS)
                .where(USERS.EMAIL.eq(loginUsername.getText()).or(USERS.PHONE_NUMBER.eq(loginUsername.getText())))
                .fetchOne();

        if (user == null) {
            showAlert("Login error", "Username does not exists", AlertType.ERROR);
            return;
        }

        if (!user.getValue(USERS.PASSWORD).equals(loginPassword.getText())) {
            showAlert("Login error", "Incorrect password", AlertType.ERROR);
            return;
        }

        showAlert("Login successfully", String.format("Welcome, %s", user.getValue(USERS.NAME)), AlertType.INFORMATION);
        
        getSceneManager().switchScene("/com/elite/cinema/layout.fxml");
    }

    @FXML
    private TextField registerName;

    @FXML
    private TextField registerEmail;

    @FXML
    private PasswordField registerPassword;

    @FXML
    private TextField registerPhone;

    @FXML
    private CheckBox registerTerms;

    public void onRegister() {
        if (registerName.getText().isEmpty() || registerEmail.getText().isEmpty()
                || registerPassword.getText().isEmpty() || registerPhone.getText().isEmpty()) {
            showAlert("Register error", "Please fill in all fields", AlertType.ERROR);
            return;
        }

        if (!registerEmail.getText().matches(EMAIL_REGEX)) {
            showAlert("Register error", "Invalid email address", AlertType.ERROR);
            return;
        }

        if (registerPassword.getText().length() < 8 || registerPassword.getText().length() > 50) {
            showAlert("Login error", "Password must be between 8 to 50 characters long", AlertType.ERROR);
            return;
        }

        if (!registerPhone.getText().matches(PHONE_REGEX)) {
            showAlert("Register error", "Invalid phone number", AlertType.ERROR);
            return;
        }

        if (!registerTerms.isSelected()) {
            showAlert("Register error", "Please accept the terms and conditions", AlertType.ERROR);
            return;
        }

        var context = DbSet.getContext();
        var user = context.selectFrom(USERS)
                .where(USERS.EMAIL.eq(registerEmail.getText()).or(USERS.PHONE_NUMBER.eq(registerPhone.getText())))
                .fetchOne();

        if (user != null) {
            showAlert("Register error", "User already exists", AlertType.ERROR);
            return;
        }

        context.insertInto(USERS, USERS.NAME, USERS.EMAIL, USERS.PASSWORD, USERS.PHONE_NUMBER, USERS.TYPE)
                .values(registerName.getText(), registerEmail.getText(), registerPassword.getText(),
                        registerPhone.getText(), UsersType.Staff)
                .execute();

        showAlert("Register success", "User registered successfully", AlertType.INFORMATION);
        onReturnToLogin();
    }

    public void onPasswordChanged() {

    }

    private void showAlert(String title, String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void onReturnToLogin() { toggleForm(true, false, false); }

    public void onOpenRegisterForm() { toggleForm(false, true, false); }

    public void onOpenForgotPasswordForm() { toggleForm(false, false, true); }

    private void toggleForm(boolean login, boolean signup, boolean forgot) {
        loginForm.setVisible(login);
        signupForm.setVisible(signup);
        forgotPasswordForm.setVisible(forgot);
    }
}
