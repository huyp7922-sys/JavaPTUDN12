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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoadingController {

    @FXML private ProgressBar progressBar;
    @FXML private Label loadingLabel;
    @FXML private Label percentLabel;

    private Stage stage;
    private LocalDateTime appStartTime;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setAppStartTime(LocalDateTime appStartTime) {
        this.appStartTime = appStartTime;
    }

    @FXML
    public void initialize() {
        startLoading();
    }

    private void startLoading() {
        String[] steps = {
                "Đang khởi động hệ thống...",
                "Đang kết nối cơ sở dữ liệu...",
                "Đang tải cấu hình...",
                "Đang khởi tạo giao diện...",
                "Hoàn tất khởi động..."
        };

        Timeline timeline = new Timeline();

        for (int i = 0; i <= 100; i++) {
            final int percent = i;
            final int stepIndex = Math.min(i / 25, steps.length - 1);

            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(i * 30), e -> {
                        progressBar.setProgress(percent / 100.0);
                        percentLabel.setText(percent + "%");
                        loadingLabel.setText(steps[stepIndex]);

                        if (percent == 100) {
                            Platform.runLater(this::openLoginScreen);
                        }
                    })
            );
        }
        timeline.play();
    }

    private void openLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/login.fxml")
            );
            Parent root = loader.load();

            Scene scene = new Scene(root, 1040, 585);
            Stage loginStage = new Stage();
            loginStage.setTitle("Đăng nhập hệ thống");
            loginStage.setScene(scene);
            loginStage.setResizable(false);

            stage.close();
            loginStage.show();

            System.out.println("App mở lúc: " +
                    appStartTime.format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"))
            );

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}