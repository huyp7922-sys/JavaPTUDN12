package com.ptudn12.main.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoadingController {

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label loadingLabel;

    @FXML
    private Label percentLabel;

    private Stage stage;
    private double progress = 0;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        // Bắt đầu loading animation
        startLoading();
    }

    private void startLoading() {
        // Các bước loading với message khác nhau
        String[] loadingSteps = {
            "Đang khởi động hệ thống...",
            "Đang kết nối cơ sở dữ liệu...",
            "Đang tải cấu hình...",
            "Đang khởi tạo giao diện...",
            "Đang chuẩn bị tài nguyên...",
            "Hoàn tất khởi động..."
        };

        Timeline timeline = new Timeline();
        
        // Tạo animation chạy từ 0-100%
        for (int i = 0; i <= 100; i++) {
            final int currentProgress = i;
            final int stepIndex = Math.min((i / 17), loadingSteps.length - 1); // Chia làm nhiều step
            
            KeyFrame keyFrame = new KeyFrame(
                Duration.millis(i * 30), // Tổng thời gian: 100 * 30 = 3000ms = 3 giây
                event -> {
                    progress = currentProgress / 100.0;
                    progressBar.setProgress(progress);
                    percentLabel.setText(currentProgress + "%");
                    loadingLabel.setText(loadingSteps[stepIndex]);
                    
                    // Khi đạt 100%, chuyển sang màn hình login
                    if (currentProgress == 100) {
                        Platform.runLater(() -> {
                            try {
                                // Delay một chút để user thấy 100%
                                Thread.sleep(500);
                                openLoginScreen();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            );
            
            timeline.getKeyFrames().add(keyFrame);
        }
        
        timeline.play();
    }

    private void openLoginScreen() {
        try {
            // Load màn hình login
            Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
            Scene scene = new Scene(root, 1040, 585);
            
            // Đóng loading screen và mở login screen
            Stage loginStage = new Stage();
            loginStage.setTitle("Đăng Nhập Hệ Thống");
            loginStage.setScene(scene);
            loginStage.setResizable(false);
            
            // Đóng loading window
            stage.close();
            
            // Hiển thị login window
            loginStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
