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
import com.ptudn12.main.entity.NhanVien;
public class BanVeController {
    @FXML private StackPane contentPane;
    @FXML private Label dateTimeLabel;
    
//    Cũ:
//    private Step1Controller step1ControllerInstance;
//    private Step2Controller step2ControllerInstance;
//    private Step2Controller_update step2Controller_updateInstance;
//    private Step3Controller step3ControllerInstance;
//    private Step4Controller step4ControllerInstance;
    
    private NhanVien nhanVien;
    // --- CACHE: Lưu trữ Controller và View của từng Step ---
    private Step1Controller step1ControllerInstance;
    private Node step1View;
    
    private Step2Controller_update step2ControllerInstance;
    private Node step2View;
    
    private Step3Controller step3ControllerInstance;
    private Node step3View;
    
    private Step4Controller step4ControllerInstance;
    private Node step4View;

    // Menu items
    @FXML private TitledPane menuVeTau;
    @FXML private Button btnDashboard;
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
    private void showDashboard() {
        resetMenuButtons();
        btnDashboard.getStyleClass().add("menu-item-active");
        loadContent("employee-dashboard-content.fxml");
    }
    @FXML
    private void showBanVe() {
        resetMenuButtons();
        btnBanVe.getStyleClass().add("menu-item-active");
        // Khi bấm vào menu Bán Vé, ta reset lại quy trình từ đầu (Step 1)
        // Hoặc có thể giữ trạng thái nếu muốn
        clearAllCache(); // Xóa cache cũ để bắt đầu mới
        userData.clear(); // Xóa data cũ
        loadContent("step-1.fxml");
    }

    @FXML
    private void showDoiVe() {
        resetMenuButtons();
        btnDoiVe.getStyleClass().add("menu-item-active");
        loadContent("doi-ve.fxml");
    }
    
    @FXML
    private void showTraVe() {
        resetMenuButtons();
        btnTraVe.getStyleClass().add("menu-item-active");
        loadContent("tra-ve.fxml");
    }

    @FXML
    private void showKhachHang() {
        resetMenuButtons();
        btnKhachHang.getStyleClass().add("menu-item-active");
        loadContent("customer-management.fxml");
    }

    @FXML
    private void showHoaDon() {
        resetMenuButtons();
        btnHoaDon.getStyleClass().add("menu-item-active");
        loadContent("invoice-management.fxml");
    }

    @FXML
    private void showThongKe() {
        resetMenuButtons();
        btnThongKe.getStyleClass().add("menu-item-active");
        loadContent("statistics-management.fxml");
    }
    
//    Cũ:
    // HÀM QUAN TRỌNG: Tải FXML vào vùng nội dung
//    public void loadContent(String fxmlFile) {
//        try {
//        // Check if file exists
//        if (getClass().getResource("/views/" + fxmlFile) == null) {
//            showPlaceholder("Chức năng chưa được tạo",
//                    "Trang này đang được phát triển.\nVui lòng quay lại sau!");
//            return;
//        }
//
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/" + fxmlFile));
//        Node view = loader.load();
//        Object controller = loader.getController();
//
//        // Lưu lại instance của controller
//        if (controller instanceof Step1Controller) {
//            step1ControllerInstance = (Step1Controller) controller;
//            step1ControllerInstance.setMainController(this);
//            step1ControllerInstance.initData();
//        }
//        else if (controller instanceof Step2Controller_update) {
//            step2Controller_updateInstance = (Step2Controller_update) controller;
//            step2Controller_updateInstance.setMainController(this);
//            step2Controller_updateInstance.initData();
//        }
//        
//        else if (controller instanceof Step3Controller) {
//            step3ControllerInstance = (Step3Controller) controller;
//            step3ControllerInstance.setMainController(this);
//            step3ControllerInstance.initData();
//        } else if (controller instanceof Step4Controller) {
//            step4ControllerInstance = (Step4Controller) controller;
//            step4ControllerInstance.setMainController(this);
//            step4ControllerInstance.initData();
//        }
//
//        contentPane.getChildren().clear();
//        contentPane.getChildren().add(view);
//
//    } catch (IOException e) {
//        e.printStackTrace();
//        showPlaceholder("Lỗi khi tải giao diện",
//                "Không thể tải file: " + fxmlFile + "\n\n"
//                + "Chi tiết lỗi: " + e.getMessage());
//    }
//    }
    
