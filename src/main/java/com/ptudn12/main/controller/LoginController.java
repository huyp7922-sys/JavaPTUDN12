package com.ptudn12.main. controller;

import com.ptudn12.main.dao.NhanVienDAO;
import com.ptudn12.main. dao.TaiKhoanDAO;
import com.ptudn12.main.entity.NhanVien;
import com.ptudn12.main.entity. TaiKhoan;
import com.ptudn12.main.util.SessionManager;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml. FXML;
import javafx. fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene. Scene;
import javafx.scene.control.Alert;
import javafx.scene. control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control. TextField;
import javafx.stage.Stage;
import com.ptudn12.main. utils.EmailService;  
import java.util.Optional;  
import javafx.scene.control.TextInputDialog;
import javafx.concurrent.Task;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    
    private NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField. getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Kiểm tra tài khoản từ database
        if (authenticateDatabase(username, password)) {
            return; // Đã xử lý trong hàm
        }
        
    }

    /**
     * Xác thực tài khoản từ database
     */
    private boolean authenticateDatabase(String username, String password) {
        try {
            // Tìm tài khoản
            TaiKhoan taiKhoan = taiKhoanDAO.findById(username);
            
            if (taiKhoan == null) {
                System.out.println("Không tìm thấy tài khoản:  " + username);
                return false;
            }
            
            // Kiểm tra mật khẩu
            if (! taiKhoan.getMatKhau().equals(password)) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên đăng nhập hoặc mật khẩu không đúng");
                return false;
            }

            // Lấy thông tin nhân viên
            NhanVien nhanVien = taiKhoan.getNhanVien();
            
            if (nhanVien == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy thông tin nhân viên!");
                return false;
            }
            
            String trangThaiTK = taiKhoan.getTrangThaiTK().trim().toLowerCase();
            String tinhTrangCV = nhanVien.getTinhTrangCV().trim().toLowerCase();
            
            //  Tài khoản bị ngừng hoạt động (KHÔNG CHO ĐĂNG NHẬP)
            if (trangThaiTK. equals("ngunghan")) {
                showAlert(Alert.AlertType.ERROR, "Tài khoản đã ngừng hoạt động", 
                    "Tài khoản của bạn đã bị ngừng hoạt động!\n" +
                    "Vui lòng liên hệ quản trị viên để được hỗ trợ.");
                return false;
            }
            
            //  Nhân viên đã nghỉ việc (KHÔNG CHO ĐĂNG NHẬP)
            if (tinhTrangCV.equals("đã nghỉ")) {
                showAlert(Alert.AlertType.ERROR, "Nhân viên đã nghỉ việc", 
                    "Bạn đã nghỉ việc và không thể đăng nhập vào hệ thống!\n" +
                    "Vui lòng liên hệ phòng nhân sự để biết thêm chi tiết.");
                return false;
            }
            
            //  Tài khoản tạm ngưng HOẶC Nhân viên tạm nghỉ (CHO ĐĂNG NHẬP VỚI XÁC NHẬN)
            if (trangThaiTK.equals("tamngung") || tinhTrangCV.equals("tạm nghỉ")) {
                boolean shouldActivate = showConfirmationDialog(
                    "Tài khoản/Nhân viên đang tạm ngưng",
                    "Thông tin trạng thái:",
                    "Bạn có muốn mở lại tài khoản và tiếp tục làm việc không?"
                );
                
                if (shouldActivate) {
                    // Cập nhật trạng thái về bình thường
                    boolean updateSuccess = activateAccountAndEmployee(taiKhoan, nhanVien);
                    
                    if (! updateSuccess) {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", 
                            "Không thể cập nhật trạng thái tài khoản!\n" +
                            "Vui lòng liên hệ quản trị viên.");
                        return false;
                    }
                    
                    // Reload lại thông tin sau khi cập nhật
                    taiKhoan = taiKhoanDAO.findById(username);
                    nhanVien = taiKhoan.getNhanVien();
                    
                    showAlert(Alert.AlertType. INFORMATION, "Thành công", 
                        "Đã mở lại tài khoản và kích hoạt trạng thái làm việc!");
                    
                } else {
                    // Người dùng chọn Hủy - không đăng nhập
                    return false;
                }
            }
            
            // Kiểm tra tài khoản đang hoạt động bình thường
            if (! trangThaiTK.equals("danghoatdong")) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", 
                    "Tài khoản không hoạt động!\n" +
                    "Trạng thái hiện tại: " + taiKhoan.getTrangThaiTK());
                return false;
            }
            
            // Kiểm tra nhân viên đang làm việc
            if (!tinhTrangCV.equals("đang làm")) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", 
                    "Nhân viên không thể làm việc!\n" +
                    "Trạng thái:  " + nhanVien.getTinhTrangCV());
                return false;
            }
            
            // Lưu session
            SessionManager.getInstance().login(username, nhanVien, taiKhoan);
            
            // Lấy quyền từ SessionManager
            boolean isAdmin = SessionManager.getInstance().isAdmin();
            
            // Load dashboard tương ứng
            loadDashboard(nhanVien. getTenNhanVien(), isAdmin);
            
            // Hiển thị thông báo thành công
            showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                "Đăng nhập thành công!\n" +
                "Chào mừng:  " + nhanVien.getTenNhanVien() + "\n" +
                "Vai trò: " + SessionManager.getInstance().getRole());
            
            return true; 
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi đăng nhập:  " + e.getMessage());
            return false;
        }
    }

    /**
     * Kích hoạt lại tài khoản và nhân viên
     */
    private boolean activateAccountAndEmployee(TaiKhoan taiKhoan, NhanVien nhanVien) {
        try {
            boolean success = true;
            
            // Cập nhật trạng thái tài khoản về DangHoatDong
            if (taiKhoan.getTrangThaiTK().trim().equalsIgnoreCase("tamngung")) {
                taiKhoan.setTrangThaiTK("DangHoatDong");
                success = success && taiKhoanDAO. update(taiKhoan);
            }
            
            // Cập nhật tình trạng nhân viên về Đang làm
            if (nhanVien.getTinhTrangCV().trim().equalsIgnoreCase("tạm nghỉ")) {
                nhanVien.setTinhTrangCV("Đang làm");
                success = success && nhanVienDAO.update(nhanVien);
            }
            
            return success;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Hiển thị dialog xác nhận với nút Xác nhận và Hủy
     */
    private boolean showConfirmationDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType. CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // Custom button text
        ButtonType btnXacNhan = new ButtonType("Xác nhận mở tài khoản");
        ButtonType btnHuy = new ButtonType("Hủy");
        
        alert. getButtonTypes().setAll(btnXacNhan, btnHuy);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == btnXacNhan;
    }

    /**
     * Load giao diện Dashboard theo quyền
     */
    private void loadDashboard(String displayName, boolean isAdmin) {
        try {
            FXMLLoader loader;
            String title;
            String role = isAdmin ? "Quản Lý" : "Nhân Viên";
            
            if (isAdmin) {
                loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
                title = "Hệ Thống Quản Lý Bán Vé Tàu - " + role + " (" + displayName + ")";
            } else {
                loader = new FXMLLoader(getClass().getResource("/views/ban-ve.fxml"));
                title = "Hệ Thống Quản Lý Bán Vé Tàu - " + role + " (" + displayName + ")";
            }
            
            Parent root = loader.load();
            
            // Set username cho controller tương ứng
            if (isAdmin) {
                DashboardController controller = loader.getController();
                controller.setUsername(displayName);
            } else {
                BanVeController controller = loader. getController();
                controller.setUsername(displayName);
            }

            // Chuyển scene
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage. setMaximized(true);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType. ERROR, "Lỗi", "Không thể tải giao diện:  " + e.getMessage());
        }
    }

    @FXML
    private void handleForgotPassword() {
        // Tạo dialog nhập mã nhân viên
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Quên mật khẩu");
        dialog.setHeaderText("Khôi phục mật khẩu");
        dialog.setContentText("Nhập mã nhân viên của bạn:");
        
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            String maNhanVien = result.get().trim();
            
            if (maNhanVien.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập mã nhân viên!");
                return;
            }
            
            // Show loading trước
            Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
            loadingAlert.setTitle("Đang xử lý");
            loadingAlert.setHeaderText(null);
            loadingAlert. setContentText("Đang gửi email khôi phục mật khẩu...");
            loadingAlert.show();
            
            // Xử lý trong background thread
            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return processPasswordReset(maNhanVien);
                }
            };
            
            task.setOnSucceeded(e -> {
                loadingAlert. close();
                if (task.getValue()) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                        "Mật khẩu mới đã được gửi về email của bạn!\n" +
                        "Vui lòng kiểm tra email (kể cả thư mục Spam).");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", 
                        "Không thể khôi phục mật khẩu!\n" +
                        "Vui lòng kiểm tra lại mã nhân viên hoặc liên hệ quản trị viên.");
                }
            });
            
            task.setOnFailed(e -> {
                loadingAlert. close();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra:  " + task.getException().getMessage());
            });
            
            new Thread(task).start();
        }
    }

    /**
     * Xử lý reset mật khẩu
     */
    private boolean processPasswordReset(String maNhanVien) {
        try {
            // Tìm tài khoản
            TaiKhoan taiKhoan = taiKhoanDAO. findById(maNhanVien);
            
            if (taiKhoan == null) {
                System.err.println("Không tìm thấy tài khoản: " + maNhanVien);
                return false;
            }
            
            // Chỉ cho phép reset nếu tài khoản đang hoạt động hoặc tạm ngưng
            String trangThai = taiKhoan. getTrangThaiTK().trim().toLowerCase();
            if (! trangThai.equals("danghoatdong") && !trangThai.equals("tamngung")) {
                System.err.println("Tài khoản không thể reset mật khẩu.  Trạng thái:  " + trangThai);
                return false;
            }
            
            // Lấy thông tin nhân viên
            NhanVien nhanVien = taiKhoan.getNhanVien();
            if (nhanVien == null || nhanVien.getEmail() == null || nhanVien.getEmail().isEmpty()) {
                System.err. println("Không tìm thấy email nhân viên");
                return false;
            }
            
            // Tạo mật khẩu mới
            String newPassword = EmailService.generateRandomPassword();
            
            // Gửi email
            boolean emailSent = EmailService.sendPasswordResetEmail(
                nhanVien.getEmail(), 
                newPassword, 
                nhanVien.getTenNhanVien()
            );
            
            if (! emailSent) {
                System.err.println("Gửi email thất bại");
                return false;
            }
            
            // Cập nhật mật khẩu trong database
            taiKhoan.setMatKhau(newPassword);
            boolean updated = taiKhoanDAO. update(taiKhoan);
            
            if (updated) {
                System.out.println("Đã cập nhật mật khẩu mới cho:  " + maNhanVien);
                return true;
            } else {
                System.err. println("Cập nhật mật khẩu thất bại");
            }
            
            return false;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Hiển thị alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}