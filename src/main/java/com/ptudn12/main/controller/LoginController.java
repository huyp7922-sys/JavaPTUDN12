package com.ptudn12.main.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", 
                     "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (authenticate(username, password)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
                Parent root = loader.load();
                
                DashboardController controller = loader.getController();
                controller.setUsername(username);
                
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Hệ Thống Quản Lý Bán Vé Tàu");
                stage.setMaximized(true);
                
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", 
                         "Không thể tải giao diện chính!");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", 
                     "Tên đăng nhập hoặc mật khẩu không đúng!");
        }
    }

    private boolean authenticate(String username, String password) {
        return username.equals("admin") && password.equals("admin123");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}