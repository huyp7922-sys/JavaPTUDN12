package com.ptudn12.main.controller;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (authenticateAdmin(username, password)) {
            // Đăng nhập với tư cách Quản lý (Admin)
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
                Parent root = loader.load();
                DashboardController controller = loader.getController();
                controller.setUsername(username);

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Hệ Thống Quản Lý Bán Vé Tàu - (Quản Lý)");
                stage.setMaximized(true);

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải giao diện Quản lý!");
            }
            
        } else if (authenticateNV(username, password)) {
            // SỬA: Đăng nhập với tư cách Nhân Viên (Tải file "vỏ" BanVe.fxml)
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ban-ve.fxml")); 
                Parent root = loader.load();

                // SỬA: Lấy Controller "vỏ" BanVeController
                BanVeController controller = loader.getController();
                controller.setUsername(username); 

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Hệ Thống Quản Lý Bán Vé Tàu - (Nhân Viên)");
                stage.setMaximized(true);
                
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải giao diện Nhân viên!");
            }
            
        } else {
            // Sai cả hai
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên đăng nhập hoặc mật khẩu không đúng!");
        }
    }

    // Đổi tên hàm cho rõ ràng
    private boolean authenticateAdmin(String username, String password) {
        return username.equals("admin") && password.equals("admin123");
    }

    private boolean authenticateNV(String username, String password) {
        return username.equals("nhanvien") && password.equals("nhanvien123");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}