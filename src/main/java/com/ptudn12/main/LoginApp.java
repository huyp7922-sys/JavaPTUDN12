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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoginApp extends Application {

    @Override
    public void start(Stage stage) {
        System.out.println("\n========================================");
        System.out.println("=== APPLICATION STARTING ===");
        System.out.println("========================================");
        System.out.println("Time: " + getCurrentTime());
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("JavaFX Version: " + System.getProperty("javafx.version"));
        System.out.println("Working Directory: " + System.getProperty("user.dir"));
        System.out.println("========================================\n");
        
        writeStartupLog("=== APPLICATION STARTING ===");
        writeStartupLog("Time: " + getCurrentTime());
        writeStartupLog("Java Version: " + System.getProperty("java.version"));
        writeStartupLog("JavaFX Version: " + System.getProperty("javafx.version"));
        writeStartupLog("Working Directory: " + System.getProperty("user.dir"));
        
        try {
            System.out.println("[Step 1] Locating FXML resource...");
            var fxmlUrl = LoginApp.class.getResource("/views/loading.fxml");
            if (fxmlUrl == null) {
                throw new RuntimeException("Không tìm thấy resource: /views/loading.fxml");
            }
            System.out.println("[Step 2] Resource found: " + fxmlUrl);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            writeStartupLog("Step 3: Loading FXML content...");
            System.out.println("[Step 3] Loading FXML content...");
            Parent root = loader.load();
            writeStartupLog("Step 4: FXML loaded successfully");
            System.out.println("[Step 4] FXML loaded successfully");

            LoadingController controller = loader.getController();
            if (controller == null) {
                throw new RuntimeException("LoadingController = null (sai fx:controller?)");
            }
            controller.setStage(stage);
            writeStartupLog("Step 5: Controller initialized");
            System.out.println("[Step 5] Controller initialized");

            Scene scene = new Scene(root, 600, 400);
            stage.setTitle("Đang tải...");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initStyle(StageStyle.UNDECORATED);
            writeStartupLog("Step 6: Showing window...");
            System.out.println("[Step 6] Showing window...");
            stage.show();
            
            writeStartupLog("Step 7: Window shown successfully!");
            writeStartupLog("=== APPLICATION STARTED SUCCESSFULLY ===");
            System.out.println("[Step 7] Window shown successfully!");
            System.out.println("\n========================================");
            System.out.println("=== APPLICATION STARTED SUCCESSFULLY ===");
            System.out.println("========================================\n");

        } catch (Throwable e) {
            handleStartupError(e, stage);
        }
    }
    
    private void handleStartupError(Throwable e, Stage stage) {
        try {
            // In ra console với format rõ ràng hơn
            System.err.println("\n========================================");
            System.err.println("!!! ERROR OCCURRED !!!");
            System.err.println("========================================");
            System.err.println("Error Type: " + e.getClass().getName());
            System.err.println("Error Message: " + e.getMessage());
            System.err.println("========================================");
            e.printStackTrace();
            
            // Ghi vào file log
            writeStartupLog("=== ERROR OCCURRED ===");
            writeStartupLog("Error message: " + e.getMessage());
            
            StringWriter sw = new StringWriter();
            sw.write("Thời gian: " + getCurrentTime() + "\n");
            sw.write("Lỗi khi khởi động ứng dụng:\n\n");
            e.printStackTrace(new PrintWriter(sw));

            Files.writeString(
                Path.of("startup-error.log"),
                sw.toString()
            );
            writeStartupLog("Error details written to startup-error.log");
            
            // Hiển thị dialog lỗi
            Platform.runLater(() -> showErrorDialog(e));
            
        } catch (Exception logError) {
            logError.printStackTrace();
            try {
                Files.writeString(Path.of("critical-error.log"), 
                    "Failed to handle error properly: " + logError.getMessage());
            } catch (Exception ignored) {}
        }
    }
    
    private void showErrorDialog(Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi Khởi Động");
        alert.setHeaderText("Không thể khởi động ứng dụng!");
        alert.setContentText("Lỗi: " + e.getMessage());

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().setExpanded(true);
        
        alert.showAndWait();
        System.exit(1);
    }
    
    private String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    private void writeStartupLog(String message) {
        try {
            // Ghi vào Desktop để dễ tìm
            String desktop = System.getProperty("user.home") + "\\Desktop";
            String logMessage = "[" + getCurrentTime() + "] " + message + "\n";
            Path logPath = Path.of(desktop, "HeThongBanVeTau-startup.log");
            
            if (Files.exists(logPath)) {
                Files.writeString(logPath, logMessage, 
                    java.nio.file.StandardOpenOption.APPEND);
            } else {
                Files.writeString(logPath, logMessage);
            }
        } catch (Exception e) {
            // Try user home if desktop fails
            try {
                String home = System.getProperty("user.home");
                String logMessage = "[" + getCurrentTime() + "] " + message + "\n";
                Path logPath = Path.of(home, "HeThongBanVeTau-startup.log");
                
                if (Files.exists(logPath)) {
                    Files.writeString(logPath, logMessage, 
                        java.nio.file.StandardOpenOption.APPEND);
                } else {
                    Files.writeString(logPath, logMessage);
                }
            } catch (Exception ex) {
                System.err.println("Failed to write to log: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("\n****************************************************");
        System.out.println("*                                                  *");
        System.out.println("*     HE THONG BAN VE TAU - STARTING UP...        *");
        System.out.println("*                                                  *");
        System.out.println("****************************************************\n");
        
        // Tạo file marker trên Desktop để biết app đã được khởi chạy
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
