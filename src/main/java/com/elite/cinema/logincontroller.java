package com.elite.cinema;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class logincontroller {

    @FXML
    private Button backtologin;

    @FXML
    private TextField emailField;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private AnchorPane formlogin;

    @FXML
    private AnchorPane formsignup;

    @FXML
    private Button loginButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button registerButton;

    @FXML
    private Button registerbutton;

    @FXML
    private DatePicker sp_dateOfBirth;

    @FXML
    private TextField sp_email;

    @FXML
    private TextField sp_fullname;

    @FXML
    private PasswordField sp_password;

    @FXML
    private TextField sp_phone;

    @FXML
    private ImageView checkbok;

    @FXML
    private Button backToLoginFromForgot;

    @FXML
    private Button changebutton;

    @FXML
    private PasswordField fp_confirmpass;
    @FXML
    private TextField fp_email;

    @FXML
    private PasswordField fp_pasword;
    @FXML
    private AnchorPane formforgotpass;

    @FXML
    public void initialize() {
        formlogin.setVisible(true);
        formsignup.setVisible(false);
        formforgotpass.setVisible(false);
    }

    public void addUser() {
        String fullname = sp_fullname.getText().trim();
        String phone = sp_phone.getText().trim();
        String email = sp_email.getText().trim();
        String password = sp_password.getText().trim();

        if (fullname.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Lỗi đăng ký", "Vui lòng nhập đầy đủ thông tin!", Alert.AlertType.WARNING);
            return;
        }

        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            showAlert("Lỗi đăng ký", "Email không hợp lệ!", Alert.AlertType.ERROR);
            return;
        }

        if (!phone.matches("^\\d{10,11}$")) {
            showAlert("Lỗi đăng ký", "Số điện thoại không hợp lệ!", Alert.AlertType.ERROR);
            return;
        }

        if (password.length() < 6) {
            showAlert("Lỗi đăng ký", "Mật khẩu phải có ít nhất 6 ký tự!", Alert.AlertType.ERROR);
            return;
        }

        String checkUserSQL = "SELECT * FROM signup WHERE email = ? OR phone_number = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement checkStmt = conn.prepareStatement(checkUserSQL)) {

            checkStmt.setString(1, email);
            checkStmt.setString(2, phone);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                showAlert("Lỗi đăng ký", "Email hoặc số điện thoại đã tồn tại!", Alert.AlertType.ERROR);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi hệ thống", "Không thể kiểm tra tài khoản. Vui lòng thử lại!", Alert.AlertType.ERROR);
            return;
        }

        String insertSQL = "INSERT INTO signup (full_name, phone_number, email, password) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setString(1, fullname);
            pstmt.setString(2, phone);
            pstmt.setString(3, email);
            pstmt.setString(4, password);
            pstmt.executeUpdate();

            showAlert("Registration successful", "Account created successfully!", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi đăng ký", "Không thể đăng ký tài khoản. Vui lòng thử lại!", Alert.AlertType.ERROR);
        }
    }



public void handleLogin(ActionEvent event) {
    String emailOrPhone = emailField.getText().trim();
    String password = passwordField.getText().trim();

    if (emailOrPhone.isEmpty() || password.isEmpty()) {
        showAlert("Lỗi đăng nhập", "Vui lòng nhập đầy đủ thông tin!", Alert.AlertType.WARNING);
        return;
    }

    String sql = "SELECT full_name FROM signup WHERE (email = ? OR phone_number = ?) AND password = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, emailOrPhone);
        pstmt.setString(2, emailOrPhone);
        pstmt.setString(3, password);

        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            String fullName = rs.getString("full_name");
             HomeController.setFullName(fullName);
             AddMoviesController.setFullName(fullName);
             AvailableMoviesController.setFullName(fullName);
             EditScreeningController.setFullName(fullName);
             CustomerController.setFullName(fullName);
            // Hiển thị thông báo đăng nhập thành công
            showAlert("Login successful", "Welcome, " + fullName + "!", Alert.AlertType.INFORMATION);

            // Load home.fxml thay vì header.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/elite/cinema/home.fxml"));
            Parent root = loader.load();

            // Lấy HomeController
           

            // Gửi dữ liệu sang HomeController (để HomeController cập nhật header)
           

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } else {
            showAlert("Lỗi đăng nhập", "Sai thông tin đăng nhập!", Alert.AlertType.ERROR);
        }
    } catch (SQLException | IOException e) {
        e.printStackTrace();
        showAlert("Lỗi hệ thống", "Không thể thực hiện đăng nhập. Vui lòng thử lại sau!", Alert.AlertType.ERROR);
    }
}



    
       public void handleChangePassword() {
        String emailOrPhone = fp_email.getText().trim();
        String newPassword = fp_pasword.getText().trim();
        String confirmPassword = fp_confirmpass.getText().trim();

        if (emailOrPhone.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin!", Alert.AlertType.WARNING);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert("Lỗi", "Mật khẩu xác nhận không khớp!", Alert.AlertType.ERROR);
            return;
        }

        // Kiểm tra xem người dùng có tồn tại
        String sqlCheckUser = "SELECT * FROM signup WHERE email = ? OR phone_number = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlCheckUser)) {

            pstmt.setString(1, emailOrPhone);
            pstmt.setString(2, emailOrPhone);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Cập nhật mật khẩu mới
                String sqlUpdatePassword = "UPDATE signup SET password = ? WHERE email = ? OR phone_number = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(sqlUpdatePassword)) {
                    updateStmt.setString(1, newPassword);
                    updateStmt.setString(2, emailOrPhone);
                    updateStmt.setString(3, emailOrPhone);
                    updateStmt.executeUpdate();
                    showAlert("Success", "Password changed successfully!", Alert.AlertType.INFORMATION);
                }
            } else {
                showAlert("Lỗi", "Không tìm thấy tài khoản với thông tin đã cung cấp!", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi hệ thống", "Có lỗi xảy ra. Vui lòng thử lại sau!", Alert.AlertType.ERROR);
        }
    }
       
      private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleForgotPassword() {
       
        formlogin.setVisible(false);

        formforgotpass.setVisible(true);

    }

    @FXML
    private void handleBackToLoginFromForgot() {
        
        formforgotpass.setVisible(false);

        formlogin.setVisible(true);

    }

    @FXML
    private void handleRegister() {

        formlogin.setVisible(false);

      
        formsignup.setVisible(true);
    }


    @FXML
    private void handleBackToLogin() {
    
        formsignup.setVisible(false);

        // Hiển thị form đăng nhập
        formlogin.setVisible(true);

    }

}