    // --- HÀM LOAD CONTENT (ĐÃ NÂNG CẤP CACHING) ---
    public void loadContent(String fxmlFile) {
        try {
            Node view = null;
            Object controller = null;
            boolean isFromCache = false;

            // 1. Kiểm tra xem View này đã có trong Cache chưa
            if (fxmlFile.equals("step-1.fxml") && step1View != null) {
                view = step1View;
                controller = step1ControllerInstance;
                isFromCache = true;
            } else if (fxmlFile.equals("step-2.fxml") && step2View != null) {
                view = step2View;
                controller = step2ControllerInstance;
                isFromCache = true;
            } else if (fxmlFile.equals("step-3.fxml") && step3View != null) {
                view = step3View;
                controller = step3ControllerInstance;
                isFromCache = true;
            } else if (fxmlFile.equals("step-4.fxml") && step4View != null) {
                view = step4View;
                controller = step4ControllerInstance;
                isFromCache = true;
            }

            // 2. Nếu chưa có trong Cache -> Load mới
            if (view == null) {
                if (getClass().getResource("/views/" + fxmlFile) == null) {
                    showPlaceholder("Chức năng chưa được tạo", "Trang này đang được phát triển.\nVui lòng quay lại sau!");
                    return;
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/" + fxmlFile));
                view = loader.load();
                controller = loader.getController();

                // Lưu vào Cache tương ứng
                if (fxmlFile.equals("step-1.fxml")) {
                    step1View = view;
                    step1ControllerInstance = (Step1Controller) controller;
                    step1ControllerInstance.setMainController(this);
                    // Init lần đầu
                    step1ControllerInstance.initData(); 
                } else if (fxmlFile.equals("step-2.fxml")) {
                    step2View = view;
                    step2ControllerInstance = (Step2Controller_update) controller;
                    step2ControllerInstance.setMainController(this);
                    // Init lần đầu
                    step2ControllerInstance.initData();
                } else if (fxmlFile.equals("step-3.fxml")) {
                    step3View = view;
                    step3ControllerInstance = (Step3Controller) controller;
                    step3ControllerInstance.setMainController(this);
                    // Init lần đầu
                    step3ControllerInstance.initData();
                } else if (fxmlFile.equals("step-4.fxml")) {
                    step4View = view;
                    step4ControllerInstance = (Step4Controller) controller;
                    step4ControllerInstance.setMainController(this);
                    // Init lần đầu
                    step4ControllerInstance.initData();
                }
            } else {
                // ĐÃ LẤY TỪ CACHE RA
                
                // Trường hợp 1: Vào Step 4 -> Luôn phải tính tiền lại
                if (controller instanceof Step4Controller) {
                     ((Step4Controller) controller).initData(); 
                }
                
                // Trường hợp 2: Vào Step 3 -> Cần load lại danh sách vé mới nếu Step 2 có thay đổi
                else if (controller instanceof Step3Controller) {
                     ((Step3Controller) controller).initData();
                }

                // Trường hợp 3: Quay lại Step 2 -> KHÔNG GỌI initData()
                // Để giữ nguyên trạng thái ghế ngồi và giỏ hàng đang chọn dở
                else if (controller instanceof Step2Controller_update) {
                    // Không làm gì cả, giữ nguyên hiện trường
                    // Trừ khi bạn muốn refresh lại tổng tiền (nếu có logic hủy vé ngầm từ step 3)
                }
            }

            // 4. Hiển thị View
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);

        } catch (IOException e) {
            e.printStackTrace();
            showPlaceholder("Lỗi khi tải giao diện", "Không thể tải file: " + fxmlFile + "\n\n" + "Chi tiết lỗi: " + e.getMessage());
        }
    }
   
    // Hàm xóa cache (dùng khi hoàn tất bán vé hoặc logout)
    private void clearAllCache() {
        step1View = null; step1ControllerInstance = null;
        step2View = null; step2ControllerInstance = null;
        step3View = null; step3ControllerInstance = null;
        step4View = null; step4ControllerInstance = null;
    }
    
    public void startNewTransaction() {
        // 1. Xóa cache giao diện cũ (để các Step được new mới hoàn toàn)
        clearAllCache();
        
        // 2. Xóa dữ liệu tạm trong Map
        userData.clear();
        
        // 3. Reset trạng thái menu (nếu cần)
        resetMenuButtons();
        btnBanVe.getStyleClass().add("menu-item-active");
        
        // 4. Load lại Step 1
        loadContent("step-1.fxml");
    }
    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }   

    public NhanVien getNhanVien() {
        return nhanVien;
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
        // Vì ta đã cache controller, nên instance này vẫn còn sống và giữ đúng dữ liệu
        if (step2ControllerInstance != null) {
            step2ControllerInstance.cancelTicketBySeatId(maCho, isChieuDi);
        } else {
            System.err.println("BanVeController: Step2Controller chưa được khởi tạo (null cache), không thể hủy vé.");
        }
    }
    
    public void setUserData(String key, Object data) { userData.put(key, data); }
    public Object getUserData(String key) { return userData.get(key); }

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