package com.ptudn12.main;

import com.ptudn12.main.controller.LoadingController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load màn hình loading trước
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/loading.fxml"));
        Parent root = loader.load();
        
        // Lấy controller và set stage
        LoadingController controller = loader.getController();
        controller.setStage(stage);
        
        Scene scene = new Scene(root, 600, 400);
        
        stage.setTitle("Đang tải...");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.initStyle(StageStyle.UNDECORATED); // Không có thanh title bar cho loading screen
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}