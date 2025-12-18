package com.ptudn12.main;

import com.ptudn12.main.controller.LoadingController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalDateTime;

public class LoginApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            LocalDateTime appStartTime = LocalDateTime.now();

            FXMLLoader loader = new FXMLLoader(
                    LoginApp.class.getResource("/views/loading.fxml")
            );
            Parent root = loader.load();

            LoadingController controller = loader.getController();
            controller.setStage(stage);
            controller.setAppStartTime(appStartTime);

            Scene scene = new Scene(root, 600, 400);
            stage.setTitle("Đang khởi động hệ thống...");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
