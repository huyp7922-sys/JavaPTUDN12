package com.ptudn12.main.controller;

import com.ptudn12.main.dao.NhanVienDAO;
import com.ptudn12.main.dao.TaiKhoanDAO;
import com.ptudn12.main.entity.NhanVien;
import com.ptudn12.main.entity.TaiKhoan;
import com.ptudn12.main.util.SessionManager;
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
    
    private NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Kiểm tra tài khoản test trước
        if (authenticateTestAccount(username, password)) {
            return; // Đã xử lý trong hàm
        }
        
        // Kiểm tra tài khoản từ database
        if (authenticateDatabase(username, password)) {
            return; // Đã xử lý trong hàm
        }
        
        // Sai tên đăng nhập hoặc mật khẩu
        showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên đăng nhập hoặc mật khẩu không đúng!");
    }

    /**
     * Xác thực tài khoản test (admin/nhanvien)
     */
    private boolean authenticateTestAccount(String username, String password) {
        boolean isAdmin = false;
        boolean isValid = false;
        
        if (username.equals("admin") && password.equals("admin123")) {
            isAdmin = true;
            isValid = true;
        } else if (username.equals("nhanvien") && password.equals("nhanvien123")) {
            isAdmin = false;
            isValid = true;
        }
        
        if (isValid) {
            // Lưu session với tài khoản test (không có NhanVien object)
            SessionManager.getInstance().login(username, null, null);
            
            // Load giao diện tương ứng
            loadDashboard(username, isAdmin);
            return true;
        }
        
        return false;
    }

    /**
     * Xác thực tài khoản từ database
     */
    private boolean authenticateDatabase(String username, String password) {
        try {
            // Tìm tài khoản theo mã nhân viên
            TaiKhoan taiKhoan = taiKhoanDAO.findById(username);
            
            if (taiKhoan == null) {
                return false;
            }
            
            // Kiểm tra mật khẩu
            if (!taiKhoan.getMatKhau().equals(password)) {
                return false;
            }
            
            // Kiểm tra trạng thái tài khoản
            if (!taiKhoan.getTrangThaiTK().equalsIgnoreCase("hoatdong")) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Tài khoản đã bị khóa!");
                return false;
            }
            
            // Lấy thông tin nhân viên
            NhanVien nhanVien = taiKhoan.getNhanVien();
            
            if (nhanVien == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy thông tin nhân viên!");
                return false;
            }
            
            // Kiểm tra trạng thái nhân viên
            if (!nhanVien.getTinhTrangCV().equalsIgnoreCase("Đang làm việc")) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Nhân viên đã nghỉ việc!");
                return false;
            }
            
            // Lưu session
            SessionManager.getInstance().login(username, nhanVien, taiKhoan);
            
            // ===== SỬA ĐOẠN NÀY (DÒNG 115-120) =====
            // Load giao diện tương ứng với quyền
            String chucVu = nhanVien.getChucVuText();
            boolean isAdmin = chucVu != null && chucVu.equalsIgnoreCase("Quản lý");
            loadDashboard(nhanVien.getTenNhanVien(), isAdmin);
            // ========================================
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi đăng nhập: " + e.getMessage());
            return false;
        }
    }

    /**
     * Load giao diện Dashboard theo quyền
     */
    private void loadDashboard(String displayName, boolean isAdmin) {
        try {
            FXMLLoader loader;
            String title;
            
            if (isAdmin) {
                // Quản lý -> Dashboard đầy đủ
                loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
                title = "Hệ Thống Quản Lý Bán Vé Tàu - Quản Lý (" + displayName + ")";
            } else {
                // Nhân viên -> Giao diện bán vé
                loader = new FXMLLoader(getClass().getResource("/views/ban-ve.fxml"));
                title = "Hệ Thống Quản Lý Bán Vé Tàu - Nhân Viên (" + displayName + ")";
            }
            
            Parent root = loader.load();
            
            // Set username cho controller
            if (isAdmin) {
                DashboardController controller = loader.getController();
                controller.setUsername(displayName);
            } else {
                BanVeController controller = loader.getController();
                controller.setUsername(displayName);
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setMaximized(true);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải giao diện!");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}