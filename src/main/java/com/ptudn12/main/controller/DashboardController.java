package com.ptudn12.main.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardController {

    @FXML private StackPane contentPane;
    @FXML private Label pageTitle;
    @FXML private Label usernameLabel;
    @FXML private Label dateTimeLabel;
    
    @FXML private Button btnHome;
    @FXML private Button btnProfile;
    @FXML private Button btnSettings;

    private String currentUser;

    @FXML
    public void initialize() {
        // Update clock every second
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dateTimeLabel.setText(LocalDateTime.now().format(formatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        // Load home view by default
        showHome();
    }

    public void setUsername(String username) {
        this.currentUser = username;
        usernameLabel.setText("Xin chào, " + username);
    }

    @FXML
    private void showHome() {
        pageTitle.setText("Trang chủ");
        resetMenuButtons();
        btnHome.setStyle("-fx-background-color: #3498db;");
        
        loadView("home.fxml");
    }

    @FXML
    private void showProfile() {
        pageTitle.setText("Hồ sơ cá nhân");
        resetMenuButtons();
        btnProfile.setStyle("-fx-background-color: #3498db;");
        
        loadView("ProfileView.fxml");
    }

    @FXML
    private void showSettings() {
        pageTitle.setText("Cài đặt");
        resetMenuButtons();
        btnSettings.setStyle("-fx-background-color: #3498db;");
        
        loadView("SettingsView.fxml");
    }

    @FXML
    private void showReports() {
        pageTitle.setText("Báo cáo");
        resetMenuButtons();
        
        loadView("ReportsView.fxml");
    }

    @FXML
    private void showUsers() {
        pageTitle.setText("Quản lý người dùng");
        resetMenuButtons();
        
        loadView("UsersView.fxml");
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận đăng xuất");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                // Load login screen
                Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
                Stage stage = (Stage) contentPane.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Đăng Nhập Hệ Thống");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadView(String fxmlFile) {
        try {
            Node view = FXMLLoader.load(getClass().getResource("/views/" + fxmlFile));
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
        } catch (IOException e) {
            Label placeholder = new Label("📄 Nội dung đang được phát triển...");
            placeholder.setStyle("-fx-font-size: 20px; -fx-text-fill: #95a5a6;");
            contentPane.getChildren().clear();
            contentPane.getChildren().add(placeholder);
        }
    }

    private void resetMenuButtons() {
        btnHome.setStyle("");
        btnProfile.setStyle("");
        btnSettings.setStyle("");
    }
}