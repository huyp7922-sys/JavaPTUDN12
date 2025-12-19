package com.ptudn12.main;

import com.ptudn12.main.controller.LoadingController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.nio.file.Files;
import java.nio.file.Path;
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
        System.out.println("\n****************************************************");
        System.out.println("*                                                  *");
        System.out.println("*     HE THONG BAN VE TAU - STARTING UP...        *");
        System.out.println("*                                                  *");
        System.out.println("****************************************************\n");
        
        try {
            String desktop = System.getProperty("user.home") + "\\Desktop";
            Files.writeString(Path.of(desktop, "HeThongBanVeTau-launched.txt"), 
                "Application launched at: " + LocalDateTime.now() + "\n" +
                "Java Version: " + System.getProperty("java.version") + "\n" +
                "Working Directory: " + System.getProperty("user.dir") + "\n");
            System.out.println("✓ Launch marker created on Desktop");
        } catch (Exception e) {
            System.err.println("✗ Cannot create launch marker: " + e.getMessage());
        }
        
        System.out.println("\nInitializing JavaFX Application...\n");
        launch(args);
    }
}
