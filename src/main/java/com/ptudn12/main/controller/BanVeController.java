/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.controller;

/**
 *
 * @author fo3cp
 */

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class BanVeController {
    @FXML private StackPane contentPane;
    @FXML private Label dateTimeLabel;
    
    private Step1Controller step1ControllerInstance;
    private Step2Controller step2ControllerInstance;
    private Step3Controller step3ControllerInstance;
    private Step4Controller step4ControllerInstance;

    // Menu items
    @FXML private TitledPane menuVeTau;
    @FXML private Button btnBanVe;
    @FXML private Button btnDoiVe;
    @FXML private Button btnTraVe;
    @FXML private Button btnKhachHang;
    @FXML private Button btnHoaDon;
    @FXML private Button btnThongKe;
    @FXML private Button btnLogout;
    
    private String currentUser;

    // BẮT BUỘC CÓ: Map để lưu data giữa các step
    private Map<String, Object> userData = new HashMap<>();

    @FXML
    public void initialize() {
        // Update đồng hồ
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dateTimeLabel.setText(LocalDateTime.now().format(formatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        // Mặc định tải trang Bán vé (Step 1)
        showBanVe();
        menuVeTau.setExpanded(true); // Mở sẵn menu vé
    }

    public void setUsername(String username) {
        this.currentUser = username;
    }
    
    // --- Các hàm xử lý menu ---
    @FXML
    private void showBanVe() {
        resetMenuButtons();
        btnBanVe.getStyleClass().add("menu-item-active");
        loadContent("step-1.fxml");
    }

    @FXML
    private void showDoiVe() {
        resetMenuButtons();
        btnDoiVe.getStyleClass().add("menu-item-active");
        loadContent("doi-ve.fxml"); // (Sẽ hiện placeholder)
    }
    
    @FXML
    private void showTraVe() {
        resetMenuButtons();
        btnTraVe.getStyleClass().add("menu-item-active");
        loadContent("tra-ve.fxml"); // (Sẽ hiện placeholder)
    }

    @FXML
    private void showKhachHang() {
        resetMenuButtons();
        btnKhachHang.getStyleClass().add("menu-item-active");
        loadContent("customer-management.fxml"); // (Sẽ hiện placeholder)
    }

    @FXML
    private void showHoaDon() {
        resetMenuButtons();
        btnHoaDon.getStyleClass().add("menu-item-active");
        loadContent("invoice-management.fxml"); // (Sẽ hiện placeholder)
    }

    @FXML
    private void showThongKe() {
        resetMenuButtons();
        btnThongKe.getStyleClass().add("menu-item-active");
        loadContent("statistics-management.fxml"); // (Sẽ hiện placeholder)
    }
    
    // HÀM QUAN TRỌNG: Tải FXML vào vùng nội dung
    public void loadContent(String fxmlFile) {
        try {
        // Check if file exists
        if (getClass().getResource("/views/" + fxmlFile) == null) {
            showPlaceholder("Chức năng chưa được tạo",
                    "Trang này đang được phát triển.\nVui lòng quay lại sau!");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/" + fxmlFile));
        Node view = loader.load();
        Object controller = loader.getController();

        // Lưu lại instance của controller
        if (controller instanceof Step1Controller) {
            step1ControllerInstance = (Step1Controller) controller;
            step1ControllerInstance.setMainController(this);
            step1ControllerInstance.initData();
        } else if (controller instanceof Step2Controller) {
            step2ControllerInstance = (Step2Controller) controller;
            step2ControllerInstance.setMainController(this);
            step2ControllerInstance.initData();
        } else if (controller instanceof Step3Controller) {
            step3ControllerInstance = (Step3Controller) controller;
            step3ControllerInstance.setMainController(this);
            step3ControllerInstance.initData();
        } else if (controller instanceof Step4Controller) {
            step4ControllerInstance = (Step4Controller) controller;
            step4ControllerInstance.setMainController(this);
            step4ControllerInstance.initData();
        }

        contentPane.getChildren().clear();
        contentPane.getChildren().add(view);

    } catch (IOException e) {
        e.printStackTrace();
        showPlaceholder("Lỗi khi tải giao diện",
                "Không thể tải file: " + fxmlFile + "\n\n"
                + "Chi tiết lỗi: " + e.getMessage());
    }
    }

    @FXML
    public void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận đăng xuất");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
                Stage stage = (Stage) contentPane.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Đăng Nhập Hệ Thống");
                stage.setMaximized(false); 
            } catch (IOException e) {
                e.printStackTrace();
                showError("Lỗi khi đăng xuất: " + e.getMessage());
            }
        }
    }
    
    /**
    * Hàm chuyển tiếp yêu cầu hủy vé từ Step 3 đến Step 2
    */
    public void requestCancelTicketInCart(int maCho, boolean isChieuDi) {
        if (step2ControllerInstance != null) {
            step2ControllerInstance.cancelTicketBySeatId(maCho, isChieuDi);
        } else {
            System.err.println("BanVeController: Step2Controller chưa được khởi tạo, không thể hủy vé.");
        }
    }
    
    // --- BẮT BUỘC CÓ: Các hàm này để truyền data giữa các step ---
    public void setUserData(String key, Object data) {
        userData.put(key, data);
    }

    public Object getUserData(String key) {
        return userData.get(key);
    }

    // --- Các hàm tiện ích (Placeholder, Reset, Error) ---

    private void showPlaceholder(String title, String message) {
        VBox placeholder = new VBox(25);
        placeholder.setStyle("-fx-alignment: center; -fx-padding: 50;");
        Label icon = new Label("⚠️");
        icon.setStyle("-fx-font-size: 80px;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e67e22;");
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d; -fx-text-alignment: center;");
        placeholder.getChildren().addAll(icon, titleLabel, messageLabel);
        contentPane.getChildren().clear();
        contentPane.getChildren().add(placeholder);
    }

    private void resetMenuButtons() {
        String activeClass = "menu-item-active";
        btnBanVe.getStyleClass().remove(activeClass);
        btnDoiVe.getStyleClass().remove(activeClass);
        btnTraVe.getStyleClass().remove(activeClass);
        btnKhachHang.getStyleClass().remove(activeClass);
        btnHoaDon.getStyleClass().remove(activeClass);
        btnThongKe.getStyleClass().remove(activeClass);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
